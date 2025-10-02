package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Department;
import com.university.takharrujy.domain.entity.University;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.NotificationType;
import com.university.takharrujy.domain.repository.DepartmentRepository;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.university.UniversityCreateRequest;
import com.university.takharrujy.presentation.dto.university.UniversityUpdateRequest;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import com.university.takharrujy.presentation.dto.user.UniversityResponse;
import com.university.takharrujy.presentation.mapper.DepartmentMapper;
import com.university.takharrujy.presentation.mapper.UniversityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final NotificationService notificationService;

    public UniversityService(UniversityRepository universityRepository,
                             UniversityMapper universityMapper,
                             UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             DepartmentMapper departmentMapper, NotificationService notificationService) {
        this.universityRepository = universityRepository;
        this.universityMapper = universityMapper;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.notificationService = notificationService;
    }

    /* ---------------------- CREATE ---------------------- */

    @Transactional
    public UniversityResponse createUniversity(UniversityCreateRequest request, Long adminId) {
        validateUniversityCreateRequest(request);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // Prevent duplicate by domain
        if (universityRepository.existsByDomainIgnoreCase(request.domain())) {
            throw BusinessException.resourceInUse("University with domain " + request.domain() + " already exists");
        }

        University university = new University();
        university.setName(request.name());
        university.setNameAr(request.nameAr());
        university.setDomain(request.domain().toLowerCase());
        university.setContactEmail(request.contactEmail());
        university.setPhone(request.phone());
        university.setAddress(request.address());
        university.setAddressAr(request.addressAr());
        university.setIsActive(true);
        university.setCreatedBy(admin.getEmail());

        University saved = universityRepository.save(university);

        return universityMapper.toUniversityResponse(saved);
    }

    /* ---------------------- READ ---------------------- */

    @Transactional(readOnly = true)
    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAll().stream()
                .map(universityMapper::toUniversityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UniversityResponse getUniversityById(Long universityId) {
        University university = findActiveUniversityById(universityId);
        return universityMapper.toUniversityResponse(university);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getUniversityDepartments(Long universityId) {
        University university = findActiveUniversityById(universityId);
        List<Department> departments = departmentRepository.findByUniversityIdAndIsActiveTrue(university.getId());
        return departments.stream()
                .map(departmentMapper::toDepartmentResponse)
                .toList();
    }

    /* ---------------------- UPDATE ---------------------- */

    @Transactional
    public UniversityResponse updateUniversity(Long universityId, UniversityUpdateRequest request) {
        University university = findActiveUniversityById(universityId);

        validateUniversityUpdateRequest(request, university);

        // Prevent duplicate domain if changed
        if (!university.getDomain().equalsIgnoreCase(request.domain())
                && universityRepository.existsByDomainIgnoreCase(request.domain())) {
            throw BusinessException.resourceInUse("University with domain " + request.domain() + " already exists");
        }

        if (request.name() != null && !request.name().isBlank()) {
            university.setName(request.name());
        }

        if (request.nameAr() != null && !request.nameAr().isBlank()) {
            university.setNameAr(request.nameAr());
        }

        if (request.domain() != null && !request.domain().isBlank()) {
            university.setDomain(request.domain().toLowerCase());
        }

        if (request.contactEmail() != null && !request.contactEmail().isBlank()) {
            university.setContactEmail(request.contactEmail());
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            university.setPhone(request.phone());
        }

        if (request.address() != null && !request.address().isBlank()) {
            university.setAddress(request.address());
        }

        if (request.addressAr() != null && !request.addressAr().isBlank()) {
            university.setAddressAr(request.addressAr());
        }

        if (request.isActive() != null) {
            university.setIsActive(request.isActive());
        }


        University updated = universityRepository.save(university);

        return universityMapper.toUniversityResponse(updated);
    }

    /* ---------------------- DELETE ---------------------- */

    @Transactional
    public void deleteUniversity(Long universityId) {
        University university = findActiveUniversityById(universityId);

        // Soft delete
        university.setIsActive(false);
        universityRepository.save(university);
    }

    /* ---------------------- HELPERS ---------------------- */

    private University findActiveUniversityById(Long id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found with ID: " + id));

        if (Boolean.FALSE.equals(university.getIsActive())) {
            throw BusinessException.invalidInput("University with ID " + id + " is inactive");
        }
        return university;
    }

    private void validateUniversityCreateRequest(UniversityCreateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw BusinessException.invalidInput("University name is required");
        }
        if (request.domain() == null || request.domain().isBlank()) {
            throw BusinessException.invalidInput("University domain is required");
        }
        if (!request.domain().contains(".")) {
            throw BusinessException.invalidInput("Invalid domain format: " + request.domain());
        }
    }

    private void validateUniversityUpdateRequest(UniversityUpdateRequest request, University existing) {
        if (request.name() == null || request.name().isBlank()) {
            throw BusinessException.invalidInput("University name cannot be empty");
        }
        if (request.domain() == null || request.domain().isBlank()) {
            throw BusinessException.invalidInput("University domain cannot be empty");
        }
    }
}
