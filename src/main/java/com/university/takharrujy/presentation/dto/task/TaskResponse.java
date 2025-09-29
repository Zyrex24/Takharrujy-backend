package com.university.takharrujy.presentation.dto.task;

import com.university.takharrujy.domain.enums.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task Response DTO
 * Used for returning task data to frontend
 */
@Schema(description = "Task information")
public record TaskResponse(

        @Schema(description = "Task ID", example = "101")
        Long id,

        @Schema(description = "Task title", example = "Design UI")
        String title,

        @Schema(description = "Task description", example = "Create wireframes for the mobile app")
        String description,

        @Schema(description = "Task status", example = "TODO")
        String status,

        @Schema(description = "Task priority (numeric)", example = "3")
        Integer priority,

        @Schema(description = "Task priority (text)", example = "High")
        String priorityText,

        @Schema(description = "Start date", example = "2025-09-10")
        LocalDate startDate,

        @Schema(description = "Due date", example = "2025-09-30")
        LocalDate dueDate,

        @Schema(description = "Completion date", example = "2025-09-20")
        LocalDate completionDate,

        @Schema(description = "Progress percentage", example = "40")
        Integer progressPercentage,

        @Schema(description = "Assigned user")
        AssigneeResponse assignee,

        @Schema(description = "Related project ID", example = "42")
        Long projectId,

        @Schema(description = "Created date")
        LocalDateTime createdAt,

        @Schema(description = "Updated date")
        LocalDateTime updatedAt
) {

    /**
     * Assignee information
     */
    public record AssigneeResponse(
            @Schema(description = "User ID", example = "12")
            Long id,

            @Schema(description = "First name", example = "Omar")
            String firstName,

            @Schema(description = "Last name", example = "Ali")
            String lastName,

            @Schema(description = "First name (Arabic)", example = "عمر")
            String firstNameAr,

            @Schema(description = "Last name (Arabic)", example = "علي")
            String lastNameAr,

            @Schema(description = "Email address", example = "omar@example.com")
            String email
    ) {}
}
