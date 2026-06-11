package com.medilinktunisia.prescriptionservice.model.enums;

public enum PrescriptionStatus {
    ACTIVE,      // Ordonnance active
    DISPENSED,   // Délivrée en pharmacie
    PARTIALLY_DISPENSED, // Partiellement délivrée
    EXPIRED,     // Expirée
    CANCELLED    // Annulée
}
