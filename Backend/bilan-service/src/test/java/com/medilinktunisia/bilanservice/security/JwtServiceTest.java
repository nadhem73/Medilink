package com.medilinktunisia.bilanservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "medilinktunisia2025SecretKeyForJWTTokenGenerationAndValidation";
    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        byte[] keyBytes = Base64.getEncoder().encodeToString(SECRET.getBytes()).getBytes();
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(SECRET.getBytes())));
    }

    @Test
    void extractUserId_shouldReturnUserIdFromValidToken() {
        String token = Jwts.builder()
                .claim("userId", "123")
                .subject("123")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        String userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo("123");
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = Jwts.builder()
                .claim("userId", "123")
                .subject("123")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        String token = Jwts.builder()
                .claim("userId", "123")
                .subject("123")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(signingKey)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("invalid-token")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForEmptyToken() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
