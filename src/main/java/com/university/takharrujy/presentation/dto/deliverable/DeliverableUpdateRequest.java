package com.university.takharrujy.presentation.dto.deliverable;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "DeliverableUpdateRequest", description = "Request payload to update a deliverable")
public record DeliverableUpdateRequest(

        @Schema(description = "Title of the deliverable", example = "Updated Final Report")
        String title,

        @Schema(description = "Detailed description of the deliverable", example = "Updated comprehensive final report")
        String description,

        @Schema(description = "Deadline for the deliverable", example = "2025-10-30T23:59:59Z")
        Instant dueDate
) {}
