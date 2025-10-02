package com.university.takharrujy.presentation.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload to update user status")
public record StatusUpdateRequest(
        @Schema(description = "Whether the user account should be active", example = "true")
        boolean isActive
) {}
