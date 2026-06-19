package com.medilinktunisia.apigateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour l'API Gateway
 * Gère l'authentification JWT, les autorisations ET le CORS
 */
@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Requêtes OPTIONS (CORS preflight) autorisées sans authentification
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints publics
                        .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        .pathMatchers("/api/auth/verify-email/**").permitAll()

                        // Actuator et monitoring
                        .pathMatchers("/actuator/**").permitAll()

                        // Fallback endpoints
                        .pathMatchers("/fallback/**").permitAll()

                        // Recherche publique de médecins et pharmacies
                        .pathMatchers(HttpMethod.GET, "/api/doctors/search").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/doctors/*/public").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/pharmacies/search").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/pharmacies/nearby").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/laboratories/search").permitAll()

                        // Géolocalisation publique
                        .pathMatchers(HttpMethod.GET, "/api/geolocation/nearby/**").permitAll()

                        // Endpoints admin
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")

                        // Analytics
                        .pathMatchers("/api/analytics/**").hasAnyRole("ADMIN", "DOCTOR")

                        // Ambulances
                        .pathMatchers("/api/ambulances/**").authenticated()

                        // Tous les autres endpoints nécessitent une authentification
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "https://medilinktunisia.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
