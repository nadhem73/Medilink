package com.medilinktunisia.ambulanceservice.service;

import com.medilinktunisia.ambulanceservice.model.entity.Emergency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service de notification (SMS, Email, Push)
 * TODO: Implémenter l'intégration avec les services externes
 */
@Service
@Slf4j
public class NotificationService {

    public void sendEmergencyCreatedNotification(Emergency emergency) {
        log.info("Notification: Nouvelle urgence créée - {}", emergency.getEmergencyCode());
        // TODO: Envoyer SMS à l'appelant
        // TODO: Notifier le dispatcher
    }

    public void sendAmbulanceAssignedNotification(Emergency emergency) {
        log.info("Notification: Ambulance assignée à l'urgence {}", emergency.getEmergencyCode());
        // TODO: Envoyer SMS à l'appelant avec l'ETA
        // TODO: Notifier l'équipage de l'ambulance
    }

    public void sendAmbulanceEnRouteNotification(Emergency emergency) {
        log.info("Notification: Ambulance en route vers {}", emergency.getAddress());
        // TODO: Envoyer SMS de mise à jour
    }

    public void sendArrivalNotification(Emergency emergency) {
        log.info("Notification: Ambulance arrivée sur les lieux");
        // TODO: Notifier l'hôpital de destination
    }

    public void sendCompletedNotification(Emergency emergency) {
        log.info("Notification: Intervention terminée pour {}", emergency.getEmergencyCode());
        // TODO: Envoyer résumé par email
    }
}
