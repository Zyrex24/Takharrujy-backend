package com.university.takharrujy.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.takharrujy.application.service.AuthenticationService;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.auth.*;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 * Tests Arabic language support and proper error handling
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private UserRegistrationRequest testRegistrationRequest;
    private LoginRequest testLoginRequest;
    private UserResponse testUserResponse;
    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        testRegistrationRequest = new UserRegistrationRequest(
            "ahmed@cu.edu.eg",
            "Password123!",
            "Password123!",
            "Ahmed",
            "Mohamed",
            "أحمد",
            "محمد",
            UserRole.STUDENT,
            "ST20241001",
            "+201234567890",
            1L,
            "ar"
        );

        testLoginRequest = new LoginRequest(
            "ahmed@cu.edu.eg",
            "Password123!",
            false
        );

        testUserResponse = new UserResponse(
            1L,
            "ahmed@cu.edu.eg",
            "Ahmed",
            "Mohamed",
            "أحمد",
            "محمد",
            UserRole.STUDENT,
            "ST20241001",
            "+201234567890",
            null,
            true,
            true,
            null,
            null,
            null,
            "ar",
            null,
            null,
            null,
            null
        );

        testAuthResponse = AuthResponse.of(
            "access-token",
            "refresh-token",
            java.time.Instant.now().plusSeconds(3600),
            java.time.Instant.now().plusSeconds(604800),
            testUserResponse
        );
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        when(authenticationService.registerUser(any(UserRegistrationRequest.class), any()))
            .thenReturn(testUserResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRegistrationRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully. Please check your email for verification instructions."))
            .andExpect(jsonPath("$.data.email").value("ahmed@cu.edu.eg"))
            .andExpect(jsonPath("$.data.firstNameAr").value("أحمد"))
            .andExpect(jsonPath("$.data.lastNameAr").value("محمد"))
            .andExpect(jsonPath("$.data.role").value("STUDENT"));

        verify(authenticationService).registerUser(any(UserRegistrationRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 for invalid data")
    void shouldReturn400ForInvalidRegistrationData() throws Exception {
        // Given - Invalid request with missing required fields
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "", // Empty email
            "weak", // Weak password
            "different", // Different confirmation
            "", // Empty first name
            "", // Empty last name
            null,
            null,
            UserRole.STUDENT,
            null,
            null,
            null,
            "ar"
        );

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors.email").exists())
                .andExpect(jsonPath("$.error.fieldErrors.password").exists())
                .andExpect(jsonPath("$.error.fieldErrors.firstName").exists());

        verify(authenticationService, never()).registerUser(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 409 for duplicate email")
    void shouldReturn409ForDuplicateEmail() throws Exception {
        // Given
        when(authenticationService.registerUser(any(UserRegistrationRequest.class), any()))
            .thenThrow(BusinessException.duplicateResource("Email address is already registered"));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRegistrationRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));

        verify(authenticationService).registerUser(any(UserRegistrationRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should login user successfully")
    void shouldLoginUserSuccessfully() throws Exception {
        // Given
        when(authenticationService.login(any(LoginRequest.class), any()))
            .thenReturn(testAuthResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoginRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("ahmed@cu.edu.eg"));

        verify(authenticationService).login(any(LoginRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 401 for invalid credentials")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        // Given
        when(authenticationService.login(any(LoginRequest.class), any()))
            .thenThrow(new BadCredentialsException("Invalid email or password"));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoginRequest)))
            .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_FAILED"));

        verify(authenticationService).login(any(LoginRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("refresh-token");
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
            .thenReturn(testAuthResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(authenticationService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - Should handle forgot password request")
    void shouldHandleForgotPasswordRequest() throws Exception {
        // Given
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("ahmed@cu.edu.eg");
        doNothing().when(authenticationService).forgotPassword(any(ForgotPasswordRequest.class));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotRequest)))
            .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account with that email exists, you will receive password reset instructions."));

        verify(authenticationService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Should reset password successfully")
    void shouldResetPasswordSuccessfully() throws Exception {
        // Given
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            "reset-token",
            "NewPassword123!",
            "NewPassword123!"
        );
        doNothing().when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful. You can now login with your new password."));

        verify(authenticationService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Should return 400 for invalid token")
    void shouldReturn400ForInvalidResetToken() throws Exception {
        // Given
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            "invalid-token",
            "NewPassword123!",
            "NewPassword123!"
        );
        doThrow(BusinessException.invalidInput("Token not found or expired"))
            .when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

        verify(authenticationService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email - Should verify email successfully")
    void shouldVerifyEmailSuccessfully() throws Exception {
        // Given
        VerifyEmailRequest verifyRequest = new VerifyEmailRequest("verification-token");
        doNothing().when(authenticationService).verifyEmail(any(VerifyEmailRequest.class));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
            .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully. You can now login to your account."));

        verify(authenticationService).verifyEmail(any(VerifyEmailRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/resend-verification - Should resend verification email")
    void shouldResendVerificationEmail() throws Exception {
        // Given
        ResendVerificationRequest resendRequest = new ResendVerificationRequest("ahmed@cu.edu.eg");
        doNothing().when(authenticationService).resendEmailVerification(any(ResendVerificationRequest.class));

        // When/Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest)))
            .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If your account exists and is not verified, you will receive new verification instructions."));

        verify(authenticationService).resendEmailVerification(any(ResendVerificationRequest.class));
    }

    @Test
    @DisplayName("Should handle Arabic text validation in registration")
    void shouldHandleArabicTextValidation() throws Exception {
        // Given - Request with Arabic names
        UserRegistrationRequest arabicRequest = new UserRegistrationRequest(
            "محمد@cu.edu.eg", // Arabic in email will fail university validation
            "Password123!",
            "Password123!",
            "محمد", // Arabic first name
            "أحمد", // Arabic last name
            "محمد",
            "أحمد",
            UserRole.STUDENT,
            "ST20241002",
            "+201234567890",
            1L,
            "ar"
        );

        UserResponse arabicUserResponse = new UserResponse(
            2L,
            "محمد@cu.edu.eg",
            "محمد",
            "أحمد", 
            "محمد",
            "أحمد",
            UserRole.STUDENT,
            "ST20241002",
            "+201234567890",
            null,
            true,
            false,
            null,
            null,
            null,
            "ar",
            null,
            null,
            null,
            null
        );

        when(authenticationService.registerUser(any(UserRegistrationRequest.class), any()))
            .thenReturn(arabicUserResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(arabicRequest)))
            .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("محمد"))
                .andExpect(jsonPath("$.data.lastName").value("أحمد"))
                .andExpect(jsonPath("$.data.preferredLanguage").value("ar"));

        verify(authenticationService).registerUser(any(UserRegistrationRequest.class), any());
    }
}