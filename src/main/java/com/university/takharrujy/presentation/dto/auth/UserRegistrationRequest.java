package com.university.takharrujy.presentation.dto.auth;

import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.infrastructure.validation.ValidArabicText;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User registration request DTO
 */
@Schema(description = "User registration request")
public record UserRegistrationRequest(
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Schema(description = "University email address", example = "student@cu.edu.eg")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "Strong password", example = "Password123!")
    String password,
    
    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Password confirmation", example = "Password123!")
    String confirmPassword,
    
    @NotBlank(message = "First name is required")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, allowNumbers = false, allowSpecialChars = false)
    @Schema(description = "First name", example = "أحمد")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, allowNumbers = false, allowSpecialChars = false)
    @Schema(description = "Last name", example = "محمد")
    String lastName,
    
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, allowNumbers = false, allowSpecialChars = false, required = false)
    @Schema(description = "First name in Arabic", example = "أحمد")
    String firstNameAr,
    
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, allowNumbers = false, allowSpecialChars = false, required = false)
    @Schema(description = "Last name in Arabic", example = "محمد")
    String lastNameAr,
    
    @NotNull(message = "User role is required")
    @Schema(description = "User role", example = "STUDENT")
    UserRole role,
    
    @Size(max = 20, message = "Student ID cannot exceed 20 characters")
    @Schema(description = "Student ID (required for students)", example = "ST20241001")
    String studentId,
    
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+201234567890")
    String phone,
    
    @NotNull(message = "Department ID is required")
    @Schema(description = "Department ID", example = "1")
    Long departmentId,
    
    @Size(max = 10, message = "Preferred language cannot exceed 10 characters")
    @Pattern(regexp = "^(ar|en)$", message = "Preferred language must be 'ar' or 'en'")
    @Schema(description = "Preferred language", example = "ar", defaultValue = "ar")
    String preferredLanguage
) {
    
    /**
     * Custom validation: passwords must match
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Custom validation: student ID required for students
     */
    public boolean isStudentIdValidForRole() {
        if (role == UserRole.STUDENT) {
            return studentId != null && !studentId.trim().isEmpty();
        }
        return true;
    }
}