package com.medilinktunisia.prescriptionservice.exception;

public class PrescriptionExpiredException extends RuntimeException {
    public PrescriptionExpiredException(String message) {
        super(message);
    }

    public PrescriptionExpiredException(Long id) {
        super("Prescription has expired: " + id);
    }
}
