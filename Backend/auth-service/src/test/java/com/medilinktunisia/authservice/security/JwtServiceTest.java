package com.medilinktunisia.authservice.security;

import com.medilinktunisia.authservice.model.entity.Patient;
import com.medilinktunisia.authservice.model.enums.Role;
import com.medilinktunisia.authservice.model.enums.UserStatus;
import io.jsonwebtoken.ExpiredJwtException;
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

    private static final String SECRET = "medilinktunisia2025SecretKeyForJWTTokenGenerationAndValidation";
    private static final long EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION = 604800000L;

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
    void generateAccessToken_shouldReturnValidJwtString() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo("1");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void generateRefreshToken_shouldReturnValidJwtString() {
        Patient user = createTestUser();
        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo("1");
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        Patient user = createTestUser();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", "1")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        Patient user = createTestUser();
        String validToken = jwtService.generateAccessToken(user);
        String tamperedToken = validToken + "tampered";

        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
    }

    @Test
    void isTokenValid_withNullToken_shouldReturnFalse() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
    }

    @Test
    void extractEmail_withExpiredToken_shouldThrow() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("userId", "1")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.extractEmail(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
