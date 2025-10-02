package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Supervisor Dashboard Response")
public record SupervisorDashboardResponse(

        @Schema(description = "Total supervised projects", example = "5")
        long totalProjects,

        @Schema(description = "Total students under supervision", example = "20")
        long totalStudents,

        @Schema(description = "Total tasks in all supervised projects", example = "120")
        long totalTasks,

        @Schema(description = "Completed tasks count", example = "75")
        long completedTasks,

        @Schema(description = "Ongoing tasks count", example = "30")
        long inProgressTasks,

        @Schema(description = "Blocked tasks count", example = "15")
        long blockedTasks,

        @Schema(description = "List of project summaries")
        List<ProjectSummaryDTO> projects
) {
}
