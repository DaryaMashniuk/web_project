package com.example.photogallery.service;

import com.example.photogallery.dto.LoginRequest;
import com.example.photogallery.dto.RefreshTokenRequest;
import com.example.photogallery.dto.RegisterRequest;
import com.example.photogallery.dto.TokenResponse;
import com.example.photogallery.entity.DeviceSession;
import com.example.photogallery.entity.User;
import com.example.photogallery.repository.DeviceSessionRepository;
import com.example.photogallery.repository.UserRepository;
import com.example.photogallery.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       DeviceSessionRepository deviceSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.deviceSessionRepository = deviceSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()), // BCrypt
                Set.of("ROLE_USER")
        );
        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return createSessionAndGenerateTokens(user, request.getDeviceId());
    }

    @Transactional
    public TokenResponse refreshTokens(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        String hash = jwtUtils.hashTokenSha256(rawToken);

        DeviceSession session = deviceSessionRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or reused refresh token"));

        if (session.getExpiryDate().isBefore(Instant.now())) {
            deviceSessionRepository.delete(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        User user = session.getUser();
        String deviceId = session.getDeviceId();

        return createSessionAndGenerateTokens(user, deviceId);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = jwtUtils.hashTokenSha256(rawRefreshToken);
        deviceSessionRepository.findByTokenHash(hash).ifPresent(deviceSessionRepository::delete);
    }

    private TokenResponse createSessionAndGenerateTokens(User user, String deviceId) {
        String rawRefreshToken = jwtUtils.generateRawRefreshToken();
        String tokenHash = jwtUtils.hashTokenSha256(rawRefreshToken);
        Instant expiryDate = Instant.now().plusMillis(jwtUtils.getRefreshExpirationMs());

        DeviceSession session = deviceSessionRepository.findByUserAndDeviceId(user, deviceId)
                .orElseGet(() -> new DeviceSession(user, deviceId, tokenHash, expiryDate));

        session.setTokenHash(tokenHash);
        session.setExpiryDate(expiryDate);
        deviceSessionRepository.save(session);

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRoles(), deviceId);
        return new TokenResponse(accessToken, rawRefreshToken, deviceId);
    }
}