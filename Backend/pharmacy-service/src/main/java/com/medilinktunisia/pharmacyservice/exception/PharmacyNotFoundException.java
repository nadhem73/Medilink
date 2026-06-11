package com.medilinktunisia.pharmacyservice.exception;

public class PharmacyNotFoundException extends RuntimeException {
    public PharmacyNotFoundException(String message) {
        super(message);
    }
}
