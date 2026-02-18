package com.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring service status.
 */
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health", description = "Health check APIs")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if service is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-management-service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
