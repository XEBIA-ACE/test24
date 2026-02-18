package com.usermanagement.repository;

import com.usermanagement.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("johndoe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .isEnabled(true)
                .isLocked(false)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void findByUsername_Success() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void findByUsername_NotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_Success() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
    }

    @Test
    void findByUsernameOrEmail_WithUsername() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByUsernameOrEmail("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
    }

    @Test
    void findByUsernameOrEmail_WithEmail() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByUsernameOrEmail("john.doe@example.com");

        assertTrue(found.isPresent());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void existsByUsername_True() {
        entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByUsername("johndoe");

        assertTrue(exists);
    }

    @Test
    void existsByUsername_False() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertFalse(exists);
    }

    @Test
    void existsByEmail_True() {
        entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_False() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void save_Success() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals("johndoe", savedUser.getUsername());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void delete_Success() {
        User savedUser = entityManager.persistAndFlush(testUser);

        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<User> found = userRepository.findById(savedUser.getId());
        assertFalse(found.isPresent());
    }
}
