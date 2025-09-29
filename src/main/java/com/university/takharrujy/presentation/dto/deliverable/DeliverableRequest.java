package com.university.takharrujy.presentation.dto.deliverable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request body for creating a deliverable")
public record DeliverableRequest(
        @NotBlank
        @Size(max = 255)
        @Schema(description = "Title of the deliverable", example = "Final Report")
        String title,

        @Size(max = 2000)
        @Schema(description = "Description of the deliverable", example = "Comprehensive final report for the graduation project")
        String description,

        @Schema(description = "Deadline for this deliverable", example = "2025-10-15T23:59:59Z")
        Instant dueDate
) {}
