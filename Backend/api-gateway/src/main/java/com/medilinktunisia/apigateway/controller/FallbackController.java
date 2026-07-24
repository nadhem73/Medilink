package com.medilinktunisia.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class FallbackController {

    @RequestMapping("/{service}")
    public ResponseEntity<Map<String, Object>> serviceFallback(@PathVariable String service) {
        log.warn("Fallback invoked for service: {}", service);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", String.format("Le service %s est temporairement indisponible. Veuillez réessayer plus tard.", service));
        response.put("service", service);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.warn("Fallback invoked for Authentication Service");
        return createFallbackResponse("Authentication Service",
                "Le service d'authentification est temporairement indisponible.");
    }

    @RequestMapping("/patient")
    public ResponseEntity<Map<String, Object>> patientServiceFallback() {
        log.warn("Fallback invoked for Patient Service");
        return createFallbackResponse("Patient Service",
                "Le service patients est temporairement indisponible.");
    }

    @RequestMapping("/doctor")
    public ResponseEntity<Map<String, Object>> doctorServiceFallback() {
        log.warn("Fallback invoked for Doctor Service");
        return createFallbackResponse("Doctor Service",
                "Le service médecins est temporairement indisponible.");
    }

    @RequestMapping("/appointment")
    public ResponseEntity<Map<String, Object>> appointmentServiceFallback() {
        log.warn("Fallback invoked for Appointment Service");
        return createFallbackResponse("Appointment Service",
                "Le service de rendez-vous est temporairement indisponible.");
    }

    @RequestMapping("/bilan")
    public ResponseEntity<Map<String, Object>> bilanServiceFallback() {
        log.warn("Fallback invoked for Bilan Service");
        return createFallbackResponse("Bilan Service",
                "Le service de scan de bilans est temporairement indisponible.");
    }

    @RequestMapping("/monitoring")
    public ResponseEntity<Map<String, Object>> monitoringServiceFallback() {
        log.warn("Fallback invoked for Monitoring Service");
        return createFallbackResponse("Monitoring Service",
                "Le service de monitoring est temporairement indisponible. Les métriques ne peuvent pas être actualisées.");
    }

    @RequestMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        log.warn("Fallback invoked for Payment Service");
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
