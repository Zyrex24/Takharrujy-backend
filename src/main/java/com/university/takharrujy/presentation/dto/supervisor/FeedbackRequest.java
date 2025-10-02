package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for providing supervisor feedback on a project")
public record FeedbackRequest(

        @Schema(
                description = "Supervisor feedback in English",
                example = "The project scope is clear and well-defined."
        )
        @NotBlank
        @Size(max = 2000, message = "Supervisor feedback cannot exceed 2000 characters")
        String supervisorFeedback,

        @Schema(
                description = "Supervisor feedback in Arabic",
                example = "نطاق المشروع واضح ومحدد بشكل جيد."
        )
        @Size(max = 2000, message = "Arabic supervisor feedback cannot exceed 2000 characters")
        String supervisorFeedbackAr
) {}
