package com.usermanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User update request")
public class UserUpdateRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;
}
