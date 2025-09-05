package com.university.takharrujy.presentation.dto.user;

import com.university.takharrujy.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

/**
 * User response DTO for API responses
 */
@Schema(description = "User information response")
public record UserResponse(
    
    @Schema(description = "User ID")
    Long id,
    
    @Schema(description = "Email address")
    String email,
    
    @Schema(description = "First name")
    String firstName,
    
    @Schema(description = "Last name")
    String lastName,
    
    @Schema(description = "First name in Arabic")
    String firstNameAr,
    
    @Schema(description = "Last name in Arabic")
    String lastNameAr,
    
    @Schema(description = "User role")
    UserRole role,
    
    @Schema(description = "Student ID")
    String studentId,
    
    @Schema(description = "Phone number")
    String phone,
    
    @Schema(description = "Date of birth")
    LocalDate dateOfBirth,
    
    @Schema(description = "Whether user is active")
    Boolean isActive,
    
    @Schema(description = "Whether email is verified")
    Boolean isEmailVerified,
    
    @Schema(description = "Profile picture URL")
    String profilePictureUrl,
    
    @Schema(description = "User bio")
    String bio,
    
    @Schema(description = "User bio in Arabic")
    String bioAr,
    
    @Schema(description = "Preferred language")
    String preferredLanguage,
    
    @Schema(description = "University information")
    UniversityResponse university,
    
    @Schema(description = "Department information")
    DepartmentResponse department,
    
    @Schema(description = "Account creation timestamp")
    Instant createdAt,
    
    @Schema(description = "Account last update timestamp")
    Instant updatedAt
) {
    
    /**
     * Get full name based on preferred language
     */
    public String getFullName() {
        if ("ar".equals(preferredLanguage) && firstNameAr != null && lastNameAr != null) {
            return firstNameAr + " " + lastNameAr;
        }
        return firstName + " " + lastName;
    }
    
    /**
     * Check if user is a student
     */
    public boolean isStudent() {
        return UserRole.STUDENT.equals(role);
    }
    
    /**
     * Check if user is a supervisor
     */
    public boolean isSupervisor() {
        return UserRole.SUPERVISOR.equals(role);
    }
    
    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }
}