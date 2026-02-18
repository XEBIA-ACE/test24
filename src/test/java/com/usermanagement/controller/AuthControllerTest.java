package com.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.dto.AuthResponse;
import com.usermanagement.domain.dto.LoginRequest;
import com.usermanagement.domain.dto.UserResponse;
import com.usermanagement.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .usernameOrEmail("johndoe")
                .password("SecurePass123!")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .isEnabled(true)
                .isLocked(false)
                .roles(Set.of("ROLE_USER"))
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token")
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .issuedAt(LocalDateTime.now())
                .user(userResponse)
                .build();
    }

    @Test
    @WithMockUser
    void login_Success() throws Exception {
        when(authenticationService.login(any(LoginRequest.class), any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(authResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(authResponse.getRefreshToken()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("johndoe"));
    }

    @Test
    @WithMockUser
    void login_InvalidRequest_BadRequest() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .usernameOrEmail("")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
