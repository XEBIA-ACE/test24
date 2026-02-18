package com.usermanagement.service;

import com.usermanagement.domain.dto.UserRegistrationRequest;
import com.usermanagement.domain.dto.UserResponse;
import com.usermanagement.domain.entity.Role;
import com.usermanagement.domain.entity.User;
import com.usermanagement.domain.enums.RoleType;
import com.usermanagement.exception.BadRequestException;
import com.usermanagement.exception.ResourceNotFoundException;
import com.usermanagement.mapper.UserMapper;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheService cacheService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequest registrationRequest;
    private User user;
    private Role userRole;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registrationRequest = UserRegistrationRequest.builder()
                .username("johndoe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        userRole = Role.builder()
                .id(UUID.randomUUID())
                .name(RoleType.ROLE_USER)
                .description("User role")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .isEnabled(true)
                .isLocked(false)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isEnabled(true)
                .isLocked(false)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRegistrationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.registerUser(registrationRequest);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(cacheService).set(anyString(), any(UserResponse.class));
        verify(kafkaProducerService).sendUserEvent(any());
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(cacheService.get(anyString(), eq(UserResponse.class))).thenReturn(null);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());

        verify(userRepository).findById(user.getId());
        verify(cacheService).set(anyString(), any(UserResponse.class));
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();
        when(cacheService.get(anyString(), eq(UserResponse.class))).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }

    @Test
    void getUserById_FromCache_Success() {
        when(cacheService.get(anyString(), eq(UserResponse.class))).thenReturn(userResponse);

        UserResponse result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(userResponse.getUsername(), result.getUsername());

        verify(userRepository, never()).findById(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        verify(userRepository).delete(user);
        verify(cacheService).delete(anyString());
        verify(kafkaProducerService).sendUserEvent(any());
    }

    @Test
    void lockUser_Success() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.lockUser(user.getId());

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(cacheService).set(anyString(), any(UserResponse.class));
    }
}
