package com.university.takharrujy.infrastructure.config;

import com.university.takharrujy.domain.enums.ProjectStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for ProjectStatus enum to handle PostgreSQL enum type
 */
@Converter(autoApply = true)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return ProjectStatus.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            // Handle case where database has invalid enum value
            return ProjectStatus.DRAFT; // Default fallback
        }
    }
}
