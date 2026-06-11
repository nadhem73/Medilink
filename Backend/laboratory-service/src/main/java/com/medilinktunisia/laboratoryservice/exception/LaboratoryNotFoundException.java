package com.medilinktunisia.laboratoryservice.exception;

public class LaboratoryNotFoundException extends RuntimeException {
    public LaboratoryNotFoundException(String message) {
        super(message);
    }
}
