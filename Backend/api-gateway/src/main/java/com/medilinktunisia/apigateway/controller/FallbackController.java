package com.medilinktunisia.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de fallback pour les services indisponibles
 * Retourne des réponses appropriées quand un microservice est down
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/{service}")
    public ResponseEntity<Map<String, Object>> serviceFallback(@PathVariable String service) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", String.format("Le service %s est temporairement indisponible. Veuillez réessayer plus tard.", service));
        response.put("service", service);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return createFallbackResponse("Authentication Service", 
                "Le service d'authentification est temporairement indisponible.");
    }

    @GetMapping("/patient")
    public ResponseEntity<Map<String, Object>> patientServiceFallback() {
        return createFallbackResponse("Patient Service", 
                "Le service patients est temporairement indisponible.");
    }

    @GetMapping("/doctor")
    public ResponseEntity<Map<String, Object>> doctorServiceFallback() {
        return createFallbackResponse("Doctor Service", 
                "Le service médecins est temporairement indisponible.");
    }

    @GetMapping("/appointment")
    public ResponseEntity<Map<String, Object>> appointmentServiceFallback() {
        return createFallbackResponse("Appointment Service", 
                "Le service de rendez-vous est temporairement indisponible.");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse("Payment Service", 
                "Le service de paiement est temporairement indisponible. Vos données sont sécurisées.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("service", serviceName);
        response.put("suggestion", "Veuillez réessayer dans quelques instants ou contacter le support si le problème persiste.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
