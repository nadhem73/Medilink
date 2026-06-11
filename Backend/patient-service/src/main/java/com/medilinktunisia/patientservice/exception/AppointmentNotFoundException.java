package com.medilinktunisia.patientservice.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String message) {
        super(message);
    }

    public AppointmentNotFoundException(Long id) {
        super("Rendez-vous non trouvé avec l'ID: " + id);
    }
}
