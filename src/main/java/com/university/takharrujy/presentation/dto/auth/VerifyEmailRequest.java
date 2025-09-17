package com.university.takharrujy.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Email verification request DTO
 */
@Schema(description = "Email verification request")
public record VerifyEmailRequest(
    
    @NotBlank(message = "Verification token is required")
    @Schema(description = "Email verification token from email")
    String token
) {}