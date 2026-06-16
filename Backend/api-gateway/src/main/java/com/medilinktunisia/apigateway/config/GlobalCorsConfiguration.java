package com.medilinktunisia.apigateway.config;

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
public class GlobalCorsConfiguration {
    // CORS géré par SecurityConfig
}
