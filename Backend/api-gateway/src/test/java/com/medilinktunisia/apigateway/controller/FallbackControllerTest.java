package com.medilinktunisia.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.medilinktunisia.apigateway.security.JwtUtil;

@WebFluxTest(controllers = FallbackController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@ActiveProfiles("test")
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void authFallback_returns503() {
        webTestClient.get().uri("/fallback/auth")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.service").isEqualTo("Authentication Service")
                .jsonPath("$.message").isEqualTo("Le service d'authentification est temporairement indisponible.");
    }

    @Test
    void patientFallback_returns503() {
        webTestClient.get().uri("/fallback/patient")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.service").isEqualTo("Patient Service");
    }
}
