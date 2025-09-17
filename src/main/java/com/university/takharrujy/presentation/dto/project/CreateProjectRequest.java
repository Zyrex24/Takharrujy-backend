package com.university.takharrujy.presentation.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Create Project Request DTO
 * Matches the frontend create project form
 */
@Schema(description = "Create project request")
public record CreateProjectRequest(
    
    @Schema(description = "Project title", example = "AI-Powered Learning Platform", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Project title is required")
    @Size(max = 255, message = "Project title cannot exceed 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.،؛:()\\u0600-\\u06FF]+$", message = "Title contains invalid characters")
    String title,
    
    @Schema(description = "Project title in Arabic", example = "منصة تعلم مدعومة بالذكاء الاصطناعي")
    @Size(max = 255, message = "Arabic title cannot exceed 255 characters")
    String titleAr,
    
    @Schema(description = "Project description", example = "A comprehensive platform that uses artificial intelligence to personalize learning experiences for university students.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Project description is required")
    @Size(min = 50, max = 5000, message = "Project description must be between 50 and 5000 characters")
    String description,
    
    @Schema(description = "Project description in Arabic", example = "منصة شاملة تستخدم الذكاء الاصطناعي لتخصيص تجارب التعلم لطلاب الجامعة")
    @Size(max = 5000, message = "Arabic description cannot exceed 5000 characters")
    String descriptionAr,
    
    @Schema(description = "Team member user IDs (excluding current user who becomes leader)", example = "[123, 456]")
    @Size(max = 3, message = "Maximum 3 additional team members allowed (4 total including leader)")
    List<Long> teamMemberIds,
    
    @Schema(description = "Preferred supervisor ID (optional)", example = "789")
    Long preferredSupervisorId,
    
    @Schema(description = "Project category ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Project category is required")
    @Positive(message = "Category ID must be positive")
    Long categoryId,
    
    @Schema(description = "Faculty ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Faculty is required")
    @Positive(message = "Faculty ID must be positive")
    Long facultyId,
    
    @Schema(description = "Expected start date", example = "2025-02-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Expected start date is required")
    LocalDate expectedStartDate,
    
    @Schema(description = "Expected end date", example = "2025-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Expected end date is required")
    LocalDate expectedEndDate,
    
    @Schema(description = "Save as draft instead of submitting", example = "false")
    Boolean saveAsDraft
) {
    
    /**
     * Check if end date is after start date
     */
    public boolean isDateRangeValid() {
        return expectedStartDate != null && expectedEndDate != null && 
               expectedEndDate.isAfter(expectedStartDate);
    }
    
    /**
     * Get team member IDs or empty list
     */
    public List<Long> getTeamMemberIds() {
        return teamMemberIds != null ? teamMemberIds : List.of();
    }
    
    /**
     * Check if should save as draft
     */
    public boolean isSaveAsDraft() {
        return saveAsDraft != null && saveAsDraft;
    }
}
