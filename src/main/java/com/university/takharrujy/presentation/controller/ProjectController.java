package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.ProjectService;
import com.university.takharrujy.infrastructure.security.CustomUserDetailsService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.project.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Project Controller
 * Handles project management endpoints
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(
        summary = "Create New Project",
        description = "Create a new graduation project. Can be saved as draft or submitted for review."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid project data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "User already has an active project",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.info("Creating project: {} for user: {}", request.title(), userId);
        
        ProjectResponse projectResponse = projectService.createProject(request, userId);
        
        String message = request.isSaveAsDraft() 
            ? "Project saved as draft successfully" 
            : "Project submitted for review successfully";
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectResponse, message));
    }

    @Operation(
        summary = "Get Current Project Dashboard",
        description = "Get dashboard overview of user's current project including progress and statistics"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dashboard data retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/me/current")
    public ResponseEntity<ApiResponse<DashboardResponse>> getCurrentProjectDashboard(
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.debug("Getting dashboard for user: {}", userId);
        
        DashboardResponse dashboard = projectService.getCurrentProjectDashboard(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(dashboard, "Dashboard data retrieved successfully")
        );
    }

    @Operation(
        summary = "Get Project Details",
        description = "Get detailed information about a specific project"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Project details retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.debug("Getting project: {} for user: {}", id, userId);
        
        ProjectResponse project = projectService.getProject(id, userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(project, "Project details retrieved successfully")
        );
    }

    @Operation(
        summary = "Get Project Overview",
        description = "Get project overview for dashboard (same as get project but focused on overview data)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Project overview retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/{id}/overview")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectOverview(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.debug("Getting project overview: {} for user: {}", id, userId);
        
        ProjectResponse project = projectService.getProject(id, userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(project, "Project overview retrieved successfully")
        );
    }

    @Operation(
        summary = "Update Project",
        description = "Update project details. Only available for DRAFT and IN_PROGRESS projects."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Project updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid project data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Project cannot be edited in current status",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.info("Updating project: {} by user: {}", id, userId);
        
        ProjectResponse project = projectService.updateProject(id, request, userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(project, "Project updated successfully")
        );
    }

    @Operation(
        summary = "Submit Project",
        description = "Submit project for review (transition from DRAFT to SUBMITTED)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Project submitted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Project cannot be submitted",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitProject(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        logger.info("Submitting project: {} by user: {}", id, userId);
        
        ProjectResponse project = projectService.submitProject(id, userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(project, "Project submitted for review successfully")
        );
    }

    /**
     * Extract current user ID from authentication
     */
    private Long getCurrentUserId(Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUserId();
    }
}
