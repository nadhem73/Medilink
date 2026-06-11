package com.medilinktunisia.ambulanceservice.model.enums;

public enum EmergencyStatus {
    PENDING,        // En attente d'affectation
    ASSIGNED,       // Affectée à une ambulance
    EN_ROUTE,       // Ambulance en route
    AT_SCENE,       // Sur les lieux
    TRANSPORTING,   // Transport en cours
    COMPLETED,      // Terminée
    CANCELLED       // Annulée
}
