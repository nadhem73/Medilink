package com.medilinktunisia.ambulanceservice.service;

import com.medilinktunisia.ambulanceservice.exception.ResourceNotFoundException;
import com.medilinktunisia.ambulanceservice.model.dto.AmbulanceDto;
import com.medilinktunisia.ambulanceservice.model.dto.LocationUpdateRequest;
import com.medilinktunisia.ambulanceservice.model.entity.Ambulance;
import com.medilinktunisia.ambulanceservice.model.entity.AmbulanceLocation;
import com.medilinktunisia.ambulanceservice.model.entity.Emergency;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import com.medilinktunisia.ambulanceservice.repository.AmbulanceLocationRepository;
import com.medilinktunisia.ambulanceservice.repository.AmbulanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;
    private final AmbulanceLocationRepository locationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<AmbulanceDto> getAllAmbulances() {
        return ambulanceRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AmbulanceDto> getAvailableAmbulances() {
        return ambulanceRepository.findByActiveTrueAndStatus(AmbulanceStatus.AVAILABLE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AmbulanceDto getAmbulanceById(Long id) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance non trouvée avec l'ID: " + id));
        return convertToDto(ambulance);
    }

    @Transactional
    public AmbulanceDto createAmbulance(AmbulanceDto ambulanceDto) {
        Ambulance ambulance = convertToEntity(ambulanceDto);
        ambulance = ambulanceRepository.save(ambulance);
        log.info("Nouvelle ambulance créée: {}", ambulance.getRegistrationNumber());
        return convertToDto(ambulance);
    }

    @Transactional
    public AmbulanceDto updateAmbulance(Long id, AmbulanceDto ambulanceDto) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance non trouvée avec l'ID: " + id));

        updateAmbulanceFields(ambulance, ambulanceDto);
        ambulance = ambulanceRepository.save(ambulance);
        log.info("Ambulance mise à jour: {}", ambulance.getRegistrationNumber());
        return convertToDto(ambulance);
    }

    @Transactional
    public void updateLocation(LocationUpdateRequest request) {
        Ambulance ambulance = ambulanceRepository.findById(request.getAmbulanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance non trouvée"));

        // Mettre à jour la position de l'ambulance
        ambulance.setCurrentLatitude(request.getLatitude());
        ambulance.setCurrentLongitude(request.getLongitude());
        ambulanceRepository.save(ambulance);

        // Sauvegarder l'historique de position
        AmbulanceLocation location = new AmbulanceLocation();
        location.setAmbulance(ambulance);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setSpeed(request.getSpeed());
        location.setHeading(request.getHeading());
        location.setAccuracy(request.getAccuracy());
        locationRepository.save(location);

        // Envoyer la mise à jour en temps réel via WebSocket
        messagingTemplate.convertAndSend("/topic/ambulance/" + ambulance.getId() + "/location", request);

        log.debug("Position mise à jour pour l'ambulance {}", ambulance.getRegistrationNumber());
    }

    public Ambulance findNearestAvailable(Double latitude, Double longitude) {
        return ambulanceRepository.findNearestAvailable(latitude, longitude)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune ambulance disponible trouvée"));
    }

    public List<Ambulance> findAvailableNearby(Double latitude, Double longitude, Double radiusKm) {
        return ambulanceRepository.findAvailableNearby(latitude, longitude, radiusKm);
    }

    @Transactional
    public void updateStatus(Long id, AmbulanceStatus status) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance non trouvée"));
        ambulance.setStatus(status);
        ambulanceRepository.save(ambulance);
        log.info("Statut de l'ambulance {} changé à {}", ambulance.getRegistrationNumber(), status);
    }

    @Transactional
    public void deleteAmbulance(Long id) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance non trouvée"));
        ambulance.setActive(false);
        ambulanceRepository.save(ambulance);
        log.info("Ambulance désactivée: {}", ambulance.getRegistrationNumber());
    }

    private AmbulanceDto convertToDto(Ambulance ambulance) {
        AmbulanceDto dto = new AmbulanceDto();
        dto.setId(ambulance.getId());
        dto.setRegistrationNumber(ambulance.getRegistrationNumber());
        dto.setVehicleModel(ambulance.getVehicleModel());
        dto.setStatus(ambulance.getStatus());
        dto.setCurrentLatitude(ambulance.getCurrentLatitude());
        dto.setCurrentLongitude(ambulance.getCurrentLongitude());
        dto.setBaseStation(ambulance.getBaseStation());
        dto.setEquipment(ambulance.getEquipment());
        dto.setHasDefibrillator(ambulance.getHasDefibrillator());
        dto.setHasOxygenSupply(ambulance.getHasOxygenSupply());
        dto.setHasAdvancedLifeSupport(ambulance.getHasAdvancedLifeSupport());
        dto.setDriverName(ambulance.getDriverName());
        dto.setDriverPhone(ambulance.getDriverPhone());
        dto.setParamedicName(ambulance.getParamedicName());
        dto.setParamedicPhone(ambulance.getParamedicPhone());
        dto.setCapacity(ambulance.getCapacity());
        dto.setActive(ambulance.getActive());
        dto.setLastMaintenanceDate(ambulance.getLastMaintenanceDate());
        dto.setNextMaintenanceDate(ambulance.getNextMaintenanceDate());
        dto.setNotes(ambulance.getNotes());
        return dto;
    }

    private Ambulance convertToEntity(AmbulanceDto dto) {
        Ambulance ambulance = new Ambulance();
        updateAmbulanceFields(ambulance, dto);
        return ambulance;
    }

    private void updateAmbulanceFields(Ambulance ambulance, AmbulanceDto dto) {
        ambulance.setRegistrationNumber(dto.getRegistrationNumber());
        ambulance.setVehicleModel(dto.getVehicleModel());
        if (dto.getStatus() != null) ambulance.setStatus(dto.getStatus());
        ambulance.setCurrentLatitude(dto.getCurrentLatitude());
        ambulance.setCurrentLongitude(dto.getCurrentLongitude());
        ambulance.setBaseStation(dto.getBaseStation());
        ambulance.setEquipment(dto.getEquipment());
        ambulance.setHasDefibrillator(dto.getHasDefibrillator());
        ambulance.setHasOxygenSupply(dto.getHasOxygenSupply());
        ambulance.setHasAdvancedLifeSupport(dto.getHasAdvancedLifeSupport());
        ambulance.setDriverName(dto.getDriverName());
        ambulance.setDriverPhone(dto.getDriverPhone());
        ambulance.setParamedicName(dto.getParamedicName());
        ambulance.setParamedicPhone(dto.getParamedicPhone());
        ambulance.setCapacity(dto.getCapacity());
        if (dto.getActive() != null) ambulance.setActive(dto.getActive());
        ambulance.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        ambulance.setNextMaintenanceDate(dto.getNextMaintenanceDate());
        ambulance.setNotes(dto.getNotes());
    }
}
