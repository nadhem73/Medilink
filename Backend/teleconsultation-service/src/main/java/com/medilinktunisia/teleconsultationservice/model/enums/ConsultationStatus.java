package com.medilinktunisia.teleconsultationservice.model.enums;

public enum ConsultationStatus {
    SCHEDULED,      // Planifiée
    WAITING,        // En attente (salle d'attente)
    IN_PROGRESS,    // En cours
    COMPLETED,      // Terminée
    CANCELLED,      // Annulée
    NO_SHOW         // Patient absent
}
