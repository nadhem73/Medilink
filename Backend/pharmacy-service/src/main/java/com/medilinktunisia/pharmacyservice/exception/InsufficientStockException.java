package com.medilinktunisia.pharmacyservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String medicationName, int available, int requested) {
        super(String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d", 
                          medicationName, available, requested));
    }
}
