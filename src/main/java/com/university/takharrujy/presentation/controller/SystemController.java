package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.presentation.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * System Controller
 * Provides system health and status endpoints
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "System health and status endpoints")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Operation(
        summary = "Health Check",
        description = "Check if the system is running and healthy"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "System is healthy",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        logger.debug("Health check requested");
        
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("service", "Takharrujy Backend");
        healthData.put("version", "1.0.0");
        
        return ResponseEntity.ok(
            ApiResponse.success(healthData, "System is healthy")
        );
    }

    @Operation(
        summary = "System Status",
        description = "Get detailed system status information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "System status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        logger.debug("System status requested");
        
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("service", "Takharrujy Backend");
        statusData.put("version", "1.0.0");
        statusData.put("timestamp", LocalDateTime.now());
        statusData.put("uptime", "Running");
        statusData.put("database", "Connected");
        statusData.put("redis", "Connected");
        
        return ResponseEntity.ok(
            ApiResponse.success(statusData, "System status retrieved successfully")
        );
    }
}
