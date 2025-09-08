package com.university.takharrujy.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Error Details
 * Provides detailed error information in API responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error details for failed API requests")
public record ErrorDetails(
    
    @Schema(description = "Error code")
    String code,
    
    @Schema(description = "Error message")
    String message,
    
    @Schema(description = "Error message in Arabic")
    String messageAr,
    
    @Schema(description = "Additional error details")
    String details,
    
    @Schema(description = "Field-specific validation errors")
    Map<String, String> fieldErrors,
    
    @Schema(description = "List of error messages")
    List<String> errors,
    
    @Schema(description = "Request path that caused the error")
    String path,
    
    @Schema(description = "HTTP status code")
    Integer status
) {
    
    /**
     * Create error details with code and message
     */
    public static ErrorDetails of(String code, String message) {
        return new ErrorDetails(code, message, null, null, null, null, null, null);
    }
    
    /**
     * Create error details with bilingual messages
     */
    public static ErrorDetails of(String code, String message, String messageAr) {
        return new ErrorDetails(code, message, messageAr, null, null, null, null, null);
    }
    
    /**
     * Create validation error with field errors
     */
    public static ErrorDetails validation(String message, Map<String, String> fieldErrors) {
        return new ErrorDetails("VALIDATION_ERROR", message, null, null, fieldErrors, null, null, 400);
    }
    
    /**
     * Create error with additional details
     */
    public static ErrorDetails withDetails(String code, String message, String details) {
        return new ErrorDetails(code, message, null, details, null, null, null, null);
    }
}