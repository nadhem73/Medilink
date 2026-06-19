package com.medilinktunisia.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des routes de l'API Gateway
 * Définit le routage vers tous les microservices
 */
@Configuration
@Slf4j
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ========================================
                // AUTH SERVICE - Service d'authentification
                // ========================================
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .retry(config -> config.setRetries(3)))
                        .uri("lb://AUTH-SERVICE"))

                // ========================================
                // PATIENT SERVICE - Gestion des patients
                // ========================================
                .route("patient-service", r -> r
                        .path("/api/patients/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("patientServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/patient")))
                        .uri("lb://PATIENT-SERVICE"))

                // ========================================
                // DOCTOR SERVICE - Gestion des médecins
                // ========================================
                .route("doctor-service", r -> r
                        .path("/api/doctors/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("doctorServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/doctor")))
                        .uri("lb://DOCTOR-SERVICE"))

                // ========================================
                // APPOINTMENT SERVICE - Rendez-vous
                // ========================================
                .route("appointment-service", r -> r
                        .path("/api/appointments/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("appointmentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/appointment")))
                        .uri("lb://APPOINTMENT-SERVICE"))

                // ========================================
                // PRESCRIPTION SERVICE - Ordonnances
                // ========================================
                .route("prescription-service", r -> r
                        .path("/api/prescriptions/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("prescriptionServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/prescription")))
                        .uri("lb://PRESCRIPTION-SERVICE"))

                // ========================================
                // PHARMACY SERVICE - Pharmacies
                // ========================================
                .route("pharmacy-service", r -> r
                        .path("/api/pharmacies/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("pharmacyServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/pharmacy")))
                        .uri("lb://PHARMACY-SERVICE"))

                // ========================================
                // LABORATORY SERVICE - Laboratoires
                // ========================================
                .route("laboratory-service", r -> r
                        .path("/api/laboratories/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("laboratoryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/laboratory")))
                        .uri("lb://LABORATORY-SERVICE"))

                // ========================================
                // AMBULANCE SERVICE - Ambulances & Urgences
                // ========================================
                .route("ambulance-service", r -> r
                        .path("/api/ambulance/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("ambulanceServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/ambulance")))
                        .uri("lb://AMBULANCE-SERVICE"))

                // ========================================
                // TELECONSULTATION SERVICE - Téléconsultations
                // ========================================
                .route("teleconsultation-service", r -> r
                        .path("/api/teleconsultations/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("teleconsultationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/teleconsultation")))
                        .uri("lb://TELECONSULTATION-SERVICE"))

                // ========================================
                // PAYMENT SERVICE - Paiements
                // ========================================
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment")))
                        .uri("lb://PAYMENT-SERVICE"))

                // ========================================
                // NOTIFICATION SERVICE - Notifications
                // ========================================
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification")))
                        .uri("lb://NOTIFICATION-SERVICE"))

                // ========================================
                // FILE SERVICE - Gestion des fichiers
                // ========================================
                .route("file-service", r -> r
                        .path("/api/files/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("fileServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/file")))
                        .uri("lb://FILE-SERVICE"))

                // ========================================
                // GEOLOCATION SERVICE - Géolocalisation
                // ========================================
                .route("geolocation-service", r -> r
                        .path("/api/geolocation/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("geolocationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/geolocation")))
                        .uri("lb://GEOLOCATION-SERVICE"))

                // ========================================
                // ANALYTICS SERVICE - Analytique
                // ========================================
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("analyticsServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/analytics")))
                        .uri("lb://ANALYTICS-SERVICE"))

                // ========================================
                // AI SERVICE - Intelligence Artificielle
                // ========================================
                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("aiServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/ai")))
                        .uri("lb://AI-SERVICE"))

                // ========================================
                // ADMIN SERVICE - Administration
                // ========================================
                .route("admin-service", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("adminServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/admin")))
                        .uri("lb://ADMIN-SERVICE"))

                .build();
    }
}
