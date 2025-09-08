package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.entity.UserPreferences;
import com.university.takharrujy.domain.enums.UserActivityType;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.UserActivityRepository;
import com.university.takharrujy.domain.repository.UserPreferencesRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.infrastructure.exception.ValidationException;
import com.university.takharrujy.presentation.dto.user.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserPreferencesRepository userPreferencesRepository;
    
    @Mock
    private UserActivityRepository userActivityRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @Mock
    private ActivityLoggingService activityLoggingService;
    
    @Mock
    private HttpServletRequest httpRequest;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserResponse testUserResponse;
    private UserPreferences testPreferences;
    
    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUserResponse = createTestUserResponse();
        testPreferences = createTestPreferences();
    }
    
    @Test
    @DisplayName("Should get current user profile successfully")
    void shouldGetCurrentUserProfileSuccessfully() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        
        // When
        UserResponse result = userService.getCurrentUserProfile(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testUser.getId());
        assertThat(result.email()).isEqualTo(testUser.getEmail());
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(testUser);
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUserProfile(userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with ID: " + userId);
    }
    
    @Test
    @DisplayName("Should update profile with Arabic names successfully")
    void shouldUpdateProfileWithArabicNamesSuccessfully() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
            "أحمد", "محمد", "Ahmed", "Mohamed",
            "+201234567890", LocalDate.of(1995, 5, 15),
            "Computer Science Student", "طالب علوم حاسوب",
            "ar", 1L
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        
        // When
        UserResponse result = userService.updateProfile(userId, request, httpRequest);
        
        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
        verify(activityLoggingService).logActivity(
            eq(testUser), 
            eq(UserActivityType.PROFILE_UPDATE), 
            eq("Profile updated"), 
            eq("تم تحديث الملف الشخصي"), 
            eq(httpRequest)
        );
        
        // Verify user properties were updated
        assertThat(testUser.getFirstName()).isEqualTo("أحمد");
        assertThat(testUser.getLastName()).isEqualTo("محمد");
        assertThat(testUser.getFirstNameAr()).isEqualTo("Ahmed");
        assertThat(testUser.getLastNameAr()).isEqualTo("Mohamed");
        assertThat(testUser.getPhone()).isEqualTo("+201234567890");
        assertThat(testUser.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(testUser.getBio()).isEqualTo("Computer Science Student");
        assertThat(testUser.getBioAr()).isEqualTo("طالب علوم حاسوب");
        assertThat(testUser.getPreferredLanguage()).isEqualTo("ar");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when no updates provided")
    void shouldThrowValidationExceptionWhenNoUpdatesProvided() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
            null, null, null, null, null, null, null, null, null, null
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(userId, request, httpRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("No updates provided");
    }
    
    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
            "currentPassword123",
            "newPassword456!",
            "newPassword456!"
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword123", testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newPassword456!")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.changePassword(userId, request, httpRequest);
        
        // Then
        verify(passwordEncoder).matches("currentPassword123", testUser.getPasswordHash());
        verify(passwordEncoder).encode("newPassword456!");
        verify(userRepository).save(testUser);
        verify(activityLoggingService).logActivity(
            eq(testUser),
            eq(UserActivityType.PASSWORD_CHANGE),
            eq("Password changed"),
            eq("تم تغيير كلمة المرور"),
            eq(httpRequest)
        );
        
        assertThat(testUser.getPasswordHash()).isEqualTo("hashedNewPassword");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when passwords don't match")
    void shouldThrowValidationExceptionWhenPasswordsDontMatch() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
            "currentPassword123",
            "newPassword456!",
            "differentPassword789!"
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        
        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, request, httpRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("New password and confirmation do not match");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when current password is incorrect")
    void shouldThrowValidationExceptionWhenCurrentPasswordIncorrect() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
            "wrongPassword",
            "newPassword456!",
            "newPassword456!"
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, request, httpRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Current password is incorrect");
    }
    
    @Test
    @DisplayName("Should upload avatar successfully")
    void shouldUploadAvatarSuccessfully() {
        // Given
        Long userId = 1L;
        MultipartFile file = new MockMultipartFile(
            "avatar", "avatar.jpg", "image/jpeg", "test image data".getBytes()
        );
        String avatarUrl = "/uploads/avatars/avatar_1_20240101_123456_abc12345.jpg";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(fileStorageService.uploadAvatar(file, userId)).thenReturn(avatarUrl);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        
        // When
        UserResponse result = userService.uploadAvatar(userId, file, httpRequest);
        
        // Then
        assertThat(result).isNotNull();
        verify(fileStorageService).uploadAvatar(file, userId);
        verify(userRepository).save(testUser);
        verify(activityLoggingService).logActivity(
            eq(testUser),
            eq(UserActivityType.AVATAR_UPLOAD),
            eq("Avatar uploaded"),
            eq("تم رفع صورة شخصية"),
            eq(httpRequest)
        );
        
        assertThat(testUser.getProfilePictureUrl()).isEqualTo(avatarUrl);
    }
    
    @Test
    @DisplayName("Should get user preferences successfully")
    void shouldGetUserPreferencesSuccessfully() {
        // Given
        Long userId = 1L;
        UserPreferencesResponse expectedResponse = createTestPreferencesResponse();
        
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(userMapper.toPreferencesResponse(testPreferences)).thenReturn(expectedResponse);
        
        // When
        UserPreferencesResponse result = userService.getUserPreferences(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        verify(userPreferencesRepository).findByUserId(userId);
        verify(userMapper).toPreferencesResponse(testPreferences);
    }
    
    @Test
    @DisplayName("Should create default preferences when not exist")
    void shouldCreateDefaultPreferencesWhenNotExist() {
        // Given
        Long userId = 1L;
        UserPreferencesResponse expectedResponse = createTestPreferencesResponse();
        
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(testPreferences);
        when(userMapper.toPreferencesResponse(testPreferences)).thenReturn(expectedResponse);
        
        // When
        UserPreferencesResponse result = userService.getUserPreferences(userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(userPreferencesRepository).save(any(UserPreferences.class));
    }
    
    @Test
    @DisplayName("Should update preferences successfully")
    void shouldUpdatePreferencesSuccessfully() {
        // Given
        Long userId = 1L;
        UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest(
            false, true, false, true, true, true, true, true, true, true, true,
            "dark", "en", "America/New_York", "public", true, false
        );
        UserPreferencesResponse expectedResponse = createTestPreferencesResponse();
        
        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(testPreferences);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toPreferencesResponse(testPreferences)).thenReturn(expectedResponse);
        
        // When
        UserPreferencesResponse result = userService.updatePreferences(userId, request, httpRequest);
        
        // Then
        assertThat(result).isNotNull();
        verify(userPreferencesRepository).save(testPreferences);
        verify(activityLoggingService).logActivity(
            eq(testUser),
            eq(UserActivityType.PREFERENCES_UPDATE),
            eq("Preferences updated"),
            eq("تم تحديث التفضيلات"),
            eq(httpRequest)
        );
        
        // Verify preferences were updated
        assertThat(testPreferences.getEmailNotifications()).isFalse();
        assertThat(testPreferences.getTheme()).isEqualTo("dark");
        assertThat(testPreferences.getLanguage()).isEqualTo("en");
        assertThat(testPreferences.getTimezone()).isEqualTo("America/New_York");
        assertThat(testPreferences.getProfileVisibility()).isEqualTo("public");
    }
    
    @Test
    @DisplayName("Should get user activity successfully")
    void shouldGetUserActivitySuccessfully() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        List<UserActivity> activities = List.of(createTestActivity());
        Page<UserActivity> activityPage = new PageImpl<>(activities, pageable, 1);
        UserActivityResponse activityResponse = createTestActivityResponse();
        Page<UserActivityResponse> expectedPage = new PageImpl<>(
            List.of(activityResponse), pageable, 1
        );
        
        when(userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
            .thenReturn(activityPage);
        when(userMapper.toActivityResponse(any(UserActivity.class))).thenReturn(activityResponse);
        
        // When
        Page<UserActivityResponse> result = userService.getUserActivity(userId, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(activityResponse);
        verify(userActivityRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    // Helper methods for creating test objects
    
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@university.edu");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setFirstNameAr("تست");
        user.setLastNameAr("يوزر");
        user.setRole(UserRole.STUDENT);
        user.setPasswordHash("hashedPassword");
        user.setPhone("+1234567890");
        user.setDateOfBirth(LocalDate.of(1995, 5, 15));
        user.setBio("Test bio");
        user.setBioAr("السيرة الذاتية للاختبار");
        user.setPreferredLanguage("ar");
        user.setUniversityId(1L);
        return user;
    }
    
    private UserResponse createTestUserResponse() {
        return new UserResponse(
            1L, "test@university.edu", "Test", "User", "تست", "يوزر",
            UserRole.STUDENT, "12345", "+1234567890", LocalDate.of(1995, 5, 15),
            true, true, null, "Test bio", "السيرة الذاتية للاختبار",
            "ar", null, null, null, null
        );
    }
    
    private UserPreferences createTestPreferences() {
        UserPreferences preferences = new UserPreferences(testUser, 1L);
        preferences.setId(1L);
        preferences.setEmailNotifications(true);
        preferences.setPushNotifications(true);
        preferences.setTheme("auto");
        preferences.setLanguage("ar");
        preferences.setTimezone("Africa/Cairo");
        preferences.setProfileVisibility("university");
        return preferences;
    }
    
    private UserPreferencesResponse createTestPreferencesResponse() {
        return new UserPreferencesResponse(
            1L, true, true, false, true, true, true, true, true, true, true, true,
            "auto", "ar", "Africa/Cairo", "university", false, false
        );
    }
    
    private UserActivity createTestActivity() {
        UserActivity activity = new UserActivity(testUser, UserActivityType.PROFILE_UPDATE, 
            "Profile updated", 1L);
        activity.setId(1L);
        activity.setDescriptionAr("تم تحديث الملف الشخصي");
        return activity;
    }
    
    private UserActivityResponse createTestActivityResponse() {
        return new UserActivityResponse(
            1L, UserActivityType.PROFILE_UPDATE, "Profile updated", 
            "تم تحديث الملف الشخصي", null, null, "127.0.0.1", null, null
        );
    }
}