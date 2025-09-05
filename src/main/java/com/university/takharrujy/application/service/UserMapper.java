package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import com.university.takharrujy.presentation.dto.user.UniversityResponse;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import org.springframework.stereotype.Component;

/**
 * User Mapper
 * Maps User entities to DTOs with proper null handling
 */
@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getFirstNameAr(),
            user.getLastNameAr(),
            user.getRole(),
            user.getStudentId(),
            user.getPhone(),
            user.getDateOfBirth(),
            user.getIsActive(),
            user.getIsEmailVerified(),
            user.getProfilePictureUrl(),
            user.getBio(),
            user.getBioAr(),
            user.getPreferredLanguage(),
            toUniversityResponse(user.getUniversity()),
            toDepartmentResponse(user.getDepartment()),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private UniversityResponse toUniversityResponse(com.university.takharrujy.domain.entity.University university) {
        if (university == null) {
            return null;
        }

        return new UniversityResponse(
            university.getId(),
            university.getName(),
            university.getNameAr(),
            university.getDomain(),
            university.getContactEmail(),
            university.getPhone(),
            university.getAddress(),
            university.getAddressAr(),
            university.getIsActive()
        );
    }

    private DepartmentResponse toDepartmentResponse(com.university.takharrujy.domain.entity.Department department) {
        if (department == null) {
            return null;
        }

        return new DepartmentResponse(
            department.getId(),
            department.getName(),
            department.getNameAr(),
            department.getCode(),
            department.getDescription(),
            department.getDescriptionAr(),
            department.getIsActive()
        );
    }
}