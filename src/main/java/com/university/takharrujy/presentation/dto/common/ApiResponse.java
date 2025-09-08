package com.university.takharrujy.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API Response wrapper
 * Provides consistent response format for all API endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
    
    @Schema(description = "Indicates if the request was successful")
    boolean success,
    
    @Schema(description = "Response message in English")
    String message,
    
    @Schema(description = "Response message in Arabic")
    String messageAr,
    
    @Schema(description = "Response data")
    T data,
    
    @Schema(description = "Error details (only present when success is false)")
    ErrorDetails error,
    
    @Schema(description = "Response timestamp")
    Instant timestamp
) {
    
    /**
     * Create successful response with data and bilingual messages
     */
    public static <T> ApiResponse<T> success(T data, String message, String messageAr) {
        return new ApiResponse<>(true, message, messageAr, data, null, Instant.now());
    }
    
    /**
     * Create successful response with data and English message only
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, null, data, null, Instant.now());
    }
    
    /**
     * Create successful response with data only
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", "نجح", data, null, Instant.now());
    }
    
    /**
     * Create successful response without data
     */
    public static <T> ApiResponse<T> success(String message, String messageAr) {
        return new ApiResponse<>(true, message, messageAr, null, null, Instant.now());
    }
    
    /**
     * Create error response with bilingual messages
     */
    public static <T> ApiResponse<T> error(String message, String messageAr, ErrorDetails error) {
        return new ApiResponse<>(false, message, messageAr, null, error, Instant.now());
    }
    
    /**
     * Create error response with English message only
     */
    public static <T> ApiResponse<T> error(String message, ErrorDetails error) {
        return new ApiResponse<>(false, message, null, null, error, Instant.now());
    }
    
    /**
     * Create error response with error code and message
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ErrorDetails error = new ErrorDetails(errorCode, message, null, null);
        return new ApiResponse<>(false, message, null, null, error, Instant.now());
    }
}