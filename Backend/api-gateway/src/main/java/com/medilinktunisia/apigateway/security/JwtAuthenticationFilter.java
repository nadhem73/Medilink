package com.medilinktunisia.apigateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtre d'authentification JWT pour l'API Gateway
 * Valide les tokens JWT et configure le contexte de sécurité
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String path = request.getPath().value();
        
        // Skip JWT validation pour les endpoints publics
        if (isPublicPath(path)) {
            log.debug("Skipping JWT validation for public path: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String userId = jwtUtil.extractUserId(token);
                    List<String> roles = jwtUtil.extractRoles(token);

                    log.debug("Authenticated user: {} with roles: {} for path: {}", username, roles, path);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Ajouter les informations utilisateur aux headers pour les microservices
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Username", username)
                            .header("X-User-Roles", String.join(",", roles))
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                } else {
                    log.warn("Invalid token provided for path: {}", path);
                }
            } catch (Exception e) {
                log.error("JWT authentication error for path {}: {}", path, e.getMessage());
                // Token invalide, continuer sans authentification
                return chain.filter(exchange);
            }
        } else {
            log.debug("No Bearer token found in request for path: {}", path);
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/auth/forgot-password") ||
                path.startsWith("/api/auth/reset-password") ||
                path.startsWith("/api/auth/verify-email") ||
                path.startsWith("/actuator") ||
                path.startsWith("/fallback");
    }
}
