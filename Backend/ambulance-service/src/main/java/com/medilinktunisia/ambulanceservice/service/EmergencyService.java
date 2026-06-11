package com.medilinktunisia.ambulanceservice.service;

import com.medilinktunisia.ambulanceservice.exception.ResourceNotFoundException;
import com.medilinktunisia.ambulanceservice.model.dto.EmergencyCreateRequest;
import com.medilinktunisia.ambulanceservice.model.dto.EmergencyDto;
import com.medilinktunisia.ambulanceservice.model.entity.Ambulance;
import com.medilinktunisia.ambulanceservice.model.entity.Emergency;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final AmbulanceService ambulanceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DispatchService dispatchService;
    private final NotificationService notificationService;

    @Transactional
    public EmergencyDto createEmergency(EmergencyCreateRequest request) {
        // Créer l'urgence
        Emergency emergency = new Emergency();
        emergency.setEmergencyCode(generateEmergencyCode());
        emergency.setCallerName(request.getCallerName());
        emergency.setCallerPhone(request.getCallerPhone());
        emergency.setCallerRelationToPatient(request.getCallerRelationToPatient());
        emergency.setPatientName(request.getPatientName());
        emergency.setPatientAge(request.getPatientAge());
        emergency.setPatientGender(request.getPatientGender());
        emergency.setPatientId(request.getPatientId());
        emergency.setEmergencyType(request.getEmergencyType());
        emergency.setPriority(request.getPriority());
        emergency.setDescription(request.getDescription());
        emergency.setSymptoms(request.getSymptoms());
        emergency.setAddress(request.getAddress());
        emergency.setLatitude(request.getLatitude());
        emergency.setLongitude(request.getLongitude());
        emergency.setLocationDetails(request.getLocationDetails());
        emergency.setStatus(EmergencyStatus.PENDING);

        emergency = emergencyRepository.save(emergency);

        log.info("Nouvelle urgence créée: {} - {} {}", 
            emergency.getEmergencyCode(), 
            emergency.getEmergencyType(), 
            emergency.getPriority());

        // Notification temps réel vers le dashboard dispatch
        messagingTemplate.convertAndSend("/topic/emergencies/new", convertToDto(emergency));

        // Notification création urgence
        notificationService.sendEmergencyCreatedNotification(emergency);

        // Tentative d'affectation automatique
        try {
            dispatchService.autoAssignAmbulance(emergency);
        } catch (Exception e) {
            log.warn("Impossible d'assigner automatiquement une ambulance: {}", e.getMessage());
        }

        return convertToDto(emergency);
    }

    public List<EmergencyDto> getAllEmergencies() {
        return emergencyRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<EmergencyDto> getActiveEmergencies() {
        return emergencyRepository.findActiveEmergencies()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EmergencyDto getEmergencyById(Long id) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée avec l'ID: " + id));
        return convertToDto(emergency);
    }

    public EmergencyDto getEmergencyByCode(String code) {
        Emergency emergency = emergencyRepository.findByEmergencyCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée avec le code: " + code));
        return convertToDto(emergency);
    }

    @Transactional
    public EmergencyDto assignAmbulance(Long emergencyId, Long ambulanceId) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée"));

        Ambulance ambulance = ambulanceService.findNearestAvailable(
            emergency.getLatitude(), 
            emergency.getLongitude()
        );

        emergency.setAssignedAmbulance(ambulance);
        emergency.setStatus(EmergencyStatus.ASSIGNED);
        emergency.setAssignedAt(LocalDateTime.now());

        // Calculer le temps estimé d'arrivée (simple estimation basée sur la distance)
        double distance = calculateDistance(
            ambulance.getCurrentLatitude(), 
            ambulance.getCurrentLongitude(),
            emergency.getLatitude(), 
            emergency.getLongitude()
        );
        emergency.setEstimatedArrivalMinutes((int) Math.ceil(distance / 0.8)); // 50 km/h moyen

        ambulanceService.updateStatus(ambulance.getId(), AmbulanceStatus.ON_MISSION);

        emergency = emergencyRepository.save(emergency);

        log.info("Ambulance {} assignée à l'urgence {}", 
            ambulance.getRegistrationNumber(), 
            emergency.getEmergencyCode());

        // Notification temps réel
        messagingTemplate.convertAndSend(
            "/topic/emergency/" + emergencyId + "/assigned", 
            convertToDto(emergency)
        );

        // Notification assignation
        notificationService.sendAmbulanceAssignedNotification(emergency);

        return convertToDto(emergency);
    }

    @Transactional
    public EmergencyDto updateStatus(Long id, EmergencyStatus newStatus) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée"));

        EmergencyStatus oldStatus = emergency.getStatus();
        emergency.setStatus(newStatus);

        // Mettre à jour les timestamps selon le statut
        switch (newStatus) {
            case ASSIGNED -> emergency.setAssignedAt(LocalDateTime.now());
            case EN_ROUTE -> {
                emergency.setDispatchedAt(LocalDateTime.now());
                if (emergency.getAssignedAmbulance() != null) {
                    ambulanceService.updateStatus(
                        emergency.getAssignedAmbulance().getId(), 
                        AmbulanceStatus.EN_ROUTE
                    );
                }
                notificationService.sendAmbulanceEnRouteNotification(emergency);
            }
            case AT_SCENE -> {
                emergency.setArrivedAt(LocalDateTime.now());
                if (emergency.getRequestedAt() != null && emergency.getArrivedAt() != null) {
                    emergency.setActualResponseTimeMinutes(
                        (int) Duration.between(emergency.getRequestedAt(), emergency.getArrivedAt()).toMinutes()
                    );
                }
                if (emergency.getAssignedAmbulance() != null) {
                    ambulanceService.updateStatus(
                        emergency.getAssignedAmbulance().getId(), 
                        AmbulanceStatus.AT_SCENE
                    );
                }
                notificationService.sendArrivalNotification(emergency);
            }
            case TRANSPORTING -> {
                if (emergency.getAssignedAmbulance() != null) {
                    ambulanceService.updateStatus(
                        emergency.getAssignedAmbulance().getId(), 
                        AmbulanceStatus.TRANSPORTING
                    );
                }
            }
            case COMPLETED, CANCELLED -> {
                emergency.setCompletedAt(LocalDateTime.now());
                if (emergency.getAssignedAmbulance() != null) {
                    ambulanceService.updateStatus(
                        emergency.getAssignedAmbulance().getId(), 
                        AmbulanceStatus.AVAILABLE
                    );
                }
                if (newStatus == EmergencyStatus.COMPLETED) {
                    notificationService.sendCompletedNotification(emergency);
                }
            }
        }

        emergency = emergencyRepository.save(emergency);

        log.info("Statut urgence {} changé de {} à {}", 
            emergency.getEmergencyCode(), oldStatus, newStatus);

        // Notification temps réel
        messagingTemplate.convertAndSend(
            "/topic/emergency/" + id + "/status", 
            convertToDto(emergency)
        );

        return convertToDto(emergency);
    }

    @Transactional
    public EmergencyDto addParamedicNotes(Long id, String notes) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée"));
        emergency.setParamedicNotes(notes);
        emergency = emergencyRepository.save(emergency);
        return convertToDto(emergency);
    }

    @Transactional
    public EmergencyDto setDestinationHospital(Long id, String hospital, String address) {
        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Urgence non trouvée"));
        emergency.setDestinationHospital(hospital);
        emergency.setDestinationHospitalAddress(address);
        emergency = emergencyRepository.save(emergency);
        return convertToDto(emergency);
    }

    private String generateEmergencyCode() {
        return "EMG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Formule de Haversine
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private EmergencyDto convertToDto(Emergency emergency) {
        EmergencyDto dto = new EmergencyDto();
        dto.setId(emergency.getId());
        dto.setEmergencyCode(emergency.getEmergencyCode());
        dto.setCallerName(emergency.getCallerName());
        dto.setCallerPhone(emergency.getCallerPhone());
        dto.setCallerRelationToPatient(emergency.getCallerRelationToPatient());
        dto.setPatientName(emergency.getPatientName());
        dto.setPatientAge(emergency.getPatientAge());
        dto.setPatientGender(emergency.getPatientGender());
        dto.setPatientId(emergency.getPatientId());
        dto.setEmergencyType(emergency.getEmergencyType());
        dto.setPriority(emergency.getPriority());
        dto.setDescription(emergency.getDescription());
        dto.setSymptoms(emergency.getSymptoms());
        dto.setAddress(emergency.getAddress());
        dto.setLatitude(emergency.getLatitude());
        dto.setLongitude(emergency.getLongitude());
        dto.setLocationDetails(emergency.getLocationDetails());
        dto.setStatus(emergency.getStatus());
        dto.setRequestedAt(emergency.getRequestedAt());
        dto.setAssignedAt(emergency.getAssignedAt());
        dto.setArrivedAt(emergency.getArrivedAt());
        dto.setCompletedAt(emergency.getCompletedAt());
        dto.setEstimatedArrivalMinutes(emergency.getEstimatedArrivalMinutes());
        dto.setDestinationHospital(emergency.getDestinationHospital());
        dto.setParamedicNotes(emergency.getParamedicNotes());
        
        if (emergency.getAssignedAmbulance() != null) {
            dto.setAssignedAmbulance(ambulanceService.getAmbulanceById(emergency.getAssignedAmbulance().getId()));
        }
        
        return dto;
    }
}
