package com.university.takharrujy.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resend email verification request DTO
 */
@Schema(description = "Resend email verification request")
public record ResendVerificationRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Schema(description = "University email address", example = "student@cu.edu.eg")
    String email
) {}