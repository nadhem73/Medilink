package com.medilinktunisia.teleconsultationservice.exception;

public class TeleconsultationNotFoundException extends RuntimeException {
    public TeleconsultationNotFoundException(String message) {
        super(message);
    }

    public TeleconsultationNotFoundException(Long id) {
        super("Teleconsultation not found with id: " + id);
    }

    public TeleconsultationNotFoundException(String field, String value) {
        super("Teleconsultation not found with " + field + ": " + value);
    }
}
