package com.example.photogallery.controller;

import com.example.photogallery.dto.*;
import com.example.photogallery.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "JWT Аутентификация (Лабораторная 4)", description = "Регистрация, вход, ротация токенов для устройств")
public class JwtAuthController {

    private final AuthService authService;

    public JwtAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered");
    }

    @Operation(summary = "Вход пользователя (генерация пары токенов для устройства)")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Обновление токенов по действующему Refresh-токену")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshTokens(request));
    }

    @Operation(summary = "Выход (инвалидация refresh-токена конкретного устройства)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Защищенный маршрут с действующим Access-токеном")
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok("Hello from protected area, " + username + "! Your access-token is valid.");
    }
}