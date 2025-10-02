package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.DeliverableService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableResponse;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableRequest;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/deliverables")
@Validated
@Tag(name = "Project Deliverables", description = "Endpoints for managing project-level deliverables")
@SecurityRequirement(name = "bearerAuth")
public class ProjectDeliverablesController {

    private final DeliverableService deliverableService;

    public ProjectDeliverablesController(DeliverableService deliverableService) {
        this.deliverableService = deliverableService;
    }

    @Operation(summary = "Get all deliverables for a project")
    @GetMapping("")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DeliverableResponse>>> getProjectDeliverables(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<DeliverableResponse> deliverables = deliverableService.getProjectDeliverables(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(deliverables, "Project deliverables retrieved successfully"));
    }

    @Operation(summary = "Create a deliverable", description = "Create a new deliverable for a project (Team Leader only)")
    @PostMapping("")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<DeliverableResponse>> createDeliverable(
            @PathVariable Long projectId,
            @Valid @RequestBody DeliverableRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        DeliverableResponse deliverable = deliverableService.createDeliverable(projectId, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(deliverable, "Deliverable created successfully"));
    }

    @Operation(summary = "Get pending deliverables for a project")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DeliverableResponse>>> getPendingDeliverables(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<DeliverableResponse> pending = deliverableService.getPendingDeliverablesByProject(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(pending, "Pending deliverables retrieved successfully"));
    }

    @Operation(summary = "Get overdue deliverables for a project")
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<DeliverableResponse>>> getOverdueDeliverables(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<DeliverableResponse> overdue = deliverableService.getOverdueDeliverablesByProject(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(overdue, "Overdue deliverables retrieved successfully"));
    }

    @Operation(summary = "Get deliverable statistics for a project")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR')")
    public ResponseEntity<ApiResponse<DeliverableStatsResponse>> getDeliverableStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        DeliverableStatsResponse stats = deliverableService.getDeliverableStats(projectId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Deliverable statistics retrieved successfully"));
    }
}
