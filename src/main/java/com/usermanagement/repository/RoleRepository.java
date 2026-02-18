package com.usermanagement.repository;

import com.usermanagement.domain.entity.Role;
import com.usermanagement.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by name.
     */
    Optional<Role> findByName(RoleType name);

    /**
     * Check if role exists by name.
     */
    boolean existsByName(RoleType name);
}
