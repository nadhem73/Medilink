package com.medilinktunisia.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre qui extrait et valide le JWT du header Authorization,
 * puis place l'utilisateur authentifié dans le SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String path = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for request: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        log.debug("Processing JWT authentication for request: {}", path);
        try {
            final String email = jwtService.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Attempting to authenticate user: {}", email);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authenticated successfully: {}", email);
                } else {
                    log.warn("Invalid JWT token for user: {}", email);
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed for request {}: {}", path, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
