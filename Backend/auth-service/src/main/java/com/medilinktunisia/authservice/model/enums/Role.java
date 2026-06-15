package com.medilinktunisia.authservice.model.enums;

/**
 * Rôles de la plateforme (RoleEnum du diagramme de classe).
 * Pour l'instant seul PATIENT s'auto-inscrit ; les autres rôles sont créés
 * par un administrateur.
 */
public enum Role {
    PATIENT,
    DOCTOR,
    PHARMACY,
    ADMIN
}
