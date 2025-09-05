package com.university.takharrujy.presentation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Department response DTO
 */
@Schema(description = "Department information")
public record DepartmentResponse(
    
    @Schema(description = "Department ID")
    Long id,
    
    @Schema(description = "Department name")
    String name,
    
    @Schema(description = "Department name in Arabic")
    String nameAr,
    
    @Schema(description = "Department code")
    String code,
    
    @Schema(description = "Department description")
    String description,
    
    @Schema(description = "Department description in Arabic")
    String descriptionAr,
    
    @Schema(description = "Whether department is active")
    Boolean isActive
) {}