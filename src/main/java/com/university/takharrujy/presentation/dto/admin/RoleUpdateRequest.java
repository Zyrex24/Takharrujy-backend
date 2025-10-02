package com.university.takharrujy.presentation.dto.admin;

import com.university.takharrujy.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object to update a user's role")
public record RoleUpdateRequest(

        @Schema(description = "New role for the user", example = "SUPERVISOR")
        UserRole role
) {}
