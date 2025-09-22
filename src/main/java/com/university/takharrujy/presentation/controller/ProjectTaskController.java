package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.ProjectTaskService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.task.GanttChartResponse;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import com.university.takharrujy.presentation.dto.task.TaskStatsResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        List<TaskResponse> tasks = projectTaskService.getProjectTasks(projectId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(tasks, "Project tasks retrieved successfully"));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<TaskResponse> overdueTasks = projectTaskService.getOverdueTasks(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(overdueTasks, "Overdue tasks retrieved successfully"));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcomingTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<TaskResponse> upcomingTasks = projectTaskService.getUpcomingTasks(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(upcomingTasks, "Upcoming tasks retrieved successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TaskStatsResponse>> getProjectTaskStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        TaskStatsResponse taskStats = projectTaskService.getTaskStats(projectId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(taskStats, "Task statistics fetched successfully"));
    }

    @GetMapping("/gantt")
    public ResponseEntity<ApiResponse<List<GanttChartResponse>>> getGanttChartData(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        List<GanttChartResponse> ganttChartData = projectTaskService.getGanttChartData(projectId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(ganttChartData, "Gantt chart data fetched successfully"));
    }
}
