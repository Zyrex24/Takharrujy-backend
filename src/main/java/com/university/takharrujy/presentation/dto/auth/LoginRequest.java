package com.university.takharrujy.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User login request DTO
 */
@Schema(description = "User login request")
public record LoginRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Schema(description = "University email address", example = "student@cu.edu.eg")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(max = 128, message = "Password cannot exceed 128 characters")
    @Schema(description = "User password", example = "Password123!")
    String password,
    
    @Schema(description = "Remember me option", example = "false", defaultValue = "false")
    Boolean rememberMe
) {
    
    public LoginRequest {
        if (rememberMe == null) {
            rememberMe = false;
        }
    }
}