package com.medilinktunisia.doctorservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-testing-only-must-be-at-least-256-bits-long";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token = createToken("doctor@test.com", 1L, 3600000);
        assertThat(jwtService.extractEmail(token)).isEqualTo("doctor@test.com");
    }

    @Test
    void extractUserId_shouldReturnUserId() {
        String token = createToken("doctor@test.com", 42L, 3600000);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractUserId_withNullClaim_shouldReturnNull() {
        String token = Jwts.builder()
                .subject("doctor@test.com")
                .signWith(KEY)
                .compact();
        assertThat(jwtService.extractUserId(token)).isNull();
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = createToken("doctor@test.com", 1L, 3600000);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        String token = createToken("doctor@test.com", 1L, -1000);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        String token = createToken("doctor@test.com", 1L, 3600000);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";
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

    private String createToken(String email, Long userId, long expirationMs) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(KEY)
                .compact();
    }
}
