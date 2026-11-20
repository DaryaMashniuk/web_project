package com.example.photogallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @Schema(example = "john_doe")
    @NotBlank
    private String username;

    @Schema(example = "qwerty12345")
    @NotBlank
    private String password;

    @Schema(description = "Уникальный ID устройства", example = "laptop-work-mac")
    @NotBlank
    private String deviceId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}