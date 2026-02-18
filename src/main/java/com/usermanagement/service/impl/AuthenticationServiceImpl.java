package com.usermanagement.service.impl;

import com.usermanagement.domain.dto.AuthResponse;
import com.usermanagement.domain.dto.LoginRequest;
import com.usermanagement.domain.entity.RefreshToken;
import com.usermanagement.domain.entity.User;
import com.usermanagement.exception.UnauthorizedException;
import com.usermanagement.mapper.UserMapper;
import com.usermanagement.repository.RefreshTokenRepository;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.AuthenticationService;
import com.usermanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AuthenticationService.
 * Handles authentication, token generation, and refresh logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            User user = userService.getUserEntityByUsernameOrEmail(request.getUsernameOrEmail());

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshToken)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusSeconds(
                            jwtTokenProvider.getRefreshTokenExpirationMs() / 1000))
                    .ipAddress(getClientIp(httpRequest))
                    .userAgent(httpRequest.getHeader("User-Agent"))
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);

            userService.updateLastLogin(user.getId());
            userService.resetFailedLoginAttempts(request.getUsernameOrEmail());

            log.info("User authenticated successfully: {}", request.getUsernameOrEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                    .issuedAt(LocalDateTime.now())
                    .user(userMapper.toResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsernameOrEmail(), e);
            userService.incrementFailedLoginAttempts(request.getUsernameOrEmail());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String token) {
        log.debug("Refreshing access token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getIsRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = refreshToken.getUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, null);

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        log.info("Access token refreshed for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .issuedAt(LocalDateTime.now())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.debug("Logging out user");

        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("User logged out successfully");
        });

        SecurityContextHolder.clearContext();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
