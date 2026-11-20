package com.example.photogallery.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_sessions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "device_id"})
})
public class DeviceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "session_id", nullable = false)
    private String sessionId; // <-- Уникальный ID текущего access-токена устройства

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    public DeviceSession() {}

    public DeviceSession(User user, String deviceId, String sessionId, String tokenHash, Instant expiryDate) {
        this.user = user;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.expiryDate = expiryDate;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}