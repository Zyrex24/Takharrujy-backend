package com.university.takharrujy.presentation.dto.deliverable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DeliverableStatsResponse", description = "Statistics for project deliverables")
public record DeliverableStatsResponse(

        @Schema(description = "Total number of deliverables", example = "10")
        long total,

        @Schema(description = "Number of pending deliverables", example = "4")
        long pending,

        @Schema(description = "Number of submitted deliverables", example = "3")
        long submitted,

        @Schema(description = "Number of approved deliverables", example = "2")
        long approved,

        @Schema(description = "Number of rejected deliverables", example = "1")
        long rejected,

        @Schema(description = "Number of overdue deliverables", example = "2")
        long overdue
) {}
