package com.medilinktunisia.patientservice.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(Long id) {
        super("Patient non trouvé avec l'ID: " + id);
    }
}
