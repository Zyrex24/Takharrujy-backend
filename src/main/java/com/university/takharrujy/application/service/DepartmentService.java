package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Department;
import com.university.takharrujy.domain.entity.University;
import com.university.takharrujy.domain.repository.DepartmentRepository;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.department.DepartmentCreateRequest;
import com.university.takharrujy.presentation.dto.department.DepartmentUpdateRequest;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import com.university.takharrujy.presentation.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(UniversityRepository universityRepository,
                             DepartmentRepository departmentRepository,
                             DepartmentMapper departmentMapper) {
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Create a new department inside a university
     */
    @Transactional
    public DepartmentResponse createDepartment(Long universityId, DepartmentCreateRequest request) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with ID: " + universityId));

        // Validate code uniqueness inside the same university
        if (departmentRepository.existsByCodeAndUniversityId(request.code(), universityId)) {
            throw BusinessException.duplicateResource("Department code already exists in this university");
        }

        Department department = new Department();
        department.setName(request.name());
        department.setNameAr(request.nameAr());
        department.setCode(request.code().toUpperCase().trim()); // normalize code
        department.setDescription(request.description());
        department.setDescriptionAr(request.descriptionAr());
        department.setIsActive(request.isActive() != null ? request.isActive() : true); // default true
        department.setUniversity(university);
        department.setUniversityId(universityId);

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(saved);
    }

    /**
     * Update an existing department
     */
    @Transactional
    public DepartmentResponse updateDepartment(Long universityId, Long deptId, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findByIdAndUniversityId(deptId, universityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID " + deptId + " in university " + universityId));

        // Validate duplicate code (only if code is changing)
        if (request.code() != null &&
                !request.code().equalsIgnoreCase(department.getCode()) &&
                departmentRepository.existsByCodeAndUniversityId(request.code(), universityId)) {
            throw BusinessException.duplicateResource("Another department with this code already exists in the university");
        }

        // Partial update: only apply non-null / non-blank values
        if (request.name() != null && !request.name().isBlank()) {
            department.setName(request.name());
        }
        if (request.nameAr() != null && !request.nameAr().isBlank()) {
            department.setNameAr(request.nameAr());
        }
        if (request.code() != null && !request.code().isBlank()) {
            department.setCode(request.code().toUpperCase().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            department.setDescription(request.description());
        }
        if (request.descriptionAr() != null && !request.descriptionAr().isBlank()) {
            department.setDescriptionAr(request.descriptionAr());
        }
        if (request.isActive() != null) {
            department.setIsActive(request.isActive());
        }

        Department updated = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(updated);
    }

    /**
     * Soft delete a department (mark inactive)
     */
    @Transactional
    public void deleteDepartment(Long universityId, Long deptId) {
        Department department = departmentRepository.findByIdAndUniversityId(deptId, universityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + deptId + " in University ID: " + universityId));

        if (!department.getIsActive()) {
            throw BusinessException.invalidInput("Department is already inactive");
        }

        department.setIsActive(false);
        departmentRepository.save(department);
    }
}
