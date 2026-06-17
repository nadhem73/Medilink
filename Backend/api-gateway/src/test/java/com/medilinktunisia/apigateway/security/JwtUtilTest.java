package com.medilinktunisia.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String SECRET = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    private String createToken(String subject, List<String> roles, long expirationMillis) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .claim("userId", "1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = createToken("user@test.com", List.of("ROLE_DOCTOR"), 3600000);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        String token = createToken("user@test.com", List.of("ROLE_DOCTOR"), -1000);
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void extractUsername_returnsUsername() {
        String token = createToken("user@test.com", List.of("ROLE_DOCTOR"), 3600000);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractRoles_returnsRolesList() {
        String token = createToken("user@test.com", List.of("ROLE_DOCTOR", "ROLE_ADMIN"), 3600000);
        assertThat(jwtUtil.extractRoles(token)).containsExactly("ROLE_DOCTOR", "ROLE_ADMIN");
    }
}
