package com.example.photogallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @Schema(example = "john_doe")
    @NotBlank
    private String username;

    @Schema(example = "qwerty12345")
    @NotBlank
    @Size(min = 6, message = "Пароль должен быть от 6 символов")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}