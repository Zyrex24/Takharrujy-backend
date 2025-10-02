package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.TaskService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.task.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Manage tasks in graduation projects")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ====================== Create ======================
    @Operation(summary = "Create a new task", description = "Creates a task inside a project")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or university not found")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse taskResponse = taskService.createTask(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskResponse, "Task created successfully"));
    }

    // ====================== Get tasks ======================
    @Operation(summary = "Get tasks for current user", description = "Fetch all tasks assigned to the authenticated student")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForUser(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        List<TaskResponse> tasks = taskService.getTasksForUser(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved successfully"));
    }

    @Operation(summary = "Get task details", description = "Fetch details of a specific task by ID")
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskDetails(
            @PathVariable Long taskId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse taskDetails = taskService.getTaskDetails(taskId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(taskDetails, "Task details retrieved successfully"));
    }

    // ====================== Update ======================
    @Operation(summary = "Update task", description = "Update fields of a task")
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse updatedTask = taskService.updateTask(taskId, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task updated successfully"));
    }

    @Operation(summary = "Update task status", description = "Change the status of a task (e.g., TODO, IN_PROGRESS, COMPLETED)")
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse updatedTask = taskService.updateTaskStatus(taskId, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task status updated successfully"));
    }

    @Operation(summary = "Assign task", description = "Assign a task to a project member (Team Leader only)")
    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignmentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse assignedTask = taskService.assignTask(taskId, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(assignedTask, "Task assigned successfully"));
    }

    // ====================== Complete ======================
    @Operation(summary = "Mark task as complete", description = "Only the assigned user can complete the task")
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @PathVariable Long taskId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskResponse completedTask = taskService.completeTask(taskId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(completedTask, "Task marked as completed successfully"));
    }

    // ====================== Dependencies ======================
    @Operation(summary = "Get task dependencies", description = "Retrieve all tasks that the given task depends on")
    @GetMapping("/{taskId}/dependencies")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getDependencies(
            @PathVariable Long taskId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        List<TaskResponse> dependencies = taskService.getTaskDependencies(taskId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(dependencies, "Dependencies retrieved successfully"));
    }

    @Operation(summary = "Add task dependency", description = "Add a dependency between tasks within the same project")
    @PostMapping("/{taskId}/dependencies")
    public ResponseEntity<ApiResponse<TaskDependencyResponse>> addDependency(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDependencyRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        TaskDependencyResponse dependency = taskService.addDependency(taskId, request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dependency, "Dependency added successfully"));
    }

    @Operation(summary = "Remove task dependency", description = "Remove an existing dependency from a task")
    @DeleteMapping("/{taskId}/dependencies/{depId}")
    public ResponseEntity<ApiResponse<Void>> removeDependency(
            @PathVariable Long taskId,
            @PathVariable Long depId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        taskService.removeTaskDependency(taskId, depId, currentUserId);
        return ResponseEntity.noContent().build(); // 204 - no content
    }

    // ====================== Delete ======================
    @Operation(summary = "Delete task", description = "Delete a task (only by creator if not referenced)")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long currentUserId) {

        taskService.deleteTask(taskId, currentUserId);
        return ResponseEntity.noContent().build(); // 204 - no content
    }
}
