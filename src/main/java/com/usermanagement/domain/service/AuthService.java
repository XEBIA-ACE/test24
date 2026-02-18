package com.usermanagement.domain.service;

import com.usermanagement.api.dto.request.LoginRequest;
import com.usermanagement.api.dto.request.RefreshTokenRequest;
import com.usermanagement.api.dto.response.AuthResponse;
import com.usermanagement.api.mapper.UserMapper;
import com.usermanagement.domain.entity.RefreshToken;
import com.usermanagement.domain.entity.User;
import com.usermanagement.domain.repository.RefreshTokenRepository;
import com.usermanagement.domain.repository.UserRepository;
import com.usermanagement.exception.InvalidTokenException;
import com.usermanagement.exception.ResourceNotFoundException;
import com.usermanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (user.isAccountLocked()) {
            throw new BadCredentialsException("Account is locked due to multiple failed login attempts");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Reset failed login attempts on successful login
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("Login successful for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request received");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        // Revoke old refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional
    public void logout(String token) {
        log.info("Logout request received");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Logout successful");
    }

    @Transactional
    public void logoutAll(UUID userId) {
        log.info("Logout all devices for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllTokensForUser(user, LocalDateTime.now());

        log.info("All tokens revoked for user: {}", user.getUsername());
    }

    private String createRefreshToken(User user) {
        String token = jwtUtil.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMillis(jwtUtil.getRefreshTokenExpiration()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired and revoked tokens");
        int deletedCount = refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Deleted {} expired/revoked tokens", deletedCount);
    }
}
