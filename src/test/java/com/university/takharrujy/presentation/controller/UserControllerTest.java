package com.university.takharrujy.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.takharrujy.application.service.UserService;
import com.university.takharrujy.domain.enums.UserActivityType;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.presentation.dto.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private UserResponse testUserResponse;
    private UserPreferencesResponse testPreferencesResponse;
    private UserActivityResponse testActivityResponse;
    
    @BeforeEach
    void setUp() {
        testUserResponse = createTestUserResponse();
        testPreferencesResponse = createTestPreferencesResponse();
        testActivityResponse = createTestActivityResponse();
    }
    
    @Test
    @DisplayName("Should get current user profile successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldGetCurrentUserProfileSuccessfully() throws Exception {
        // Given
        when(userService.getCurrentUserProfile(1L)).thenReturn(testUserResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile retrieved successfully"))
                .andExpect(jsonPath("$.messageAr").value("تم استرداد الملف الشخصي بنجاح"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@university.edu"))
                .andExpect(jsonPath("$.data.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.data.firstNameAr").value("أحمد"));
    }
    
    @Test
    @DisplayName("Should return 401 when user not authenticated")
    void shouldReturn401WhenUserNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should update user profile successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldUpdateUserProfileSuccessfully() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
            "أحمد", "محمد", "Ahmed", "Mohamed",
            "+201234567890", LocalDate.of(1995, 5, 15),
            "Computer Science Student", "طالب علوم حاسوب",
            "ar", 1L
        );
        
        when(userService.updateProfile(eq(1L), any(UserUpdateRequest.class), any()))
            .thenReturn(testUserResponse);
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("Profile updated successfully"))
                .andExpect(jsonPath("$.messageAr").value("تم تحديث الملف الشخصي بنجاح"))
                .andExpect(jsonPath("$.data.id").value(1));
    }
    
    @Test
    @DisplayName("Should return 400 when profile update validation fails")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldReturn400WhenProfileUpdateValidationFails() throws Exception {
        // Given - Invalid Arabic text (too short)
        UserUpdateRequest request = new UserUpdateRequest(
            "أ", null, null, null, null, null, null, null, null, null
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should upload avatar successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldUploadAvatarSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", "avatar.jpg", "image/jpeg", "test image data".getBytes()
        );
        
        when(userService.uploadAvatar(eq(1L), any(), any())).thenReturn(testUserResponse);
        
        // When & Then
        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                .file(file)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"))
                .andExpected(jsonPath("$.messageAr").value("تم رفع الصورة الشخصية بنجاح"));
    }
    
    @Test
    @DisplayName("Should return 400 when avatar file is missing")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldReturn400WhenAvatarFileMissing() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpected(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should change password successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldChangePasswordSuccessfully() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
            "currentPassword123",
            "newPassword456!",
            "newPassword456!"
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpected(jsonPath("$.messageAr").value("تم تغيير كلمة المرور بنجاح"));
    }
    
    @Test
    @DisplayName("Should return 400 when password validation fails")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldReturn400WhenPasswordValidationFails() throws Exception {
        // Given - Weak password
        ChangePasswordRequest request = new ChangePasswordRequest(
            "currentPassword123",
            "weak",
            "weak"
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpected(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should get user preferences successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldGetUserPreferencesSuccessfully() throws Exception {
        // Given
        when(userService.getUserPreferences(1L)).thenReturn(testPreferencesResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/me/preferences")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Preferences retrieved successfully"))
                .andExpected(jsonPath("$.messageAr").value("تم استرداد التفضيلات بنجاح"))
                .andExpected(jsonPath("$.data.userId").value(1))
                .andExpected(jsonPath("$.data.emailNotifications").value(true))
                .andExpected(jsonPath("$.data.theme").value("auto"))
                .andExpect(jsonPath("$.data.language").value("ar"));
    }
    
    @Test
    @DisplayName("Should update user preferences successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldUpdateUserPreferencesSuccessfully() throws Exception {
        // Given
        UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest(
            false, true, false, true, true, true, true, true, true, true, true,
            "dark", "en", "America/New_York", "public", true, false
        );
        
        when(userService.updatePreferences(eq(1L), any(UserPreferencesUpdateRequest.class), any()))
            .thenReturn(testPreferencesResponse);
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me/preferences")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Preferences updated successfully"))
                .andExpect(jsonPath("$.messageAr").value("تم تحديث التفضيلات بنجاح"));
    }
    
    @Test
    @DisplayName("Should return 400 when preferences validation fails")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldReturn400WhenPreferencesValidationFails() throws Exception {
        // Given - Invalid theme value
        UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest(
            null, null, null, null, null, null, null, null, null, null, null,
            "invalid_theme", null, null, null, null, null
        );
        
        // When & Then
        mockMvc.perform(put("/api/v1/users/me/preferences")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpected(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should get user activity successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldGetUserActivitySuccessfully() throws Exception {
        // Given
        Page<UserActivityResponse> activityPage = new PageImpl<>(
            List.of(testActivityResponse), PageRequest.of(0, 20), 1
        );
        
        when(userService.getUserActivity(eq(1L), any())).thenReturn(activityPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/me/activity")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("Activity history retrieved successfully"))
                .andExpect(jsonPath("$.messageAr").value("تم استرداد سجل الأنشطة بنجاح"))
                .andExpected(jsonPath("$.data.content").isArray())
                .andExpected(jsonPath("$.data.content[0].id").value(1))
                .andExpected(jsonPath("$.data.content[0].activityType").value("PROFILE_UPDATE"));
    }
    
    @Test
    @DisplayName("Should get recent user activity successfully")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldGetRecentUserActivitySuccessfully() throws Exception {
        // Given
        Page<UserActivityResponse> activityPage = new PageImpl<>(
            List.of(testActivityResponse), PageRequest.of(0, 20), 1
        );
        
        when(userService.getRecentActivity(eq(1L), any())).thenReturn(activityPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/users/me/activity")
                .param("page", "0")
                .param("size", "20")
                .param("recent", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.content").isArray());
    }
    
    @Test
    @DisplayName("Should validate pagination parameters")
    @WithMockUser(username = "1", roles = "STUDENT")
    void shouldValidatePaginationParameters() throws Exception {
        // Given
        Page<UserActivityResponse> activityPage = new PageImpl<>(
            List.of(testActivityResponse), PageRequest.of(0, 20), 1
        );
        
        when(userService.getUserActivity(eq(1L), any())).thenReturn(activityPage);
        
        // When & Then - Test negative page number correction
        mockMvc.perform(get("/api/v1/users/me/activity")
                .param("page", "-1")
                .param("size", "150") // Should be limited to 100
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    // Test helper methods
    
    private UserResponse createTestUserResponse() {
        return new UserResponse(
            1L, "test@university.edu", "Ahmed", "Mohamed", "أحمد", "محمد",
            UserRole.STUDENT, "12345", "+201234567890", LocalDate.of(1995, 5, 15),
            true, true, "/avatars/avatar.jpg", "Computer Science Student", 
            "طالب علوم حاسوب", "ar", null, null, null, null
        );
    }
    
    private UserPreferencesResponse createTestPreferencesResponse() {
        return new UserPreferencesResponse(
            1L, true, true, false, true, true, true, true, true, true, true, true,
            "auto", "ar", "Africa/Cairo", "university", false, false
        );
    }
    
    private UserActivityResponse createTestActivityResponse() {
        return new UserActivityResponse(
            1L, UserActivityType.PROFILE_UPDATE, "Profile updated", 
            "تم تحديث الملف الشخصي", null, null, "127.0.0.1", null, null
        );
    }
}