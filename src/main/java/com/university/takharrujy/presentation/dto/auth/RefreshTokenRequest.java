package com.university.takharrujy.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO
 */
@Schema(description = "Refresh token request")
public record RefreshTokenRequest(
    
    @NotBlank(message = "Refresh token is required")
    @Schema(description = "JWT refresh token")
    String refreshToken
) {}