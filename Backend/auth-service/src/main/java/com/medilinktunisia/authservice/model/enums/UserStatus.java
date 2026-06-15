package com.medilinktunisia.authservice.model.enums;

/**
 * Statut du compte (StatutEnum du diagramme : actif, inactif, suspendu).
 * PENDING couvre la période avant vérification de l'email.
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
