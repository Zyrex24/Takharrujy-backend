package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing supervisor feedback")
public record FeedbackResponse(

        @Schema(
                description = "ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Supervisor feedback in English",
                example = "The project scope is clear and well-defined."
        )
        String supervisorFeedback,

        @Schema(
                description = "Supervisor feedback in Arabic",
                example = "نطاق المشروع واضح ومحدد بشكل جيد."
        )
        String supervisorFeedbackAr
) {}
