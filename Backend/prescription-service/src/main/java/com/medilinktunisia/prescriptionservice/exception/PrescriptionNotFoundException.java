package com.medilinktunisia.prescriptionservice.exception;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }

    public PrescriptionNotFoundException(Long id) {
        super("Prescription not found with id: " + id);
    }

    public PrescriptionNotFoundException(String field, String value) {
        super("Prescription not found with " + field + ": " + value);
    }
}
