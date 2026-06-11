package com.medilinktunisia.pharmacyservice.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException() {
        super("Accès non autorisé à cette ressource");
    }
}
