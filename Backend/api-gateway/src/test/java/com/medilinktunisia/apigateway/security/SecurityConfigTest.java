package com.medilinktunisia.apigateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void contextLoads() {
        assertThat(securityWebFilterChain).isNotNull();
        assertThat(corsConfigurationSource).isNotNull();
    }
}
