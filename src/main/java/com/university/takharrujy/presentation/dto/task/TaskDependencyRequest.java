package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record TaskDependencyRequest (
        @Schema(description = "Dependency task ID", example = "2")
        @Positive(message = "Dependency Task ID must be positive")
        Long dependencyId
) {
}
