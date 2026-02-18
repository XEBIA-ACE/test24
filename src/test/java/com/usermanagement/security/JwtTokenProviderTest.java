package com.usermanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserPrincipal userPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "test-secret-key-for-jwt-token-generation-in-test-environment-must-be-long");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 7200000L);
        jwtTokenProvider.init();

        userPrincipal = new UserPrincipal(
                UUID.randomUUID(),
                "johndoe",
                "john.doe@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true,
                true
        );

        authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtTokenProvider.generateAccessToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtTokenProvider.generateRefreshToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void getUserIdFromToken_Success() {
        String token = jwtTokenProvider.generateAccessToken(authentication);

        String userId = jwtTokenProvider.getUserIdFromToken(token);

        assertNotNull(userId);
        assertEquals(userPrincipal.getId().toString(), userId);
    }

    @Test
    void validateToken_Valid() {
        String token = jwtTokenProvider.generateAccessToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_Invalid() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_Malformed() {
        String malformedToken = "malformed-token";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    void getJwtExpirationMs_ReturnsCorrectValue() {
        long expiration = jwtTokenProvider.getJwtExpirationMs();

        assertEquals(3600000L, expiration);
    }

    @Test
    void getRefreshTokenExpirationMs_ReturnsCorrectValue() {
        long expiration = jwtTokenProvider.getRefreshTokenExpirationMs();

        assertEquals(7200000L, expiration);
    }
}
