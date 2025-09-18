package com.university.takharrujy.presentation.dto.task;

import com.university.takharrujy.domain.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

/**
 * Create Task Request DTO
 * Matches the frontend create task form
 */
@Schema(description = "Create task request")
public record TaskCreateRequest(

        @Schema(description = "Task title", example = "Prepare AI model dataset", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title cannot exceed 255 characters")
        String title,

        @Schema(description = "Task title in Arabic", example = "تحضير بيانات نموذج الذكاء الاصطناعي")
        @Size(max = 255, message = "Arabic title cannot exceed 255 characters")
        String titleAr,

        @Schema(description = "Task description", example = "Collect and clean dataset for training AI model")
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @Schema(description = "Task description in Arabic", example = "جمع وتنظيف البيانات لتدريب نموذج الذكاء الاصطناعي")
        @Size(max = 2000, message = "Arabic description cannot exceed 2000 characters")
        String descriptionAr,

        @Schema(description = "Task status (enum name: TODO, IN_PROGRESS, COMPLETED, BLOCKED)", example = "TODO")
        String status,

        @Schema(description = "Task start date", example = "2025-02-01")
        LocalDate startDate,

        @Schema(description = "Task due date", example = "2025-02-10")
        LocalDate dueDate,

        @Schema(description = "Priority level (1 = Low, 2 = Medium, 3 = High, 4 = Critical)", example = "2")
        @Min(value = 1, message = "Priority must be at least 1")
        @Max(value = 4, message = "Priority cannot exceed 4")
        Integer priority,

        @Schema(description = "Estimated hours to complete task", example = "10")
        @Positive(message = "Estimated hours must be positive")
        Integer estimatedHours,

        @Schema(description = "Notes about task", example = "Dataset must be cleaned and normalized")
        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes,

        @Schema(description = "Notes in Arabic", example = "يجب تنظيف البيانات وتوحيدها")
        @Size(max = 1000, message = "Arabic notes cannot exceed 1000 characters")
        String notesAr,

        @Schema(description = "Actual hours spent on task", example = "8")
        @PositiveOrZero(message = "Actual hours must be zero or positive")
        Integer actualHours,

        @Schema(description = "Task progress percentage", example = "50")
        @Min(value = 0, message = "Progress percentage must be at least 0")
        @Max(value = 100, message = "Progress percentage cannot exceed 100")
        Integer progressPercentage,

        @Schema(description = "Is this task a milestone?", example = "false")
        Boolean isMilestone,

        @Schema(description = "Task order in project", example = "1")
        @Positive(message = "Task order must be positive")
        Integer taskOrder,

        @Schema(description = "University ID", example = "1")
        @Positive(message = "University ID must be positive")
        Long universityId,

        @Schema(description = "Project ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Project ID is required")
        @Positive(message = "Project ID must be positive")
        Long projectId,

        @Schema(description = "Assigned user ID", example = "123")
        @Positive(message = "AssignedTo ID must be positive")
        Long assignedToId,

        @Schema(description = "Parent task ID", example = "5")
        @Positive(message = "Parent Task ID must be positive")
        Long parentTaskId,

        @Schema(description = "Dependency task IDs", example = "[2, 3]")
        Set<@Positive(message = "Dependency IDs must be positive") Long> dependencyIds
) {

    /**
     * Check if due date is after start date
     */
    public boolean isDateRangeValid() {
        return startDate == null || dueDate == null || dueDate.isAfter(startDate);
    }
}
