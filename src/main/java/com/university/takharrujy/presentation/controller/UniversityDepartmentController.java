package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.DepartmentService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.department.DepartmentCreateRequest;
import com.university.takharrujy.presentation.dto.department.DepartmentUpdateRequest;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/universities")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - University Departments", description = "Admin endpoints for managing departments within universities")
@SecurityRequirement(name = "bearerAuth")
public class UniversityDepartmentController {

    private final DepartmentService departmentService;

    public UniversityDepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Create department", description = "Create a new department in a university")
    @PostMapping("/{universityId}/departments")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @PathVariable Long universityId,
            @Valid @RequestBody DepartmentCreateRequest request) {

        DepartmentResponse response = departmentService.createDepartment(universityId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Department created successfully"));
    }

    @Operation(summary = "Update department", description = "Update an existing department in a university")
    @PutMapping("/{universityId}/departments/{deptId}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long universityId,
            @PathVariable Long deptId,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        DepartmentResponse response = departmentService.updateDepartment(universityId, deptId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Department updated successfully"));
    }

    @Operation(summary = "Delete department", description = "Soft delete a department in a university")
    @DeleteMapping("/{universityId}/departments/{deptId}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @PathVariable Long universityId,
            @PathVariable Long deptId) {
        departmentService.deleteDepartment(universityId, deptId);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }
}
