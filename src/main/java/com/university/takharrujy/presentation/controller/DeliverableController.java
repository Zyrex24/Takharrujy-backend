package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.DeliverableService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableResponse;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableSubmissionRequest;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableUpdateRequest;
import com.university.takharrujy.presentation.dto.supervisor.ApprovalRequest;
import com.university.takharrujy.presentation.dto.supervisor.FeedbackRequest;
import com.university.takharrujy.presentation.dto.supervisor.FeedbackResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliverables")
@Tag(name = "Deliverables", description = "Endpoints for managing individual project deliverables")
@SecurityRequirement(name = "bearerAuth")
public class DeliverableController {

    private final DeliverableService deliverableService;

    public DeliverableController(DeliverableService deliverableService) {
        this.deliverableService = deliverableService;
    }

    @Operation(summary = "Get deliverable details", description = "Fetch details of a specific deliverable by ID")
    @GetMapping("/{deliverableId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<DeliverableResponse>> getDeliverable(
            @PathVariable Long deliverableId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        DeliverableResponse deliverable = deliverableService.getDeliverable(deliverableId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(deliverable, "Deliverable details retrieved successfully"));
    }

    @Operation(summary = "Update a deliverable", description = "Update a deliverable (Team Leader only)")
    @PutMapping("/{deliverableId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<DeliverableResponse>> updateDeliverable(
            @PathVariable Long deliverableId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody DeliverableUpdateRequest request
    ) {
        DeliverableResponse updated = deliverableService.updateDeliverable(deliverableId, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Deliverable updated successfully"));
    }

    @Operation(summary = "Delete a deliverable", description = "Delete a deliverable (Team Leader only)")
    @DeleteMapping("/{deliverableId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<String>> deleteDeliverable(
            @PathVariable Long deliverableId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        deliverableService.deleteDeliverable(deliverableId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Deliverable deleted successfully"));
    }

    @Operation(summary = "Submit deliverable", description = "Submit a deliverable (Team Leader only)")
    @PostMapping("/{deliverableId}/submit")
    @PreAuthorize("hasRole('STUDENT')") // Checked inside service for leader
    public ResponseEntity<ApiResponse<DeliverableResponse>> submitDeliverable(
            @PathVariable Long deliverableId,
            @Valid @RequestBody DeliverableSubmissionRequest request,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        DeliverableResponse submitted = deliverableService.submitDeliverable(deliverableId, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(submitted, "Deliverable submitted successfully"));
    }

    @Operation(summary = "Provide feedback", description = "Supervisor provides feedback on a deliverable")
    @PostMapping("/{deliverableId}/feedback")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<DeliverableResponse>> submitFeedback(
            @PathVariable Long deliverableId,
            @Valid @RequestBody FeedbackRequest request
    ) {
        DeliverableResponse feedback = deliverableService.provideFeedback(deliverableId, request);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback submitted successfully"));
    }

    @Operation(summary = "Get deliverable feedback", description = "Get feedback for a specific deliverable")
    @GetMapping("/{deliverableId}/feedback")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @PathVariable Long deliverableId
    ) {
        FeedbackResponse feedback = deliverableService.getFeedback(deliverableId);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback retrieved successfully"));
    }

    @Operation(summary = "Approve deliverable", description = "Supervisor approves a deliverable")
    @PutMapping("/{deliverableId}/approve")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<DeliverableResponse>> approveDeliverable(
            @PathVariable Long deliverableId,
            @Valid @RequestBody ApprovalRequest request
    ) {
        DeliverableResponse approved = deliverableService.approveDeliverable(deliverableId, request);
        return ResponseEntity.ok(ApiResponse.success(approved, "Deliverable approved successfully"));
    }
}
