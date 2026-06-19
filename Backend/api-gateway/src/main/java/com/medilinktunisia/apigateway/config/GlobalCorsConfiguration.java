package com.medilinktunisia.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration CORS déplacée dans SecurityConfig.java (via CorsConfigurationSource)
 * pour une meilleure intégration avec Spring Security WebFlux.
 * 
 * Ce fichier est conservé vide pour éviter les conflits de bean CorsWebFilter.
 * 
 * @see com.medilinktunisia.apigateway.security.SecurityConfig#corsConfigurationSource()
 */
@Configuration
@Slf4j
public class GlobalCorsConfiguration {
    // CORS géré par SecurityConfig

    public GlobalCorsConfiguration() {
        log.debug("GlobalCorsConfiguration initialized - CORS is managed by SecurityConfig");
    }
}
