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
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "medilinktunisia2025SecretKeyForJWTTokenGenerationAndValidation";
    private static final long EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION = 604800000L;

    private JwtService jwtService;
    private Patient testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION, REFRESH_EXPIRATION);

        testUser = new Patient();
        testUser.setId(1L);
        testUser.setEmail("john.doe@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.PATIENT);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void generateAccessToken_shouldReturnValidJwtString() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateRefreshToken_shouldReturnValidJwtString() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(testUser);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("john.doe@example.com");
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateAccessToken(testUser);

        String userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo("1");
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("john.doe@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(key)
                .compact();

        boolean valid = jwtService.isTokenValid(expiredToken);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        String validToken = jwtService.generateAccessToken(testUser);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        boolean valid = jwtService.isTokenValid(tamperedToken);

        assertThat(valid).isFalse();
    }
}
