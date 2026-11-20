package com.example.photogallery.utils;

import com.example.photogallery.entity.DeviceSession;
import com.example.photogallery.entity.User;
import com.example.photogallery.repository.DeviceSessionRepository;
import com.example.photogallery.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final DeviceSessionRepository deviceSessionRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   UserRepository userRepository,
                                   DeviceSessionRepository deviceSessionRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.deviceSessionRepository = deviceSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtils.parseAndValidateClaims(token);

                String tokenType = claims.get("type", String.class);
                if (!"access".equals(tokenType)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN_TYPE");
                    return;
                }

                String username = claims.getSubject();
                String deviceId = claims.get("deviceId", String.class);
                String tokenSessionId = claims.get("sessionId", String.class);

                if (deviceId != null && tokenSessionId != null) {
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "USER_NOT_FOUND");
                        return;
                    }

                    Optional<DeviceSession> sessionOpt = deviceSessionRepository.findByUserAndDeviceId(userOpt.get(), deviceId);

                    if (sessionOpt.isEmpty() || !tokenSessionId.equals(sessionOpt.get().getSessionId())) {
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_REVOKED");
                        return;
                    }
                }

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
                        roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String error) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + error + "\"}");
    }
}