package com.university.takharrujy.presentation.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminDashboardResponse", description = "Response DTO for admin dashboard")
public record AdminDashboardResponse(

        @Schema(description = "Total number of projects", example = "120")
        long totalProjects,

        @Schema(description = "Total number of users", example = "450")
        long totalUsers,

        @Schema(description = "Total number of supervisors", example = "25")
        long totalSupervisors,

        @Schema(description = "Total number of deliverables", example = "300")
        long totalDeliverables,

        @Schema(description = "Total number of universities", example = "10")
        long totalUniversities
) {}
