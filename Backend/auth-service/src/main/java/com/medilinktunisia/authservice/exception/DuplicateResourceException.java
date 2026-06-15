package com.medilinktunisia.authservice.exception;

/**
 * Levée lorsqu'une valeur censée être unique (téléphone, CIN, ...) existe déjà.
 * Traduite en réponse HTTP 409 (CONFLICT) par le GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
