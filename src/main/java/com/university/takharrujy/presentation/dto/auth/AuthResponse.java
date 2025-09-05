package com.university.takharrujy.presentation.dto.auth;

import com.university.takharrujy.presentation.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Authentication response DTO
 */
@Schema(description = "Authentication response with tokens and user data")
public record AuthResponse(
    
    @Schema(description = "JWT access token")
    String accessToken,
    
    @Schema(description = "JWT refresh token")
    String refreshToken,
    
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    
    @Schema(description = "Access token expiration time")
    Instant accessTokenExpiresAt,
    
    @Schema(description = "Refresh token expiration time")
    Instant refreshTokenExpiresAt,
    
    @Schema(description = "Authenticated user information")
    UserResponse user
) {
    
    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
    
    public static AuthResponse of(String accessToken, String refreshToken, 
                                 Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt, 
                                 UserResponse user) {
        return new AuthResponse(
            accessToken, 
            refreshToken, 
            "Bearer", 
            accessTokenExpiresAt, 
            refreshTokenExpiresAt, 
            user
        );
    }
}