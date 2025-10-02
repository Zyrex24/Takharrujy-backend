package com.university.takharrujy.presentation.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object for creating a new department")
public record DepartmentCreateRequest(

        @NotBlank(message = "Department name is required")
        @Schema(description = "Name of the department", example = "Computer Science")
        String name,

        @NotBlank(message = "Department name is required")
        @Schema(description = "Department name in Arabic", example = "علوم الحاسب")
        String nameAr,

        @Schema(description = "Department code", example = "CS")
        String code,

        @Schema(description = "Optional description of the department", example = "Department focusing on AI, ML, and Systems")
        String description,

        @Schema(description = "Department description in Arabic")
        String descriptionAr,

        @Schema(description = "Whether department is active")
        Boolean isActive
) {}
