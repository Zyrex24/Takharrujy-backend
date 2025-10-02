package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.AdminService;
import com.university.takharrujy.application.service.ProjectService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.admin.AdminDashboardResponse;
import com.university.takharrujy.presentation.dto.admin.RoleUpdateRequest;
import com.university.takharrujy.presentation.dto.admin.StatusUpdateRequest;
import com.university.takharrujy.presentation.dto.admin.SupervisorAssignmentRequest;
import com.university.takharrujy.presentation.dto.project.ProjectResponse;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only management operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final ProjectService projectService;

    public AdminController(AdminService adminService, ProjectService projectService) {
        this.adminService = adminService;
        this.projectService = projectService;
    }

    @Operation(summary = "Get admin dashboard data", description = "Fetches statistics about projects, users, supervisors, deliverables, and universities")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard data fetched successfully"));
    }

    @Operation(summary = "Get all users", description = "Fetches all users in the system (Admin only)")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsersForAdmin();
        return ResponseEntity.ok(ApiResponse.success(users, "All users fetched successfully"));
    }

    @Operation(summary = "Update user role", description = "Update the role of a user")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request
    ) {
        UserResponse updatedUser = adminService.updateUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User role updated successfully"));
    }

    @Operation(summary = "Update user status", description = "Activate or deactivate a user account")
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request
    ) {
        UserResponse updated = adminService.updateUserStatus(userId, request.isActive());
        return ResponseEntity.ok(ApiResponse.success(updated, "User status updated successfully"));
    }

    @Operation(summary = "Delete user account", description = "Soft-delete a user (mark inactive)")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @Operation(summary = "Get all projects", description = "Fetch all graduation projects")
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(projects, "All projects fetched successfully"));
    }

    @Operation(summary = "Assign supervisor to project", description = "Assign or change a supervisor for a given project")
    @PutMapping("/projects/{projectId}/supervisor")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignSupervisor(
            @PathVariable Long projectId,
            @RequestBody SupervisorAssignmentRequest request
    ) {
        ProjectResponse updatedProject = projectService.assignSupervisor(projectId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProject, "Supervisor assigned successfully"));
    }
}
