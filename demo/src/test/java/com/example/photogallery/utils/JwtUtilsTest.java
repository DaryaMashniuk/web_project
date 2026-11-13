package com.example.photogallery.security;

import com.example.photogallery.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        String secret = "test-super-secret-key-that-is-at-least-256-bits-long-hs256!";
        jwtUtils = new JwtUtils(secret, 60000, 600000);
    }

    @Test
    @DisplayName("Unit-тест: корректная генерация и парсинг токена для конкретного устройства")
    void generateAndValidateAccessToken() {
        String token = jwtUtils.generateAccessToken(1L, "photographer_pro", Set.of("ROLE_USER"), "macbook-m1");

        assertNotNull(token);
        Claims claims = jwtUtils.parseAndValidateClaims(token);

        assertEquals("photographer_pro", claims.getSubject());
        assertEquals("access", claims.get("type", String.class));
        assertEquals("macbook-m1", claims.get("deviceId", String.class));
    }

    @Test
    @DisplayName("Unit-тест: хеширование refresh-токена в SHA-256")
    void hashTokenSha256() {
        String rawToken = "sample-refresh-token-12345";
        String hash1 = jwtUtils.hashTokenSha256(rawToken);
        String hash2 = jwtUtils.hashTokenSha256(rawToken);

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Хеши одинаковых токенов должны совпадать");
        assertNotEquals(rawToken, hash1, "В базе не должен храниться открытый токен");
    }
}