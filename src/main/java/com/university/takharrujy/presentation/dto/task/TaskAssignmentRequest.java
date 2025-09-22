package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record TaskAssignmentRequest (
        @Schema(description = "Assigned user ID", example = "123")
        @Positive(message = "AssignedTo ID must be positive")
        Long assignedToId
) {
}
