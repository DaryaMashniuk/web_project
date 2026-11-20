package com.example.photogallery.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        String secret = "test-super-secret-key-that-is-at-least-256-bits-long-hs256!";
        jwtUtils = new JwtUtils(secret, 60000, 600000);
    }

    @Test
    @DisplayName("Unit-тест: генерация Access-токена со всеми полями ТЗ (Вариант 7)")
    void generateAndValidateAccessToken() {
        String testSessionId = UUID.randomUUID().toString();

        String token = jwtUtils.generateAccessToken(
                10L,
                "alex_photographer",
                Set.of("ROLE_USER"),
                "macbook-pro-16",
                testSessionId
        );

        assertNotNull(token);
        Claims claims = jwtUtils.parseAndValidateClaims(token);

        assertEquals("alex_photographer", claims.getSubject());
        assertEquals("access", claims.get("type", String.class));
        assertEquals("macbook-pro-16", claims.get("deviceId", String.class));
        assertEquals(testSessionId, claims.get("sessionId", String.class));
    }

    @Test
    @DisplayName("Unit-тест: хеширование refresh-токена алгоритмом SHA-256")
    void hashTokenSha256() {
        String rawToken = "secure-random-refresh-token-value";
        String hash = jwtUtils.hashTokenSha256(rawToken);

        assertNotNull(hash);
        assertNotEquals(rawToken, hash);
        assertEquals(hash, jwtUtils.hashTokenSha256(rawToken), "Хеши одинаковых токенов должны совпадать");
    }
}