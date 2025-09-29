package com.university.takharrujy.presentation.dto.deliverable;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for submitting a deliverable")
public record DeliverableSubmissionRequest(

        @Schema(description = "Submission notes or comments",
                example = "Final version with supervisor feedback applied")
        String notes,

        @Schema(description = "Optional file URL or reference ID",
                example = "https://files.takharrujy.com/deliverables/12345")
        String fileUrl
) {}
