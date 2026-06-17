package com.medilinktunisia.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.medilinktunisia.apigateway.security.JwtUtil;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GatewayController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@ActiveProfiles("test")
class GatewayControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DiscoveryClient discoveryClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void home_returnsAppInfo() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.application").isEqualTo("Smart Health Tunisia API Gateway")
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void health_returnsStatus() {
        webTestClient.get().uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.gateway").isEqualTo("healthy");
    }

    @Test
    void getRegisteredServices_returnsServices() {
        when(discoveryClient.getServices()).thenReturn(List.of("auth-service", "doctor-service"));
        when(discoveryClient.getInstances("auth-service")).thenReturn(List.of());
        when(discoveryClient.getInstances("doctor-service")).thenReturn(List.of());

        webTestClient.get().uri("/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.total").isEqualTo(2)
                .jsonPath("$.services[0]").isEqualTo("auth-service");
    }
}
