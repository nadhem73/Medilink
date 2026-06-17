package com.medilinktunisia.patientservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "medilinktunisia2025SecretKeyForJWTTokenGenerationAndValidation";

    private JwtService jwtService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(Map<String, Object> claims, long expiresInMillis) {
        return Jwts.builder()
                .claims(claims)
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiresInMillis))
                .signWith(key)
                .compact();
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token = generateToken(Map.of("userId", 1L), 3600000);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractUserId_shouldReturnUserId() {
        String token = generateToken(Map.of("userId", 42L), 3600000);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = generateToken(Map.of("userId", 1L), 3600000);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        String token = generateToken(Map.of("userId", 1L), -1000);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        String token = generateToken(Map.of("userId", 1L), 3600000);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_withNullToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void isTokenValid_withEmptyToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
