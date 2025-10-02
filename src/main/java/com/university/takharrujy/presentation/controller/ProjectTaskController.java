package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.ProjectTaskService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.task.GanttChartResponse;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import com.university.takharrujy.presentation.dto.task.TaskStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@Validated
@Tag(name = "Tasks", description = "Manage project-tasks in graduation projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @Operation(summary = "Get all tasks for a project")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<TaskResponse> tasks = projectTaskService.getProjectTasks(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Project tasks retrieved successfully"));
    }

    @Operation(summary = "Get overdue tasks for a project")
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<TaskResponse> overdueTasks = projectTaskService.getOverdueTasks(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(overdueTasks, "Overdue tasks retrieved successfully"));
    }

    @Operation(summary = "Get upcoming tasks for a project")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcomingTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<TaskResponse> upcomingTasks = projectTaskService.getUpcomingTasks(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(upcomingTasks, "Upcoming tasks retrieved successfully"));
    }

    @Operation(summary = "Get project task statistics")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TaskStatsResponse>> getProjectTaskStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskStatsResponse taskStats = projectTaskService.getTaskStats(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(taskStats, "Task statistics fetched successfully"));
    }

    @Operation(summary = "Get Gantt chart data for a project")
    @GetMapping("/gantt")
    public ResponseEntity<ApiResponse<List<GanttChartResponse>>> getGanttChartData(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        List<GanttChartResponse> ganttChartData = projectTaskService.getGanttChartData(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(ganttChartData, "Gantt chart data fetched successfully"));
    }
}
