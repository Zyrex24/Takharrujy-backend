package com.university.takharrujy.presentation.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Reset password request DTO
 */
@Schema(description = "Reset password request")
public record ResetPasswordRequest(
    
    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token from email")
    String token,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "New strong password", example = "NewPassword123!")
    String newPassword,
    
    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "New password confirmation", example = "NewPassword123!")
    String confirmPassword
) {
    
    /**
     * Custom validation: passwords must match
     */
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}