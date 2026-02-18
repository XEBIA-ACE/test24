package com.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main application class for User Management Service.
 * Provides user management capabilities with OAuth2 PKCE, JWT authentication,
 * Redis caching, and Kafka event streaming.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
public class UserManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }
}
