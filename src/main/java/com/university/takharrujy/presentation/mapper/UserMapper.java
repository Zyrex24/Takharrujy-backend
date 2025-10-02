package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.entity.UserPreferences;
import com.university.takharrujy.presentation.dto.user.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * User Mapper
 * Maps User entities to DTOs with proper null handling
 */
@Component
public class UserMapper {
    private final UniversityMapper universityMapper;
    private final DepartmentMapper departmentMapper;

    public UserMapper(UniversityMapper universityMapper, DepartmentMapper departmentMapper) {
        this.universityMapper = universityMapper;
        this.departmentMapper = departmentMapper;
    }

    public UserResponse toResponse(User user) {
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
            universityMapper.toUniversityResponse(user.getUniversity()),
            departmentMapper.toDepartmentResponse(user.getDepartment()),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    // For backward compatibility
    public UserResponse toUserResponse(User user) {
        return toResponse(user);
    }
    
    /**
     * Map UserPreferences to UserPreferencesResponse
     */
    public UserPreferencesResponse toPreferencesResponse(UserPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        
        return new UserPreferencesResponse(
            preferences.getUser().getId(),
            preferences.getEmailNotifications(),
            preferences.getPushNotifications(),
            preferences.getSmsNotifications(),
            preferences.getProjectUpdates(),
            preferences.getTaskAssignments(),
            preferences.getTaskDueReminders(),
            preferences.getProjectInvitations(),
            preferences.getNewMessages(),
            preferences.getMessageMentions(),
            preferences.getSubmissionNotifications(),
            preferences.getProgressReports(),
            preferences.getTheme(),
            preferences.getLanguage(),
            preferences.getTimezone(),
            preferences.getProfileVisibility(),
            preferences.getShowEmail(),
            preferences.getShowPhone()
        );
    }
    
    /**
     * Map UserActivity to UserActivityResponse
     */
    public UserActivityResponse toActivityResponse(UserActivity activity) {
        if (activity == null) {
            return null;
        }
        
        return new UserActivityResponse(
            activity.getId(),
            activity.getActivityType(),
            activity.getDescription(),
            activity.getDescriptionAr(),
            activity.getResourceType(),
            activity.getResourceId(),
            activity.getIpAddress(),
            activity.getCreatedAt(),
            activity.getAdditionalData()
        );
    }
}