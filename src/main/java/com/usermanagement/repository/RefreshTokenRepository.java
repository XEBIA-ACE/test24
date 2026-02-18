package com.usermanagement.repository;

import com.usermanagement.domain.entity.RefreshToken;
import com.usermanagement.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user.
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Delete all refresh tokens for a user.
     */
    void deleteByUser(User user);

    /**
     * Delete expired tokens.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete revoked tokens older than specified date.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.revokedAt < :date")
    void deleteRevokedTokensOlderThan(@Param("date") LocalDateTime date);
}
