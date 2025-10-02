package com.university.takharrujy.presentation.dto.supervisor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Approval request payload")
public record ApprovalRequest(

        @Schema(description = "Whether the project or deliverable is approved", example = "true")
        boolean approved
) {}
