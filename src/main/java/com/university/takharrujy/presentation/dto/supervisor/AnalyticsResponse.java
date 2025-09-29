package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supervision analytics response for a supervisor")
public record AnalyticsResponse(

        @Schema(description = "Total number of supervised projects", example = "12")
        long totalProjects,

        @Schema(description = "Number of approved projects", example = "7")
        long approvedProjects,

        @Schema(description = "Number of rejected projects", example = "2")
        long rejectedProjects,

        @Schema(description = "Number of projects currently in progress", example = "3")
        long inProgressProjects
) {}
