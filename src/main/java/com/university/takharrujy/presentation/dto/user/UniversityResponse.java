package com.university.takharrujy.presentation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * University response DTO
 */
@Schema(description = "University information")
public record UniversityResponse(
    
    @Schema(description = "University ID")
    Long id,
    
    @Schema(description = "University name")
    String name,
    
    @Schema(description = "University name in Arabic")
    String nameAr,
    
    @Schema(description = "University domain")
    String domain,
    
    @Schema(description = "Contact email")
    String contactEmail,
    
    @Schema(description = "Phone number")
    String phone,
    
    @Schema(description = "Address")
    String address,
    
    @Schema(description = "Address in Arabic")
    String addressAr,
    
    @Schema(description = "Whether university is active")
    Boolean isActive
) {}