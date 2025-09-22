package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

public record TaskStatusUpdateRequest (
        @Schema(description = "Task status (enum name: TODO, IN_PROGRESS, COMPLETED, BLOCKED)", example = "TODO")
        String status
) {}
