package com.university.takharrujy.presentation.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Dashboard Response DTO
 * Contains project overview and statistics for dashboard
 */
@Schema(description = "Dashboard project overview")
public record DashboardResponse(
    
    @Schema(description = "Current project information")
    CurrentProjectResponse project,
    
    @Schema(description = "Progress information")
    ProgressResponse progress,
    
    @Schema(description = "Dashboard counters")
    CountersResponse counters,
    
    @Schema(description = "Recent activity feed")
    List<ActivityResponse> recentActivity
) {
    
    /**
     * Current project summary
     */
    public record CurrentProjectResponse(
        Long id,
        String title,
        String titleAr,
        String status,
        String supervisorName,
        String supervisorNameAr,
        int teamCount,
        String facultyName,
        String facultyNameAr,
        LocalDate deadline
    ) {}
    
    /**
     * Progress information
     */
    public record ProgressResponse(
        int overall,
        PhaseProgress phases
    ) {}
    
    /**
     * Phase progress breakdown
     */
    public record PhaseProgress(
        int research,
        int development,
        int testing
    ) {}
    
    /**
     * Dashboard counters
     */
    public record CountersResponse(
        long activeProjects,
        long pendingTasks,
        long upcomingDeadlines,
        long notifications
    ) {}
    
    /**
     * Activity feed item
     */
    public record ActivityResponse(
        String type,
        String title,
        String titleAr,
        String description,
        String descriptionAr,
        String fromUser,
        String fromUserAr,
        String timeAgo,
        String timestamp
    ) {}
}
