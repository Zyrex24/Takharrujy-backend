package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Department;
import com.university.takharrujy.presentation.dto.user.DepartmentResponse;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {
    public DepartmentResponse toDepartmentResponse(Department department) {
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
