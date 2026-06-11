package com.medilinktunisia.ambulanceservice.service;

import com.medilinktunisia.ambulanceservice.model.entity.Ambulance;
import com.medilinktunisia.ambulanceservice.model.entity.Emergency;
import com.medilinktunisia.ambulanceservice.model.enums.AmbulanceStatus;
import com.medilinktunisia.ambulanceservice.model.enums.EmergencyStatus;
import com.medilinktunisia.ambulanceservice.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service de dispatch intelligent pour l'affectation automatique des ambulances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchService {

    private final AmbulanceService ambulanceService;
    private final EmergencyRepository emergencyRepository;

    @Transactional
    public void autoAssignAmbulance(Emergency emergency) {
        log.info("Recherche d'ambulance disponible pour l'urgence {}", emergency.getEmergencyCode());

        // Trouver l'ambulance la plus proche
        Ambulance nearestAmbulance = ambulanceService.findNearestAvailable(
            emergency.getLatitude(),
            emergency.getLongitude()
        );

        if (nearestAmbulance != null) {
            // Assigner l'ambulance
            emergency.setAssignedAmbulance(nearestAmbulance);
            emergency.setStatus(EmergencyStatus.ASSIGNED);
            emergency.setAssignedAt(LocalDateTime.now());

            // Calculer le temps estimé d'arrivée
            double distance = calculateDistance(
                nearestAmbulance.getCurrentLatitude(),
                nearestAmbulance.getCurrentLongitude(),
                emergency.getLatitude(),
                emergency.getLongitude()
            );
            
            // Estimation: vitesse moyenne de 60 km/h en ville
            int estimatedMinutes = (int) Math.ceil((distance / 60.0) * 60);
            emergency.setEstimatedArrivalMinutes(estimatedMinutes);

            // Mettre à jour le statut de l'ambulance
            ambulanceService.updateStatus(nearestAmbulance.getId(), AmbulanceStatus.ON_MISSION);

            emergencyRepository.save(emergency);

            log.info("Ambulance {} automatiquement assignée à l'urgence {} (distance: {} km, ETA: {} min)",
                nearestAmbulance.getRegistrationNumber(),
                emergency.getEmergencyCode(),
                String.format("%.2f", distance),
                estimatedMinutes);
        } else {
            log.warn("Aucune ambulance disponible pour l'urgence {}", emergency.getEmergencyCode());
        }
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
