package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.TaskService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.task.*;
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
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        TaskResponse taskResponse = taskService.createTask(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskResponse, "Task created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForUser(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        List<TaskResponse> tasks = taskService.getTasksForUser(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved successfully"));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskDetails(
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        TaskResponse taskDetails = taskService.getTaskDetails(taskId, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(taskDetails, "Task details retrieved successfully"));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        TaskResponse updatedTask = taskService.updateTask(taskId, request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(updatedTask, "Task updated successfully"));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<String>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.deleteTask(taskId, currentUserId)));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTaskStatus(taskId, request, currentUserId), "Task status updated successfully"));
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignmentRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.assignTask(taskId, request, currentUserId), "Task assigned to student successfully"));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.completeTask(taskId, currentUserId), "Task marked as completed successfully"));
    }

    @GetMapping("/{taskId}/dependencies")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getDependencies(
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskDependencies(taskId, currentUserId), "Dependencies retrieved successfully"));
    }

    @PostMapping("/{taskId}/dependencies")
    public ResponseEntity<ApiResponse<TaskDependencyResponse>> addDependency(
            @PathVariable Long taskId,
            @RequestBody TaskDependencyRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.addDependency(taskId, request, currentUserId), "Dependency added successfully"));
    }

    @DeleteMapping("/{taskId}/dependencies/{depId}")
    public ResponseEntity<ApiResponse<String>> removeDependency(
            @PathVariable Long taskId,
            @PathVariable Long depId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(taskService.removeTaskDependency(taskId, depId, currentUserId)));
    }
}
