package com.medilinktunisia.doctorservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    private String createToken(String subject, Long userId, long expirationMillis) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    @Test
    void extractEmail_validToken_returnsEmail() {
        String token = createToken("test@example.com", 1L, 3600000);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractUserId_validToken_returnsUserId() {
        String token = createToken("test@example.com", 42L, 3600000);
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = createToken("test@example.com", 1L, 3600000);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String token = createToken("test@example.com", 1L, -1000);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = createToken("test@example.com", 1L, 3600000);
        String tampered = token + "tampered";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void extractEmail_expiredToken_throwsExpiredJwtException() {
        String token = createToken("test@example.com", 1L, -1000);
        assertThatThrownBy(() -> jwtService.extractEmail(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void extractUserId_expiredToken_throwsExpiredJwtException() {
        String token = createToken("test@example.com", 42L, -1000);
        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void isTokenValid_nullToken_returnsFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void isTokenValid_emptyToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_differentSecret_returnsFalse() {
        String differentSecret = "differentDifferentDifferentDifferentDifferentDifferentDifferent!";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void extractUserId_missingClaim_returnsNull() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
        assertThat(jwtService.extractUserId(token)).isNull();
    }
}
