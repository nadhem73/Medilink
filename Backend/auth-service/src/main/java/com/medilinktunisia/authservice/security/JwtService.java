package com.medilinktunisia.authservice.security;

import com.medilinktunisia.authservice.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Génération et validation des tokens JWT.
 * Le sujet du token est l'email ; les claims contiennent userId et role.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(User user) {
        log.info("Generating access token for user ID: {}, email: {}", user.getId(), user.getEmail());
        String token = buildToken(user, expiration);
        log.debug("Access token generated for user ID: {}", user.getId());
        return token;
    }

    public String generateRefreshToken(User user) {
        log.info("Generating refresh token for user ID: {}, email: {}", user.getId(), user.getEmail());
        String token = buildToken(user, refreshExpiration);
        log.debug("Refresh token generated for user ID: {}", user.getId());
        return token;
    }

    public long getExpiration() {
        return expiration;
    }

    private String buildToken(User user, long ttl) {
        Date now = new Date();
        return Jwts.builder()
                // Claims compatibles avec l'API Gateway : userId (String) et roles (liste)
                .claims(Map.of(
                        "userId", String.valueOf(user.getId()),
                        "role", user.getRole().name(),
                        "roles", List.of(user.getRole().name())))
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String email) {
        try {
            boolean valid = extractEmail(token).equals(email) && !isExpired(token);
            log.debug("Token validation for email {}: {}", email, valid);
            return valid;
        } catch (Exception e) {
            log.warn("Token validation failed for email {}: {}", email, e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            boolean valid = !isExpired(token);
            log.debug("Token validity check result: {}", valid);
            return valid;
        } catch (Exception e) {
            log.warn("Token validity check failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
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
