package com.medilinktunisia.pharmacyservice.exception;

public class MedicationNotFoundException extends RuntimeException {
    public MedicationNotFoundException(String message) {
        super(message);
    }

    public MedicationNotFoundException(Long id) {
        super("Médicament non trouvé avec l'ID: " + id);
    }
}
