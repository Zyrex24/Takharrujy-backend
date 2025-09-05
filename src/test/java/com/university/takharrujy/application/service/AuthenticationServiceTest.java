package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Department;
import com.university.takharrujy.domain.entity.University;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.DepartmentRepository;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.infrastructure.security.JwtTokenProvider;
import com.university.takharrujy.infrastructure.security.SessionService;
import com.university.takharrujy.infrastructure.service.EmailService;
import com.university.takharrujy.infrastructure.service.TokenService;
import com.university.takharrujy.presentation.dto.auth.LoginRequest;
import com.university.takharrujy.presentation.dto.auth.UserRegistrationRequest;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 * Tests Arabic language support and FERPA compliance
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthenticationService authenticationService;

    private University testUniversity;
    private Department testDepartment;
    private User testUser;
    private UserRegistrationRequest testRegistrationRequest;

    @BeforeEach
    void setUp() {
        // Setup test university
        testUniversity = new University();
        testUniversity.setId(1L);
        testUniversity.setName("Cairo University");
        testUniversity.setNameAr("جامعة القاهرة");
        testUniversity.setDomain("cu.edu.eg");
        testUniversity.setIsActive(true);

        // Setup test department
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setUniversityId(1L);
        testDepartment.setName("Computer Science");
        testDepartment.setNameAr("علوم الحاسوب");
        testDepartment.setCode("CS");
        testDepartment.setIsActive(true);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUniversityId(1L);
        testUser.setDepartment(testDepartment);
        testUser.setEmail("ahmed@cu.edu.eg");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Mohamed");
        testUser.setFirstNameAr("أحمد");
        testUser.setLastNameAr("محمد");
        testUser.setRole(UserRole.STUDENT);
        testUser.setStudentId("ST20241001");
        testUser.setPhone("+201234567890");
        testUser.setPreferredLanguage("ar");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);

        // Setup test registration request
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
    }

    @Test
    @DisplayName("Should register user successfully with Arabic names")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(universityRepository.findByDomainIgnoreCase("cu.edu.eg")).thenReturn(Optional.of(testUniversity));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenService.generateEmailVerificationToken(anyString(), anyLong())).thenReturn("verification-token");
        when(userMapper.toUserResponse(any(User.class))).thenReturn(createUserResponse());
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        // When
        UserResponse result = authenticationService.registerUser(testRegistrationRequest, httpRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("ahmed@cu.edu.eg");
        assertThat(result.firstNameAr()).isEqualTo("أحمد");
        assertThat(result.lastNameAr()).isEqualTo("محمد");
        
        verify(userRepository).existsByEmail("ahmed@cu.edu.eg");
        verify(universityRepository).findByDomainIgnoreCase("cu.edu.eg");
        verify(departmentRepository).findById(1L);
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
        verify(tokenService).generateEmailVerificationToken("ahmed@cu.edu.eg", 1L);
        verify(emailService).sendWelcomeEmail(eq("ahmed@cu.edu.eg"), anyString(), anyString(), eq("verification-token"), eq("ar"));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticationService.registerUser(testRegistrationRequest, httpRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Email address is already registered");

        verify(userRepository).existsByEmail("ahmed@cu.edu.eg");
        verifyNoMoreInteractions(universityRepository, departmentRepository, passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("Should throw exception when passwords don't match")
    void shouldThrowExceptionWhenPasswordsDontMatch() {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "ahmed@cu.edu.eg",
            "Password123!",
            "DifferentPassword123!",
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

        // When/Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest, httpRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Passwords do not match");
    }

    @Test
    @DisplayName("Should throw exception when university domain not found")
    void shouldThrowExceptionWhenUniversityDomainNotFound() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(universityRepository.findByDomainIgnoreCase("unknown.edu")).thenReturn(Optional.empty());

        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
            "student@unknown.edu",
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

        // When/Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest, httpRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("is not associated with any registered university");

        verify(universityRepository).findByDomainIgnoreCase("unknown.edu");
    }

    @Test
    @DisplayName("Should throw exception when department doesn't belong to university")
    void shouldThrowExceptionWhenDepartmentDoesntBelongToUniversity() {
        // Given
        Department otherUniversityDepartment = new Department();
        otherUniversityDepartment.setId(2L);
        otherUniversityDepartment.setUniversityId(2L); // Different university
        otherUniversityDepartment.setIsActive(true);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(universityRepository.findByDomainIgnoreCase("cu.edu.eg")).thenReturn(Optional.of(testUniversity));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(otherUniversityDepartment));

        UserRegistrationRequest invalidRequest = new UserRegistrationRequest(
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
            2L, // Department from different university
            "ar"
        );

        // When/Then
        assertThatThrownBy(() -> authenticationService.registerUser(invalidRequest, httpRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Department does not belong to your university");

        verify(departmentRepository).findById(2L);
    }

    @Test
    @DisplayName("Should login user successfully with verified email")
    void shouldLoginUserSuccessfully() {
        // Given
        LoginRequest loginRequest = new LoginRequest("ahmed@cu.edu.eg", "Password123!", false);
        testUser.setIsEmailVerified(true);

        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        
        // Mock CustomUserPrincipal
        var mockPrincipal = mock(com.university.takharrujy.infrastructure.security.CustomUserDetailsService.CustomUserPrincipal.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getUser()).thenReturn(testUser);
        
        when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userMapper.toUserResponse(any(User.class))).thenReturn(createUserResponse());
        doNothing().when(sessionService).createSession(anyString(), anyString(), anyString(), any());

        // When
        var result = authenticationService.login(loginRequest, httpRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().email()).isEqualTo("ahmed@cu.edu.eg");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateAccessToken(any(Authentication.class));
        verify(tokenProvider).generateRefreshToken("ahmed@cu.edu.eg");
        verify(sessionService).createSession(eq("ahmed@cu.edu.eg"), eq("access-token"), eq("refresh-token"), any());
    }

    @Test
    @DisplayName("Should throw exception when email is not verified")
    void shouldThrowExceptionWhenEmailNotVerified() {
        // Given
        LoginRequest loginRequest = new LoginRequest("ahmed@cu.edu.eg", "Password123!", false);
        testUser.setIsEmailVerified(false); // Email not verified

        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        
        var mockPrincipal = mock(com.university.takharrujy.infrastructure.security.CustomUserDetailsService.CustomUserPrincipal.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getUser()).thenReturn(testUser);

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest, httpRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Email verification required");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider, sessionService);
    }

    @Test
    @DisplayName("Should throw bad credentials exception for invalid login")
    void shouldThrowBadCredentialsForInvalidLogin() {
        // Given
        LoginRequest loginRequest = new LoginRequest("ahmed@cu.edu.eg", "wrongpassword", false);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest, httpRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid email or password");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider, sessionService);
    }

    @Test
    @DisplayName("Should handle forgot password request gracefully")
    void shouldHandleForgotPasswordRequest() {
        // Given
        var request = new com.university.takharrujy.presentation.dto.auth.ForgotPasswordRequest("ahmed@cu.edu.eg");
        
        when(userRepository.findByEmail("ahmed@cu.edu.eg")).thenReturn(Optional.of(testUser));
        when(tokenService.generatePasswordResetToken(anyString(), anyLong())).thenReturn("reset-token");
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        // When
        authenticationService.forgotPassword(request);

        // Then
        verify(userRepository).findByEmail("ahmed@cu.edu.eg");
        verify(tokenService).generatePasswordResetToken("ahmed@cu.edu.eg", 1L);
        verify(emailService).sendPasswordResetEmail(eq("ahmed@cu.edu.eg"), anyString(), anyString(), eq("reset-token"), eq("ar"));
    }

    @Test
    @DisplayName("Should not reveal non-existent email in forgot password")
    void shouldNotRevealNonExistentEmailInForgotPassword() {
        // Given
        var request = new com.university.takharrujy.presentation.dto.auth.ForgotPasswordRequest("nonexistent@cu.edu.eg");
        
        when(userRepository.findByEmail("nonexistent@cu.edu.eg")).thenReturn(Optional.empty());

        // When
        authenticationService.forgotPassword(request);

        // Then
        verify(userRepository).findByEmail("nonexistent@cu.edu.eg");
        verifyNoInteractions(tokenService, emailService); // Should not send email for non-existent user
    }

    private UserResponse createUserResponse() {
        return new UserResponse(
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
    }
}