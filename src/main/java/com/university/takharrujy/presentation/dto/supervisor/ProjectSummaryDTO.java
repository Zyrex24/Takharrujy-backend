package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project summary for supervisor dashboard")
public record ProjectSummaryDTO(

        @Schema(description = "Project ID", example = "10")
        Long id,

        @Schema(description = "Project title", example = "AI-powered Student Assistant")
        String title,

        @Schema(description = "Project title in Arabic", example = "المساعد الذكي للطلاب")
        String titleAr,

        @Schema(description = "Number of tasks in this project", example = "25")
        long totalTasks,

        @Schema(description = "Completed tasks in this project", example = "15")
        long completedTasks,

        @Schema(description = "Students count in this project", example = "5")
        long studentCount
) {
}
