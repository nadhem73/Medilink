package com.medilinktunisia.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration de sécurité pour l'API Gateway
 * Gère l'authentification JWT et les autorisations
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)  // CORS géré par GlobalCorsConfiguration
                .authorizeExchange(exchanges -> exchanges
                        // ✅ IMPORTANT: Autoriser les requêtes OPTIONS (CORS preflight) sans authentification
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Endpoints publics - Pas d'authentification requise
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
                        
                        // Endpoints admin - Réservés aux administrateurs
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Analytics - Admin et médecins
                        .pathMatchers("/api/analytics/**").hasAnyRole("ADMIN", "DOCTOR")
                        
                        // Ambulances - Tous les utilisateurs authentifiés
                        .pathMatchers("/api/ambulances/**").authenticated()
                        
                        // Tous les autres endpoints nécessitent une authentification
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
