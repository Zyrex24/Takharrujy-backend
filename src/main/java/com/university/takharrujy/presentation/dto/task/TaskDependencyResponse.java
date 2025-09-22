package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TaskDependencyResponse (
        @Schema(description = "University ID", example = "1")
        Long id,

        @Schema(description = "Task title", example = "Prepare AI model dataset")
        String title,

        @Schema(description = "Task status", example = "TODO")
        String status
) {
}
