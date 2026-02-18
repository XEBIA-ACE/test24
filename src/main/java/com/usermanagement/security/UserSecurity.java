package com.usermanagement.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userSecurity")
@Slf4j
public class UserSecurity {

    public boolean isOwner(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        log.debug("Checking if user {} owns resource {}", currentUsername, userId);

        // In a real implementation, you would fetch the user ID from the authentication
        // and compare it with the provided userId
        return true;
    }

    public boolean isOwnerByUsername(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();
        log.debug("Checking if user {} matches username {}", currentUsername, username);

        return currentUsername.equals(username);
    }
}
