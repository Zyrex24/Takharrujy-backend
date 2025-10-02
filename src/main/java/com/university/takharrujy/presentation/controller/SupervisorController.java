package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.SupervisorService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.project.ProjectResponse;
import com.university.takharrujy.presentation.dto.supervisor.*;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supervisor")
@PreAuthorize("hasRole('SUPERVISOR')")
@Validated
@Tag(name = "Supervisor", description = "Supervisor project and student management APIs")
public class SupervisorController {

    private static final Logger log = LoggerFactory.getLogger(SupervisorController.class);

    private final SupervisorService supervisorService;

    public SupervisorController(SupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }

    @Operation(summary = "Get supervisor dashboard", description = "Retrieve aggregated dashboard data for supervisor")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SupervisorDashboardResponse>> getDashboard(
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        SupervisorDashboardResponse dashboard = supervisorService.getDashboard(supervisorId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard retrieved successfully"));
    }

    @Operation(summary = "Get assigned projects", description = "Retrieve all projects assigned to the supervisor")
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        List<ProjectResponse> projects = supervisorService.getAssignedProjects(supervisorId);
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved successfully"));
    }

    @Operation(summary = "Get project overview", description = "Retrieve overview of a specific project supervised by this supervisor")
    @GetMapping("/projects/{projectId}/overview")
    public ResponseEntity<ApiResponse<ProjectOverviewResponse>> getProjectOverview(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        ProjectOverviewResponse response = supervisorService.getProjectOverview(projectId, supervisorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Project overview retrieved successfully"));
    }

    @Operation(summary = "Get workload", description = "Retrieve workload analytics (tasks, completion, etc.) for supervisor")
    @GetMapping("/workload")
    public ResponseEntity<ApiResponse<WorkloadResponse>> getSupervisorWorkload(
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        WorkloadResponse workload = supervisorService.getSupervisorWorkload(supervisorId);
        return ResponseEntity.ok(ApiResponse.success(workload, "Workload retrieved successfully"));
    }

    @Operation(summary = "Get supervised students", description = "Retrieve a list of all students under supervisor’s projects")
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getSupervisedStudents(
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        List<UserResponse> students = supervisorService.getSupervisedStudents(supervisorId);
        return ResponseEntity.ok(ApiResponse.success(students, "Students retrieved successfully"));
    }

    @Operation(summary = "Approve or reject project", description = "Approve a submitted project and assign supervisor, or reject it")
    @PostMapping("/projects/{projectId}/approve")
    public ResponseEntity<ApiResponse<ProjectResponse>> approveProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long supervisorId,
            @Valid @RequestBody ApprovalRequest request
    ) {
        ProjectResponse response = supervisorService.approveProject(projectId, supervisorId, request);
        log.info("Supervisor [{}] {} project [{}]",
                supervisorId,
                request.approved() ? "approved" : "rejected",
                projectId);

        String message = request.approved() ?
                "Project approved successfully" :
                "Project rejected successfully";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(summary = "Provide project feedback", description = "Add supervisor feedback (EN/AR) to a supervised project")
    @PostMapping("/projects/{projectId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> provideFeedback(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long supervisorId,
            @Valid @RequestBody FeedbackRequest request
    ) {
        FeedbackResponse response = supervisorService.provideFeedback(projectId, supervisorId, request);
        log.info("Supervisor [{}] provided feedback for project [{}]", supervisorId, projectId);
        return ResponseEntity.ok(ApiResponse.success(response, "Feedback submitted successfully"));
    }

    @Operation(summary = "Get supervision analytics", description = "Retrieve analytics for supervised projects (approved, rejected, in-progress)")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getSupervisionAnalytics(
            @AuthenticationPrincipal(expression = "userId") Long supervisorId
    ) {
        AnalyticsResponse response = supervisorService.getSupervisionAnalytics(supervisorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Analytics retrieved successfully"));
    }
}
