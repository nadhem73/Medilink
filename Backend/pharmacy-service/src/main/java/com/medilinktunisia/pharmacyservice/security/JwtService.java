package com.medilinktunisia.pharmacyservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Validation des tokens JWT émis par l'auth-service.
 * Utilise le même secret partagé pour vérifier la signature.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractEmail(String token) {
        String email = extractClaim(token, Claims::getSubject);
        log.debug("Extracted email from token: {}", email);
        return email;
    }

    public Long extractUserId(String token) {
        Object userId = extractClaim(token, claims -> claims.get("userId"));
        Long id = userId == null ? null : Long.valueOf(userId.toString());
        log.debug("Extracted userId from token: {}", id);
        return id;
    }

    public boolean isTokenValid(String token) {
        try {
            boolean valid = !extractClaim(token, Claims::getExpiration).before(new Date());
            log.debug("Token validation result: {}", valid);
            return valid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
