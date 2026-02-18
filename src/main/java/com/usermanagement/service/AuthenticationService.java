package com.usermanagement.service;

import com.usermanagement.domain.dto.AuthResponse;
import com.usermanagement.domain.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthenticationService {

    /**
     * Authenticate user and generate tokens.
     */
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refresh access token using refresh token.
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Logout user and revoke tokens.
     */
    void logout(String refreshToken);
}
