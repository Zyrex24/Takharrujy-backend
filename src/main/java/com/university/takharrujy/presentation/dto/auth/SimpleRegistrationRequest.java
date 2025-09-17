package com.university.takharrujy.presentation.dto.auth;

import com.university.takharrujy.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Simple Registration Request DTO
 * Simplified registration for frontend forms
 */
@Schema(description = "Simple user registration request")
public record SimpleRegistrationRequest(
    
    @Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName,
    
    @Schema(description = "University email address", example = "john.doe@university.edu", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,
    
    @Schema(description = "Password (minimum 8 characters)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    String password,
    
    @Schema(description = "Password confirmation", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password confirmation is required")
    String confirmPassword,
    
    @Schema(description = "User role", example = "STUDENT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is required")
    UserRole role,
    
    @Schema(description = "University ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "University is required")
    @Positive(message = "University ID must be positive")
    Long universityId,
    
    
    @Schema(description = "Accept terms and conditions", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "You must accept the terms and conditions")
    @AssertTrue(message = "You must accept the terms and conditions")
    Boolean acceptTerms,
    
    @Schema(description = "Preferred language", example = "en", allowableValues = {"en", "ar"})
    String locale,
    
    @Schema(description = "Remember me option", example = "false")
    Boolean rememberMe
) {
    
    /**
     * Check if passwords match
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Check if student ID is valid for the role (always true for simple registration)
     */
    public boolean isStudentIdValidForRole() {
        return true; // Not required for simple registration form
    }
    
    
    /**
     * Get first name from full name
     */
    public String getFirstName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }
    
    /**
     * Get last name from full name
     */
    public String getLastName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
    
    /**
     * Get preferred language with default
     */
    public String getPreferredLanguage() {
        return locale != null && !locale.trim().isEmpty() ? locale : "en";
    }
    
    /**
     * Check if remember me is enabled
     */
    public boolean isRememberMe() {
        return rememberMe != null && rememberMe;
    }
}
