package com.example.photogallery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Tag(name = "Авторизация (OAuth2)", description = "Вход через Google, GitHub и GitLab")
public class AuthController {

    @Operation(summary = "Главная страница", description = "Показывает статус авторизации пользователя")
    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "You are not logged in!";
        }
        return "You are logged in as " + extractUserName(principal);
    }

    @Operation(summary = "Вход через Google", description = "Перенаправляет на страницу авторизации Google (открывать в браузере)")
    @GetMapping("/google")
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @Operation(summary = "Вход через GitHub", description = "Перенаправляет на страницу авторизации GitHub (открывать в браузере)")
    @GetMapping("/github")
    public void loginWithGithub(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github");
    }

    @Operation(summary = "Вход через GitLab", description = "Перенаправляет на страницу авторизации GitLab (открывать в браузере)")
    @GetMapping("/gitlab")
    public void loginWithGitlab(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/gitlab");
    }

    @Operation(summary = "Защищенная страница после входа", description = "Доступна только авторизованным пользователям")
    @GetMapping("/good")
    public String good(@AuthenticationPrincipal OAuth2User principal) {
        return "Welcome mr " + extractUserName(principal);
    }

    @Operation(summary = "Выход из системы (OAuth2 / Сессия браузера)",
            description = "Очищает cookies сессии JSESSIONID и сбрасывает авторизацию")
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "You have been logged out successfully!";
    }

    private String extractUserName(OAuth2User principal) {
        String name = principal.getAttribute("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        String username = principal.getAttribute("username");
        if (username != null && !username.isBlank()) {
            return username;
        }
        String login = principal.getAttribute("login");
        if (login != null && !login.isBlank()) {
            return login;
        }
        return principal.getName();
    }
}