package com.university.takharrujy.presentation.dto.deliverable;

import com.university.takharrujy.domain.enums.DeliverableStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "DeliverableResponse", description = "Response DTO for project deliverables")
public record DeliverableResponse(

        @Schema(description = "Unique identifier of the deliverable", example = "101")
        Long id,

        @Schema(description = "Title of the deliverable", example = "Final Report")
        String title,

        @Schema(description = "Detailed description of the deliverable", example = "Comprehensive final report for the graduation project.")
        String description,

        @Schema(description = "Current status of the deliverable", example = "PENDING", allowableValues = {"PENDING", "SUBMITTED", "REVIEWED", "APPROVED", "REJECTED"})
        DeliverableStatus status,

        @Schema(description = "Deadline for this deliverable", example = "2025-10-15T23:59:59Z")
        Instant dueDate,

        @Schema(description = "Submission timestamp if already submitted", example = "2025-09-20T12:30:00Z")
        Instant submittedAt,

        @Schema(description = "ID of the associated project", example = "5")
        Long projectId,

        @Schema(description = "User who created this deliverable", example = "teamleader@example.com")
        String createdBy,

        @Schema(description = "Supervisor feedback in English", example = "Good work on the report")
        String supervisorFeedback,

        @Schema(description = "Supervisor feedback in Arabic", example = "عمل جيد على التقرير")
        String supervisorFeedbackAr,

        @Schema(description = "Submission notes or comments", example = "Final version with supervisor feedback applied")
        String submissionNotes,

        @Schema(description = "URL of the submitted file", example = "https://files.takharrujy.com/deliverables/101.pdf")
        String submissionFileUrl,

        @Schema(description = "Creation timestamp", example = "2025-09-01T08:15:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2025-09-15T14:20:00Z")
        Instant updatedAt
) {}
