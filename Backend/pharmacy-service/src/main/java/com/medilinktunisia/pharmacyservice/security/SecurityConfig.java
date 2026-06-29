package com.medilinktunisia.pharmacyservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Loading SecurityConfig - configuring HTTP security filter chain");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permettre les requetes OPTIONS (CORS preflight) sans authentification
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoint interne appele par l'auth-service (service-a-service)
                        .requestMatchers("/pharmacy-profiles/internal/**").permitAll()
                        // Liste des profils pharmacies (accessible aux utilisateurs authentifies)
                        .requestMatchers("/pharmacy-profiles/all").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Le reste (ex. /pharmacy-profiles/me) necessite un JWT valide
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
