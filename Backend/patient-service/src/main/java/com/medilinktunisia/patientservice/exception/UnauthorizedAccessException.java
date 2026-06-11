package com.medilinktunisia.patientservice.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException() {
        super("Vous n'êtes pas autorisé à accéder à cette ressource");
    }
}
