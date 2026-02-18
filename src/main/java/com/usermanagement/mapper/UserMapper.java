package com.usermanagement.mapper;

import com.usermanagement.domain.dto.UserRegistrationRequest;
import com.usermanagement.domain.dto.UserResponse;
import com.usermanagement.domain.entity.User;
import com.usermanagement.domain.enums.RoleType;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserRegistrationRequest request);

    default Set<String> mapRolesToStrings(Set<com.usermanagement.domain.entity.Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
