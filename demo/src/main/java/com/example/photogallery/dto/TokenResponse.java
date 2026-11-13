package com.example.photogallery.dto;

public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String deviceId;

    public TokenResponse(String accessToken, String refreshToken, String deviceId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public String getDeviceId() { return deviceId; }
}