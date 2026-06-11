package com.medilinktunisia.ambulanceservice.model.enums;

public enum AmbulanceStatus {
    AVAILABLE,      // Disponible pour intervention
    ON_MISSION,     // En mission
    UNAVAILABLE,    // Indisponible (maintenance, etc.)
    EN_ROUTE,       // En route vers le patient
    AT_SCENE,       // Sur les lieux
    TRANSPORTING,   // Transport du patient
    AT_HOSPITAL     // À l'hôpital
}
