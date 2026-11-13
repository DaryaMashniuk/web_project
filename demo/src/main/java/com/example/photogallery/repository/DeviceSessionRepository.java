package com.example.photogallery.repository;

import com.example.photogallery.entity.DeviceSession;
import com.example.photogallery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    Optional<DeviceSession> findByUserAndDeviceId(User user, String deviceId);
    Optional<DeviceSession> findByTokenHash(String tokenHash);
    void deleteByUserAndDeviceId(User user, String deviceId);
}