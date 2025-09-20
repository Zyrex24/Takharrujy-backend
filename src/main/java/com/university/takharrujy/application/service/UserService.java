package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.entity.UserPreferences;
import com.university.takharrujy.domain.enums.UserActivityType;
import com.university.takharrujy.domain.repository.UserActivityRepository;
import com.university.takharrujy.domain.repository.UserPreferencesRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.presentation.dto.user.*;
import com.university.takharrujy.presentation.mapper.UserMapper;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.infrastructure.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * User Service
 * Handles user-related business logic and operations
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserPreferencesRepository userPreferencesRepository;
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ActivityLoggingService activityLoggingService;
    
    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(Long userId) {
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateProfile(Long userId, UserUpdateRequest request, HttpServletRequest httpRequest) {
        User user = findUserById(userId);
        
        if (!request.hasUpdates()) {
            throw new ValidationException("No updates provided");
        }
        
        // Update basic information
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.firstNameAr() != null) {
            user.setFirstNameAr(request.firstNameAr());
        }
        if (request.lastNameAr() != null) {
            user.setLastNameAr(request.lastNameAr());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.bioAr() != null) {
            user.setBioAr(request.bioAr());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        
        User savedUser = userRepository.save(user);
        
        // Log activity
        activityLoggingService.logActivity(
            user, 
            UserActivityType.PROFILE_UPDATE, 
            "Profile updated", 
            "تم تحديث الملف الشخصي", 
            httpRequest
        );
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        if (!request.passwordsMatch()) {
            throw new ValidationException("New password and confirmation do not match");
        }
        
        User user = findUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        // Log activity
        activityLoggingService.logActivity(
            user, 
            UserActivityType.PASSWORD_CHANGE, 
            "Password changed", 
            "تم تغيير كلمة المرور", 
            httpRequest
        );
    }
    
    /**
     * Upload user avatar
     */
    public UserResponse uploadAvatar(Long userId, MultipartFile file, HttpServletRequest httpRequest) {
        User user = findUserById(userId);
        
        // Validate file
        validateAvatarFile(file);
        
        // Upload file and get URL
        String avatarUrl = fileStorageService.uploadAvatar(file, userId);
        
        // Update user profile picture URL
        user.setProfilePictureUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        
        // Log activity
        activityLoggingService.logActivity(
            user, 
            UserActivityType.AVATAR_UPLOAD, 
            "Avatar uploaded", 
            "تم رفع صورة شخصية", 
            httpRequest
        );
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get user preferences
     */
    @Transactional(readOnly = true)
    public UserPreferencesResponse getUserPreferences(Long userId) {
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
        
        return userMapper.toPreferencesResponse(preferences);
    }
    
    /**
     * Update user preferences
     */
    public UserPreferencesResponse updatePreferences(Long userId, UserPreferencesUpdateRequest request, HttpServletRequest httpRequest) {
        UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
        
        // Update notification preferences
        if (request.emailNotifications() != null) {
            preferences.setEmailNotifications(request.emailNotifications());
        }
        if (request.pushNotifications() != null) {
            preferences.setPushNotifications(request.pushNotifications());
        }
        if (request.smsNotifications() != null) {
            preferences.setSmsNotifications(request.smsNotifications());
        }
        if (request.projectUpdates() != null) {
            preferences.setProjectUpdates(request.projectUpdates());
        }
        if (request.taskAssignments() != null) {
            preferences.setTaskAssignments(request.taskAssignments());
        }
        if (request.taskDueReminders() != null) {
            preferences.setTaskDueReminders(request.taskDueReminders());
        }
        if (request.projectInvitations() != null) {
            preferences.setProjectInvitations(request.projectInvitations());
        }
        if (request.newMessages() != null) {
            preferences.setNewMessages(request.newMessages());
        }
        if (request.messageMentions() != null) {
            preferences.setMessageMentions(request.messageMentions());
        }
        if (request.submissionNotifications() != null) {
            preferences.setSubmissionNotifications(request.submissionNotifications());
        }
        if (request.progressReports() != null) {
            preferences.setProgressReports(request.progressReports());
        }
        
        // Update UI preferences
        if (request.theme() != null) {
            preferences.setTheme(request.theme());
        }
        if (request.language() != null) {
            preferences.setLanguage(request.language());
        }
        if (request.timezone() != null) {
            preferences.setTimezone(request.timezone());
        }
        
        // Update privacy preferences
        if (request.profileVisibility() != null) {
            preferences.setProfileVisibility(request.profileVisibility());
        }
        if (request.showEmail() != null) {
            preferences.setShowEmail(request.showEmail());
        }
        if (request.showPhone() != null) {
            preferences.setShowPhone(request.showPhone());
        }
        
        UserPreferences saved = userPreferencesRepository.save(preferences);
        
        // Log activity
        User user = findUserById(userId);
        activityLoggingService.logActivity(
            user, 
            UserActivityType.PREFERENCES_UPDATE, 
            "Preferences updated", 
            "تم تحديث التفضيلات", 
            httpRequest
        );
        
        return userMapper.toPreferencesResponse(saved);
    }
    
    /**
     * Get user activity history
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivity(Long userId, Pageable pageable) {
        Page<UserActivity> activities = userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return activities.map(userMapper::toActivityResponse);
    }
    
    /**
     * Get recent user activities (last 24 hours)
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getRecentActivity(Long userId, Pageable pageable) {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        Page<UserActivity> activities = userActivityRepository.findByUserIdAndDateRange(
            userId, since, Instant.now(), pageable);
        return activities.map(userMapper::toActivityResponse);
    }
    
    /**
     * Helper method to find user by ID
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    
    /**
     * Create default preferences for a user
     */
    private UserPreferences createDefaultPreferences(Long userId) {
        User user = findUserById(userId);
        UserPreferences preferences = new UserPreferences(user, user.getUniversityId());
        return userPreferencesRepository.save(preferences);
    }
    
    /**
     * Validate avatar file
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Avatar file is required");
        }
        
        // Check file size (5MB limit)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidationException("Avatar file size cannot exceed 5MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("Avatar must be an image file");
        }
        
        // Check supported formats
        if (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/png") && 
            !contentType.equals("image/webp")) {
            throw new ValidationException("Avatar must be in JPEG, PNG, or WebP format");
        }
    }
}
