package com.example.photogallery.controller;

import com.example.photogallery.dto.LoginRequest;
import com.example.photogallery.dto.RefreshTokenRequest;
import com.example.photogallery.dto.RegisterRequest;
import com.example.photogallery.dto.TokenResponse;
import com.example.photogallery.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "JWT Аутентификация (Лабораторная 4)", description = "Управление токенами и сессиями устройств (Вариант 7)")
public class JwtAuthController {

    private final AuthService authService;

    public JwtAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "1. Регистрация пользователя", description = "Создает нового пользователя с bcrypt-хешем пароля")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered");
    }

    @Operation(summary = "2. Вход (Login)", description = "Принимает логин, пароль и ID устройства. Возвращает пару Access и Refresh токенов")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "3. Обновление токенов (Refresh)", description = "Выдает новую пару токенов, деактивируя предыдущий access и refresh токен устройства")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request));
    }

    @Operation(summary = "4. Выход (Logout)", description = "Удаляет сохраненный SHA-256 хеш refresh-токена конкретного устройства из базы данных")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Successfully logged out from device. Session terminated.");
    }

    @Operation(
            summary = "5. Защищенный тестовый маршрут",
            description = "Маршрут доступен только при наличии действующего Access-токена в заголовке Authorization: Bearer <token>",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok("Access granted! Authenticated user: " + username);
    }
}