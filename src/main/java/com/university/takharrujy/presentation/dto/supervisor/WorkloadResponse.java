package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supervisor workload overview")
public record WorkloadResponse(

        @Schema(description = "Total projects supervised", example = "5")
        int totalProjects,

        @Schema(description = "Total tasks in all projects", example = "120")
        int totalTasks,

        @Schema(description = "Number of tasks completed", example = "75")
        int completedTasks,

        @Schema(description = "Number of tasks still pending", example = "45")
        int pendingTasks,

        @Schema(description = "Overall workload completion percentage", example = "62")
        int completionPercentage
) {}
