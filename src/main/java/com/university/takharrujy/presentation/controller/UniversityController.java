package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.UniversityService;
import com.university.takharrujy.presentation.dto.university.UniversityCreateRequest;
import com.university.takharrujy.presentation.dto.university.UniversityUpdateRequest;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import com.university.takharrujy.presentation.dto.user.UniversityResponse;
import com.university.takharrujy.presentation.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/universities")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Universities", description = "Admin operations for managing universities and departments")
@SecurityRequirement(name = "bearerAuth")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @Operation(summary = "Get all universities", description = "Retrieve all universities in the system")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UniversityResponse>>> getAllUniversities() {
        List<UniversityResponse> responses = universityService.getAllUniversities();
        return ResponseEntity.ok(ApiResponse.success(responses, "Universities retrieved successfully"));
    }

    @Operation(summary = "Create university", description = "Create a new university")
    @PostMapping
    public ResponseEntity<ApiResponse<UniversityResponse>> createUniversity(
            @Valid @RequestBody UniversityCreateRequest request,
            @AuthenticationPrincipal(expression = "userId") Long adminId) {

        UniversityResponse response = universityService.createUniversity(request, adminId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "University created successfully"));
    }

    @Operation(summary = "Get university details", description = "Retrieve details of a specific university by its ID")
    @GetMapping("/{universityId}")
    public ResponseEntity<ApiResponse<UniversityResponse>> getUniversity(
            @PathVariable Long universityId) {

        UniversityResponse response = universityService.getUniversityById(universityId);
        return ResponseEntity.ok(ApiResponse.success(response, "University details retrieved successfully"));
    }

    @Operation(summary = "Update university", description = "Update an existing university's information")
    @PutMapping("/{universityId}")
    public ResponseEntity<ApiResponse<UniversityResponse>> updateUniversity(
            @PathVariable Long universityId,
            @Valid @RequestBody UniversityUpdateRequest request) {

        UniversityResponse response = universityService.updateUniversity(universityId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "University updated successfully"));
    }

    @Operation(summary = "Get university departments", description = "Retrieve all departments for a given university")
    @GetMapping("/{universityId}/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments(
            @PathVariable Long universityId) {

        List<DepartmentResponse> departments = universityService.getUniversityDepartments(universityId);
        return ResponseEntity.ok(ApiResponse.success(departments, "Departments retrieved successfully"));
    }
}
