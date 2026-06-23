package com.medilinktunisia.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmctb25seS12ZXJ5LWxvbmctYW5kLXNlY3VyZQ==";
    private static final long EXPIRATION = 3600000L;

    private JwtUtil jwtUtil;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(String username, String userId, List<String> roles, long expirationMs) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void extractUsername_ShouldReturnSubject() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), EXPIRATION);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void extractUserId_ShouldReturnUserIdClaim() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), EXPIRATION);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user123");
    }

    @Test
    void extractRoles_ShouldReturnRolesList() {
        String token = createToken("testuser", "user123", List.of("DOCTOR", "ADMIN"), EXPIRATION);
        assertThat(jwtUtil.extractRoles(token)).containsExactly("DOCTOR", "ADMIN");
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), EXPIRATION);
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), -1000);
        assertThat(jwtUtil.isTokenExpired(token)).isTrue();
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), EXPIRATION);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), -1000);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_WithTamperedToken_ShouldReturnFalse() {
        String token = createToken("testuser", "user123", List.of("DOCTOR"), EXPIRATION);
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".tampered";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        assertThat(jwtUtil.validateToken(null)).isFalse();
    }
}
