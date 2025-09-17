package com.university.takharrujy.presentation.dto.project;

import com.university.takharrujy.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Response DTO
 * Used for returning project data to frontend
 */
@Schema(description = "Project information")
public record ProjectResponse(
    
    @Schema(description = "Project ID", example = "42")
    Long id,
    
    @Schema(description = "Project title", example = "AI-Powered Learning Platform")
    String title,
    
    @Schema(description = "Project title in Arabic", example = "منصة تعلم مدعومة بالذكاء الاصطناعي")
    String titleAr,
    
    @Schema(description = "Project description", example = "A comprehensive platform...")
    String description,
    
    @Schema(description = "Project description in Arabic")
    String descriptionAr,
    
    @Schema(description = "Project status", example = "IN_PROGRESS")
    ProjectStatus status,
    
    @Schema(description = "Start date", example = "2025-02-01")
    LocalDate startDate,
    
    @Schema(description = "Due date", example = "2025-05-15")
    LocalDate dueDate,
    
    @Schema(description = "Completion date", example = "2025-05-10")
    LocalDate completionDate,
    
    @Schema(description = "Progress percentage", example = "65")
    Integer progressPercentage,
    
    @Schema(description = "Final grade", example = "85.5")
    BigDecimal finalGrade,
    
    @Schema(description = "Team leader information")
    TeamMemberResponse teamLeader,
    
    @Schema(description = "Supervisor information")
    SupervisorResponse supervisor,
    
    @Schema(description = "Team members")
    List<TeamMemberResponse> teamMembers,
    
    @Schema(description = "Faculty name", example = "FCAI-CU")
    String facultyName,
    
    @Schema(description = "Project category", example = "Web Development")
    String categoryName,
    
    @Schema(description = "Created date")
    LocalDateTime createdAt,
    
    @Schema(description = "Updated date")
    LocalDateTime updatedAt
) {
    
    /**
     * Team member information
     */
    public record TeamMemberResponse(
        Long id,
        String firstName,
        String lastName,
        String firstNameAr,
        String lastNameAr,
        String email,
        boolean isLeader
    ) {}
    
    /**
     * Supervisor information
     */
    public record SupervisorResponse(
        Long id,
        String firstName,
        String lastName,
        String firstNameAr,
        String lastNameAr,
        String email,
        String title
    ) {}
}
