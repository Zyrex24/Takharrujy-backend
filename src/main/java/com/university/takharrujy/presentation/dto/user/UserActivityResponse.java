package com.university.takharrujy.presentation.dto.user;

import com.university.takharrujy.domain.enums.UserActivityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * User activity response DTO
 */
@Schema(description = "User activity response")
public record UserActivityResponse(
    
    @Schema(description = "Activity ID")
    Long id,
    
    @Schema(description = "Activity type")
    UserActivityType activityType,
    
    @Schema(description = "Activity description")
    String description,
    
    @Schema(description = "Activity description in Arabic")
    String descriptionAr,
    
    @Schema(description = "Resource type (project, task, file, etc.)")
    String resourceType,
    
    @Schema(description = "Resource ID")
    Long resourceId,
    
    @Schema(description = "IP address")
    String ipAddress,
    
    @Schema(description = "Activity timestamp")
    Instant timestamp,
    
    @Schema(description = "Additional data as JSON")
    String additionalData
) {
    
    /**
     * Get localized description based on language preference
     */
    public String getLocalizedDescription(String language) {
        if ("ar".equals(language) && descriptionAr != null && !descriptionAr.isEmpty()) {
            return descriptionAr;
        }
        return description;
    }
}