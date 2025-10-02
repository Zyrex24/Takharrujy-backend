package com.university.takharrujy.presentation.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload to assign a supervisor to a project")
public record SupervisorAssignmentRequest(

        @Schema(description = "ID of the supervisor", example = "2")
        Long supervisorId
) {}
