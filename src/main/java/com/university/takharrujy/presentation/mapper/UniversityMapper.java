package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.University;
import com.university.takharrujy.presentation.dto.user.UniversityResponse;
import org.springframework.stereotype.Component;

@Component
public class UniversityMapper {

    public UniversityResponse toUniversityResponse(University university) {
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
}
