package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

public record TaskStatsResponse(

        @Schema(description = "Total number of tasks", example = "50")
        long totalTasks,

        @Schema(description = "Number of completed tasks", example = "20")
        long completedTasks,

        @Schema(description = "Number of pending tasks", example = "25")
        long pendingTasks,

        @Schema(description = "Number of overdue tasks", example = "5")
        long overdueTasks
) {}