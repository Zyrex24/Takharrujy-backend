package com.university.takharrujy.presentation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for changing user password
 */
@Schema(description = "Change password request")
public record ChangePasswordRequest(
    
    @Schema(description = "Current password")
    @NotBlank(message = "Current password is required")
    String currentPassword,
    
    @Schema(description = "New password")
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    String newPassword,
    
    @Schema(description = "Confirm new password")
    @NotBlank(message = "Password confirmation is required")
    String confirmPassword
) {
    
    /**
     * Check if new password and confirmation match
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}