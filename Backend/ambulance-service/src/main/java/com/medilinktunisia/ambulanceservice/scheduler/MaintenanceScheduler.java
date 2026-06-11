package com.medilinktunisia.ambulanceservice.scheduler;

import com.medilinktunisia.ambulanceservice.repository.AmbulanceLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Tâches planifiées pour la maintenance automatique
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceScheduler {

    private final AmbulanceLocationRepository locationRepository;

    /**
     * Nettoie les anciennes positions GPS (> 30 jours)
     * Exécuté tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanOldLocations() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        locationRepository.deleteByTimestampBefore(thirtyDaysAgo);
        log.info("Nettoyage des positions GPS > 30 jours effectué");
    }

    /**
     * Vérification des ambulances nécessitant une maintenance
     * Exécuté tous les jours à 8h du matin
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkMaintenanceDue() {
        log.info("Vérification des maintenances dues");
        // TODO: Implémenter la vérification et l'envoi d'alertes
    }
}
