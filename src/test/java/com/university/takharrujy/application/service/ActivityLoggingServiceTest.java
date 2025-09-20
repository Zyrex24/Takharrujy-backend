package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.enums.UserActivityType;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.UserActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ActivityLoggingService
 * Tests activity logging functionality for audit and tracking purposes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Activity Logging Service Tests")
class ActivityLoggingServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ActivityLoggingService activityLoggingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@university.edu");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Mohamed");
        testUser.setFirstNameAr("أحمد");
        testUser.setLastNameAr("محمد");
        testUser.setRole(UserRole.STUDENT);
        testUser.setUniversityId(1L);
        testUser.setIsActive(true);
    }

    // Basic Activity Logging Tests

    @Test
    @DisplayName("Should log basic activity successfully")
    void shouldLogBasicActivitySuccessfully() {
        // Given
        UserActivityType activityType = UserActivityType.LOGIN;
        String description = "User logged in successfully";
        String descriptionAr = "تم تسجيل دخول المستخدم بنجاح";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log basic activity without request")
    void shouldLogBasicActivityWithoutRequest() {
        // Given
        UserActivityType activityType = UserActivityType.LOGOUT;
        String description = "User logged out";
        String descriptionAr = "تم تسجيل خروج المستخدم";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, null);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log activity with Arabic description")
    void shouldLogActivityWithArabicDescription() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_CREATE;
        String description = "Project created successfully";
        String descriptionAr = "تم إنشاء المشروع بنجاح";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    // Activity Logging with Resource Reference Tests

    @Test
    @DisplayName("Should log activity with resource reference successfully")
    void shouldLogActivityWithResourceReferenceSuccessfully() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_UPDATE;
        String description = "Project updated";
        String descriptionAr = "تم تحديث المشروع";
        String resourceType = "PROJECT";
        Long resourceId = 123L;

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                        resourceType, resourceId, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log activity with resource reference without request")
    void shouldLogActivityWithResourceReferenceWithoutRequest() {
        // Given
        UserActivityType activityType = UserActivityType.FILE_UPLOAD;
        String description = "File uploaded";
        String descriptionAr = "تم رفع الملف";
        String resourceType = "FILE";
        Long resourceId = 456L;

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                        resourceType, resourceId, null);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log activity with different resource types")
    void shouldLogActivityWithDifferentResourceTypes() {
        // Test with different resource types
        String[] resourceTypes = {"PROJECT", "FILE", "TASK", "USER", "DEPARTMENT"};
        Long[] resourceIds = {1L, 2L, 3L, 4L, 5L};

        for (int i = 0; i < resourceTypes.length; i++) {
            // Given
            UserActivityType activityType = UserActivityType.PROJECT_CREATE;
            String description = "Resource accessed";
            String descriptionAr = "تم الوصول للمورد";

            // When
            activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                            resourceTypes[i], resourceIds[i], httpRequest);

            // Then
            verify(userActivityRepository, atLeast(i + 1)).save(any(UserActivity.class));
        }
    }

    // Activity Logging with Additional Data Tests

    @Test
    @DisplayName("Should log activity with additional data successfully")
    void shouldLogActivityWithAdditionalDataSuccessfully() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_CREATE;
        String description = "Project submitted for review";
        String descriptionAr = "تم تقديم المشروع للمراجعة";
        String resourceType = "PROJECT";
        Long resourceId = 789L;
        String additionalData = "{\"status\":\"submitted\",\"reviewer\":\"admin\"}";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                        resourceType, resourceId, additionalData, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log activity with additional data without request")
    void shouldLogActivityWithAdditionalDataWithoutRequest() {
        // Given
        UserActivityType activityType = UserActivityType.PASSWORD_CHANGE;
        String description = "Password changed";
        String descriptionAr = "تم تغيير كلمة المرور";
        String resourceType = "USER";
        Long resourceId = 1L;
        String additionalData = "{\"changeType\":\"user_initiated\"}";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                        resourceType, resourceId, additionalData, null);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should log activity with complex additional data")
    void shouldLogActivityWithComplexAdditionalData() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_CREATE;
        String description = "Project approved by supervisor";
        String descriptionAr = "تم الموافقة على المشروع من قبل المشرف";
        String resourceType = "PROJECT";
        Long resourceId = 999L;
        String additionalData = "{\"approver\":\"supervisor\",\"approvalDate\":\"2024-01-15\",\"comments\":\"Excellent work\"}";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, 
                                        resourceType, resourceId, additionalData, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    // Cleanup Tests

    @Test
    @DisplayName("Should cleanup old activities successfully")
    void shouldCleanupOldActivitiesSuccessfully() {
        // Given
        Long userId = 1L;
        int daysToKeep = 30;

        // When
        activityLoggingService.cleanupOldActivities(userId, daysToKeep);

        // Then
        verify(userActivityRepository).deleteOldActivities(eq(userId), any(Instant.class));
    }

    @Test
    @DisplayName("Should cleanup old activities with different retention periods")
    void shouldCleanupOldActivitiesWithDifferentRetentionPeriods() {
        // Test different retention periods
        int[] retentionPeriods = {7, 30, 90, 365};

        for (int days : retentionPeriods) {
            // When
            activityLoggingService.cleanupOldActivities(testUser.getId(), days);

            // Then
            verify(userActivityRepository).deleteOldActivities(eq(testUser.getId()), any(Instant.class));
        }
    }

    // Request Information Tests

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIpFromXForwardedForHeader() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        UserActivityType activityType = UserActivityType.LOGIN;
        String description = "User logged in";
        String descriptionAr = "تم تسجيل دخول المستخدم";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
    }

    @Test
    @DisplayName("Should extract client IP from X-Real-IP header when X-Forwarded-For is not available")
    void shouldExtractClientIpFromXRealIpHeader() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn("203.0.113.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Chrome/91.0");

        UserActivityType activityType = UserActivityType.FILE_DOWNLOAD;
        String description = "File downloaded";
        String descriptionAr = "تم تحميل الملف";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
        verify(httpRequest).getHeader("X-Real-IP");
    }

    @Test
    @DisplayName("Should use remote address when headers are not available")
    void shouldUseRemoteAddressWhenHeadersNotAvailable() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Safari/14.0");

        UserActivityType activityType = UserActivityType.PROFILE_UPDATE;
        String description = "Profile updated";
        String descriptionAr = "تم تحديث الملف الشخصي";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
        verify(httpRequest).getHeader("X-Real-IP");
        verify(httpRequest).getRemoteAddr();
    }

    @Test
    @DisplayName("Should handle multiple IPs in X-Forwarded-For header")
    void shouldHandleMultipleIpsInXForwardedForHeader() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1, 192.0.2.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Firefox/89.0");

        UserActivityType activityType = UserActivityType.MESSAGE_SEND;
        String description = "Email sent";
        String descriptionAr = "تم إرسال البريد الإلكتروني";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
    }

    // Edge Cases and Error Handling Tests

    @Test
    @DisplayName("Should handle empty X-Forwarded-For header")
    void shouldHandleEmptyXForwardedForHeader() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("");
        when(httpRequest.getHeader("X-Real-IP")).thenReturn("203.0.113.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Edge/91.0");

        UserActivityType activityType = UserActivityType.LOGIN;
        String description = "System accessed";
        String descriptionAr = "تم الوصول للنظام";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
        verify(httpRequest).getHeader("X-Real-IP");
    }

    @Test
    @DisplayName("Should handle null headers gracefully")
    void shouldHandleNullHeadersGracefully() {
        // Given
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn(null);

        UserActivityType activityType = UserActivityType.FILE_DOWNLOAD;
        String description = "Data exported";
        String descriptionAr = "تم تصدير البيانات";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(httpRequest).getHeader("X-Forwarded-For");
        verify(httpRequest).getHeader("X-Real-IP");
        verify(httpRequest).getRemoteAddr();
        verify(httpRequest).getHeader("User-Agent");
    }

    @Test
    @DisplayName("Should handle all activity types")
    void shouldHandleAllActivityTypes() {
        // Test all available activity types
        UserActivityType[] activityTypes = UserActivityType.values();

        for (UserActivityType activityType : activityTypes) {
            // Given
            String description = "Activity: " + activityType.name();
            String descriptionAr = "النشاط: " + activityType.name();

            // When
            activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

            // Then
            verify(userActivityRepository, atLeastOnce()).save(any(UserActivity.class));
        }
    }

    @Test
    @DisplayName("Should handle long descriptions")
    void shouldHandleLongDescriptions() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_CREATE;
        String longDescription = "This is a very long description that contains detailed information about the activity that was performed by the user in the system. It includes multiple sentences and detailed context about what happened during this particular activity.";
        String longDescriptionAr = "هذا وصف طويل جداً يحتوي على معلومات مفصلة حول النشاط الذي تم تنفيذه من قبل المستخدم في النظام. يتضمن جمل متعددة وسياق مفصل حول ما حدث خلال هذا النشاط المحدد.";

        // When
        activityLoggingService.logActivity(testUser, activityType, longDescription, longDescriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("Should handle special characters in descriptions")
    void shouldHandleSpecialCharactersInDescriptions() {
        // Given
        UserActivityType activityType = UserActivityType.PROJECT_UPDATE;
        String description = "User updated project with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        String descriptionAr = "قام المستخدم بتحديث المشروع بأحرف خاصة: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        activityLoggingService.logActivity(testUser, activityType, description, descriptionAr, httpRequest);

        // Then
        verify(userActivityRepository).save(any(UserActivity.class));
    }
}
