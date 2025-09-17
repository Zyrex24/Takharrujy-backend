package com.university.takharrujy.presentation.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Error details for API responses
 * Provides structured error information with validation details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error details for failed requests")
public record ErrorDetails(
    
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    String code,
    
    @Schema(description = "Error message", example = "Invalid input provided")
    String message,
    
    @Schema(description = "Field validation errors")
    Map<String, String> fieldErrors,
    
    @Schema(description = "Additional error details")
    List<String> details
) {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String code;
        private String message;
        private Map<String, String> fieldErrors;
        private List<String> details;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder fieldErrors(Map<String, String> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }
        
        public Builder details(List<String> details) {
            this.details = details;
            return this;
        }
        
        public ErrorDetails build() {
            return new ErrorDetails(code, message, fieldErrors, details);
        }
    }
}