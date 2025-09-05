package com.university.takharrujy.presentation.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API response wrapper for all REST endpoints
 * Provides consistent response format across the application
 * 
 * @param <T> The type of data being returned
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
    
    @Schema(description = "Whether the request was successful", example = "true")
    boolean success,
    
    @Schema(description = "Response message", example = "Operation completed successfully")
    String message,
    
    @Schema(description = "Response data")
    T data,
    
    @Schema(description = "Error details if request failed")
    ErrorDetails error,
    
    @Schema(description = "Response timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant timestamp
) {
    
    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }
    
    /**
     * Create a successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, Instant.now());
    }
    
    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String message, ErrorDetails error) {
        return new ApiResponse<>(false, message, null, error, Instant.now());
    }
    
    /**
     * Create an error response with simple message
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }
}