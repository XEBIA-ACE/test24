package com.usermanagement.service;

import com.usermanagement.domain.dto.UserRegistrationRequest;
import com.usermanagement.domain.dto.UserResponse;
import com.usermanagement.domain.dto.UserUpdateRequest;
import com.usermanagement.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for user management operations.
 */
public interface UserService {

    /**
     * Register a new user.
     */
    UserResponse registerUser(UserRegistrationRequest request);

    /**
     * Get user by ID.
     */
    UserResponse getUserById(UUID id);

    /**
     * Get user entity by username or email.
     */
    User getUserEntityByUsernameOrEmail(String usernameOrEmail);

    /**
     * Get all users with pagination.
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Update user information.
     */
    UserResponse updateUser(UUID id, UserUpdateRequest request);

    /**
     * Delete user by ID.
     */
    void deleteUser(UUID id);

    /**
     * Enable user account.
     */
    UserResponse enableUser(UUID id);

    /**
     * Disable user account.
     */
    UserResponse disableUser(UUID id);

    /**
     * Lock user account.
     */
    UserResponse lockUser(UUID id);

    /**
     * Unlock user account.
     */
    UserResponse unlockUser(UUID id);

    /**
     * Update last login timestamp.
     */
    void updateLastLogin(UUID userId);

    /**
     * Increment failed login attempts.
     */
    void incrementFailedLoginAttempts(String usernameOrEmail);

    /**
     * Reset failed login attempts.
     */
    void resetFailedLoginAttempts(String usernameOrEmail);
}
