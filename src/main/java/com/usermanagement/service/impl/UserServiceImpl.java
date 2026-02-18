package com.usermanagement.service.impl;

import com.usermanagement.domain.dto.UserRegistrationRequest;
import com.usermanagement.domain.dto.UserResponse;
import com.usermanagement.domain.dto.UserUpdateRequest;
import com.usermanagement.domain.entity.Role;
import com.usermanagement.domain.entity.User;
import com.usermanagement.domain.enums.RoleType;
import com.usermanagement.exception.BadRequestException;
import com.usermanagement.exception.ResourceNotFoundException;
import com.usermanagement.mapper.UserMapper;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.CacheService;
import com.usermanagement.service.KafkaProducerService;
import com.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of UserService.
 * Handles user management operations with caching and event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    private final KafkaProducerService kafkaProducerService;

    private static final String USER_CACHE_KEY_PREFIX = "user:";
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleType.ROLE_USER));
        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        UserResponse response = userMapper.toResponse(savedUser);
        cacheService.set(USER_CACHE_KEY_PREFIX + savedUser.getId(), response);

        publishUserEvent("USER_REGISTERED", savedUser);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);

        UserResponse cachedUser = cacheService.get(USER_CACHE_KEY_PREFIX + id, UserResponse.class);
        if (cachedUser != null) {
            log.debug("User found in cache: {}", id);
            return cachedUser;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserResponse response = userMapper.toResponse(user);
        cacheService.set(USER_CACHE_KEY_PREFIX + id, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByUsernameOrEmail(String usernameOrEmail) {
        log.debug("Fetching user by username or email: {}", usernameOrEmail);
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", usernameOrEmail));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        UserResponse response = userMapper.toResponse(updatedUser);
        cacheService.set(USER_CACHE_KEY_PREFIX + id, response);

        publishUserEvent("USER_UPDATED", updatedUser);

        return response;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
        cacheService.delete(USER_CACHE_KEY_PREFIX + id);

        publishUserEvent("USER_DELETED", user);

        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public UserResponse enableUser(UUID id) {
        log.info("Enabling user: {}", id);
        return updateUserStatus(id, true, null);
    }

    @Override
    @Transactional
    public UserResponse disableUser(UUID id) {
        log.info("Disabling user: {}", id);
        return updateUserStatus(id, false, null);
    }

    @Override
    @Transactional
    public UserResponse lockUser(UUID id) {
        log.info("Locking user: {}", id);
        return updateUserStatus(id, null, true);
    }

    @Override
    @Transactional
    public UserResponse unlockUser(UUID id) {
        log.info("Unlocking user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);

        User updatedUser = userRepository.save(user);
        UserResponse response = userMapper.toResponse(updatedUser);
        cacheService.set(USER_CACHE_KEY_PREFIX + id, response);

        publishUserEvent("USER_UNLOCKED", updatedUser);

        return response;
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        log.debug("Updating last login for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        cacheService.delete(USER_CACHE_KEY_PREFIX + userId);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(String usernameOrEmail) {
        log.debug("Incrementing failed login attempts for: {}", usernameOrEmail);
        userRepository.findByUsernameOrEmail(usernameOrEmail).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setIsLocked(true);
                log.warn("User account locked due to too many failed login attempts: {}", usernameOrEmail);
                publishUserEvent("USER_LOCKED", user);
            }

            userRepository.save(user);
            cacheService.delete(USER_CACHE_KEY_PREFIX + user.getId());
        });
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(String usernameOrEmail) {
        log.debug("Resetting failed login attempts for: {}", usernameOrEmail);
        userRepository.findByUsernameOrEmail(usernameOrEmail).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            cacheService.delete(USER_CACHE_KEY_PREFIX + user.getId());
        });
    }

    private UserResponse updateUserStatus(UUID id, Boolean isEnabled, Boolean isLocked) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (isEnabled != null) {
            user.setIsEnabled(isEnabled);
        }
        if (isLocked != null) {
            user.setIsLocked(isLocked);
        }

        User updatedUser = userRepository.save(user);
        UserResponse response = userMapper.toResponse(updatedUser);
        cacheService.set(USER_CACHE_KEY_PREFIX + id, response);

        String eventType = isEnabled != null
                ? (isEnabled ? "USER_ENABLED" : "USER_DISABLED")
                : (isLocked ? "USER_LOCKED" : "USER_UNLOCKED");
        publishUserEvent(eventType, updatedUser);

        return response;
    }

    private void publishUserEvent(String eventType, User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("userId", user.getId().toString());
            event.put("username", user.getUsername());
            event.put("email", user.getEmail());
            event.put("timestamp", LocalDateTime.now().toString());

            kafkaProducerService.sendUserEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish user event: {}", eventType, e);
        }
    }
}
