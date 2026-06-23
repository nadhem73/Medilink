package com.medilinktunisia.authservice.security;

import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import io.jsonwebtoken.Claims;
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

    private static final String SECRET = "test-secret-key-for-unit-testing-only-very-long-and-secure";
    private static final long EXPIRATION = 3600000;
    private static final long REFRESH_EXPIRATION = 86400000;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION, REFRESH_EXPIRATION);
    }

    private Patient createTestUser() {
        Patient user = new Patient();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    @Test
    void generateAccessToken_returnsValidToken() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotNull();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo("1");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void generateRefreshToken_returnsValidToken() {
        Patient user = createTestUser();
        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotNull();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo("1");
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", 1L)
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);
        String tampered = token + "tampered";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_nullToken_returnsFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void extractEmail_expiredToken_throws() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", 1L)
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.extractEmail(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
