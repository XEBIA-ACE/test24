package com.usermanagement.repository;

import com.usermanagement.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find permission by name.
     */
    Optional<Permission> findByName(String name);

    /**
     * Check if permission exists by name.
     */
    boolean existsByName(String name);
}
