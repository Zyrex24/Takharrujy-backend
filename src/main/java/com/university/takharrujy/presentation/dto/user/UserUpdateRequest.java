package com.university.takharrujy.presentation.dto.user;

import com.university.takharrujy.infrastructure.validation.ValidArabicText;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for updating user profile
 */
@Schema(description = "User profile update request")
public record UserUpdateRequest(
    
    @Schema(description = "First name", example = "Ahmed")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, required = false)
    String firstName,
    
    @Schema(description = "Last name", example = "Mohamed")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = true, required = false)
    String lastName,
    
    @Schema(description = "First name in Arabic", example = "أحمد")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = false, required = false)
    String firstNameAr,
    
    @Schema(description = "Last name in Arabic", example = "محمد")
    @ValidArabicText(minLength = 2, maxLength = 100, allowEnglish = false, required = false)
    String lastNameAr,
    
    @Schema(description = "Phone number", example = "+201234567890")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String phone,
    
    @Schema(description = "Date of birth")
    LocalDate dateOfBirth,
    
    @Schema(description = "User bio", example = "Computer Science student")
    @ValidArabicText(minLength = 10, maxLength = 1000, allowEnglish = true, required = false)
    String bio,
    
    @Schema(description = "User bio in Arabic", example = "طالب علوم حاسوب")
    @ValidArabicText(minLength = 10, maxLength = 1000, allowEnglish = false, required = false)
    String bioAr,
    
    @Schema(description = "Preferred language", example = "ar", allowableValues = {"ar", "en"})
    @Pattern(regexp = "^(ar|en)$", message = "Preferred language must be 'ar' or 'en'")
    String preferredLanguage,
    
    @Schema(description = "Department ID")
    Long departmentId
) {
    
    /**
     * Check if the request has any non-null fields to update
     */
    public boolean hasUpdates() {
        return firstName != null || lastName != null || firstNameAr != null || 
               lastNameAr != null || phone != null || dateOfBirth != null || 
               bio != null || bioAr != null || preferredLanguage != null || 
               departmentId != null;
    }
}