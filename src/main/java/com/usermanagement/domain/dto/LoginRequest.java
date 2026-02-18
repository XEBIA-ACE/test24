package com.usermanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username or email", example = "john_doe")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "SecurePass123!")
    private String password;
}
