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
import com.university.takharrujy.infrastructure.security.CustomUserDetailsService;
import com.university.takharrujy.infrastructure.security.JwtTokenProvider;
import com.university.takharrujy.infrastructure.security.SessionService;
import com.university.takharrujy.infrastructure.service.EmailService;
import com.university.takharrujy.infrastructure.service.TokenService;
import com.university.takharrujy.presentation.dto.auth.*;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import com.university.takharrujy.presentation.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Authentication Service
 * Comprehensive authentication service with FERPA compliance and Arabic language support
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Value("${takharrujy.auth.require-email-verification:false}")
    private boolean requireEmailVerification;
    
    private static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile("@(.+)$");
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_DURATION_HOURS = 24;

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    public AuthenticationService(UserRepository userRepository,
                               UniversityRepository universityRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder,
                               JwtTokenProvider tokenProvider,
                               AuthenticationManager authenticationManager,
                               SessionService sessionService,
                               TokenService tokenService,
                               EmailService emailService,
                               UserMapper userMapper) {
        this.userRepository = userRepository;
        this.universityRepository = universityRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }

    /**
     * Simple user registration for frontend forms (without department selection)
     */
    public UserResponse registerUserSimple(String email, String password, String firstName, String lastName, 
                                         UserRole role, Long universityId, String preferredLanguage, 
                                         HttpServletRequest httpRequest) {
        logger.info("Starting simple user registration for email: {}", email);

        // Validate basic requirements
        if (email == null || password == null || firstName == null || lastName == null || 
            role == null || universityId == null) {
            throw BusinessException.invalidInput("Required fields are missing");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw BusinessException.invalidInput("Email address is already registered");
        }

        // Find university
        University university = universityRepository.findById(universityId)
            .orElseThrow(() -> ResourceNotFoundException.university(universityId));
            
        if (!university.getIsActive()) {
            throw BusinessException.invalidInput("Selected university is not active");
        }

        // Find first active department for this university as default
        Department department = departmentRepository.findByUniversityIdAndIsActiveTrue(universityId)
            .stream()
            .findFirst()
            .orElseThrow(() -> BusinessException.invalidInput("No active departments found for this university"));

        // Create user entity
        User user = new User();
        user.setUniversityId(university.getId());
        user.setDepartment(department);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setFirstNameAr(firstName); // Use same name as fallback
        user.setLastNameAr(lastName);   // Use same name as fallback
        user.setRole(role);
        user.setStudentId(null); // Not provided in simple form
        user.setPhone(null);     // Not provided in simple form
        user.setPreferredLanguage(preferredLanguage != null ? preferredLanguage : "en");
        user.setIsActive(true);
        user.setIsEmailVerified(!requireEmailVerification);

        // Save user
        user = userRepository.save(user);

        // Generate email verification token and send email (only if verification is required)
        if (requireEmailVerification) {
            String verificationToken = tokenService.generateEmailVerificationToken(user.getEmail(), user.getId());
            sendWelcomeEmail(user, verificationToken);
        } else {
            // Send welcome email without verification token
            sendWelcomeEmailWithoutVerification(user);
        }

        logger.info("Successfully registered user with ID: {} for university: {}", user.getId(), university.getName());
        return userMapper.toUserResponse(user);
    }

    /**
     * Register a new user with university email validation
     */
    public UserResponse registerUser(UserRegistrationRequest request, HttpServletRequest httpRequest) {
        logger.info("Starting user registration for email: {}", request.email());

        // Validate request
        validateRegistrationRequest(request);

        // Extract and validate university domain
        String domain = extractDomainFromEmail(request.email());
        University university = findUniversityByDomain(domain);

        // Validate department belongs to university
        Department department = validateDepartmentBelongsToUniversity(request.departmentId(), university.getId());

        // Create user entity
        User user = createUserFromRequest(request, university, department);

        // Save user
        user = userRepository.save(user);

        // Generate email verification token and send email (only if verification is required)
        if (requireEmailVerification) {
            String verificationToken = tokenService.generateEmailVerificationToken(user.getEmail(), user.getId());
            sendWelcomeEmail(user, verificationToken);
        } else {
            // Send welcome email without verification token
            sendWelcomeEmailWithoutVerification(user);
        }

        logger.info("Successfully registered user with ID: {} for university: {}", user.getId(), university.getName());
        return userMapper.toUserResponse(user);
    }

    /**
     * Authenticate user and return tokens
     */
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("User login attempt for email: {}", request.email());

        try {
            // Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            User user = userPrincipal.getUser();

            // Check if email is verified (only if email verification is required)
            if (requireEmailVerification && !user.getIsEmailVerified()) {
                logger.warn("Login attempt with unverified email: {}", request.email());
                throw new BusinessException("OPERATION_NOT_ALLOWED", "Email verification required. Please check your email for verification instructions.", "business.operation.not.allowed", HttpStatus.FORBIDDEN);
            }

            // Generate tokens (with remember me support)
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail(), request.rememberMe());

            // Calculate token expiration times
            Instant accessTokenExpiresAt = Instant.now().plusSeconds(getAccessTokenExpirationSeconds());
            Instant refreshTokenExpiresAt = Instant.now().plusSeconds(getRefreshTokenExpirationSeconds(request.rememberMe()));

            // Create session data
            SessionService.UserSessionData sessionData = new SessionService.UserSessionData(
                user.getId().toString(),
                user.getUniversityId().toString(),
                httpRequest.getHeader("User-Agent"),
                getClientIpAddress(httpRequest),
                user.getPreferredLanguage(),
                Instant.now()
            );

            // Create session
            sessionService.createSession(user.getEmail(), accessToken, refreshToken, sessionData);

            logger.info("Successful login for user: {} from university: {}", user.getId(), user.getUniversityId());

            // Create response
            UserResponse userResponse = userMapper.toUserResponse(user);
            return AuthResponse.of(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, userResponse);

        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for email: {}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.debug("Token refresh attempt");

        // Validate refresh token
        if (!tokenProvider.validateToken(request.refreshToken()) || !tokenProvider.isRefreshToken(request.refreshToken())) {
            logger.warn("Invalid refresh token provided");
            throw BusinessException.invalidInput("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(request.refreshToken());
        
        // Check if user has active session with this refresh token
        String storedRefreshToken = sessionService.getRefreshToken(username);
        if (!request.refreshToken().equals(storedRefreshToken)) {
            logger.warn("Refresh token mismatch for user: {}", username);
            throw BusinessException.invalidInput("Invalid refresh token");
        }

        // Load user and generate new tokens
        User user = userRepository.findByEmailAndIsActiveTrue(username)
            .orElseThrow(() -> ResourceNotFoundException.user(null));

        // Create new authentication for token generation
        UserDetails userDetails = new CustomUserDetailsService.CustomUserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        // Update session
        sessionService.updateSession(username, newAccessToken, newRefreshToken);

        // Calculate expiration times
        Instant accessTokenExpiresAt = Instant.now().plusSeconds(getAccessTokenExpirationSeconds());
        Instant refreshTokenExpiresAt = Instant.now().plusSeconds(getRefreshTokenExpirationSeconds());

        logger.info("Successfully refreshed tokens for user: {}", user.getId());

        UserResponse userResponse = userMapper.toUserResponse(user);
        return AuthResponse.of(newAccessToken, newRefreshToken, accessTokenExpiresAt, refreshTokenExpiresAt, userResponse);
    }

    /**
     * Logout user and invalidate session
     */
    public void logout(String username, String accessToken) {
        logger.info("User logout for: {}", username);
        
        // Invalidate session
        sessionService.invalidateSession(username, accessToken);
        
        logger.info("Successfully logged out user: {}", username);
    }

    /**
     * Send password reset email
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        logger.info("Password reset request for email: {}", request.email());

        User user = userRepository.findByEmail(request.email()).orElse(null);
        
        if (user == null || !user.getIsActive()) {
            // Don't reveal whether email exists - just log and return success
            logger.warn("Password reset requested for non-existent or inactive email: {}", request.email());
            return; // Return success to prevent email enumeration
        }

        // Generate reset token
        String resetToken = tokenService.generatePasswordResetToken(user.getEmail(), user.getId());

        // Send reset email
        emailService.sendPasswordResetEmail(
            user.getEmail(),
            user.getFullName(),
            user.getFullNameAr(),
            resetToken,
            user.getPreferredLanguage()
        );

        logger.info("Password reset email sent for user: {}", user.getId());
    }

    /**
     * Reset password using reset token
     */
    public void resetPassword(ResetPasswordRequest request) {
        logger.info("Password reset attempt with token");

        // Validate passwords match
        if (!request.isPasswordConfirmed()) {
            throw BusinessException.passwordMismatch("Passwords do not match");
        }

        // Validate reset token
        TokenService.TokenValidationResult tokenResult = tokenService.validatePasswordResetToken(request.token());
        if (!tokenResult.isValid()) {
            logger.warn("Invalid password reset token used");
            throw BusinessException.tokenInvalid(tokenResult.getErrorMessage());
        }

        // Find user
        User user = userRepository.findById(tokenResult.getUserId())
            .orElseThrow(() -> ResourceNotFoundException.user(tokenResult.getUserId()));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate all sessions for security
        sessionService.invalidateAllSessions(user.getEmail());

        // Send confirmation email
        emailService.sendPasswordChangedEmail(
            user.getEmail(),
            user.getFullName(),
            user.getFullNameAr(),
            user.getPreferredLanguage()
        );

        logger.info("Password successfully reset for user: {}", user.getId());
    }

    /**
     * Verify email using verification token
     */
    public void verifyEmail(VerifyEmailRequest request) {
        logger.info("Email verification attempt");

        // Validate verification token
        TokenService.TokenValidationResult tokenResult = tokenService.validateEmailVerificationToken(request.token());
        if (!tokenResult.isValid()) {
            logger.warn("Invalid email verification token used");
            throw BusinessException.invalidInput(tokenResult.getErrorMessage());
        }

        // Find user
        User user = userRepository.findById(tokenResult.getUserId())
            .orElseThrow(() -> ResourceNotFoundException.user(tokenResult.getUserId()));

        // Update email verification status
        user.setIsEmailVerified(true);
        userRepository.save(user);

        logger.info("Email successfully verified for user: {}", user.getId());
    }

    /**
     * Resend email verification
     */
    public void resendEmailVerification(ResendVerificationRequest request) {
        logger.info("Resend email verification request for: {}", request.email());

        User user = userRepository.findByEmail(request.email()).orElse(null);
        
        if (user == null || !user.getIsActive()) {
            logger.warn("Email verification resend requested for non-existent email: {}", request.email());
            return; // Don't reveal if email exists
        }

        if (user.getIsEmailVerified()) {
            logger.info("Email verification resend requested for already verified email: {}", request.email());
            return; // Email already verified
        }

        // Generate new verification token
        String verificationToken = tokenService.generateEmailVerificationToken(user.getEmail(), user.getId());

        // Send verification reminder email
        emailService.sendEmailVerificationReminder(
            user.getEmail(),
            user.getFullName(),
            user.getFullNameAr(),
            verificationToken,
            user.getPreferredLanguage()
        );

        logger.info("Email verification resent for user: {}", user.getId());
    }

    // Private helper methods
    
    private void validateRegistrationRequest(UserRegistrationRequest request) {
        // Check password confirmation
        if (!request.isPasswordConfirmed()) {
            throw BusinessException.passwordMismatch("Passwords do not match");
        }
        
        // Check student ID for students
        if (!request.isStudentIdValidForRole()) {
            throw BusinessException.invalidInput("Student ID is required for student accounts");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw BusinessException.duplicateResource("Email address is already registered");
        }
    }

    private String extractDomainFromEmail(String email) {
        java.util.regex.Matcher matcher = EMAIL_DOMAIN_PATTERN.matcher(email);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }
        throw BusinessException.invalidInput("Invalid email format");
    }

    private University findUniversityByDomain(String domain) {
        return universityRepository.findByDomainIgnoreCase(domain)
            .orElseThrow(() -> BusinessException.invalidInput(
                "Email domain '" + domain + "' is not associated with any registered university"
            ));
    }

    private Department validateDepartmentBelongsToUniversity(Long departmentId, Long universityId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> ResourceNotFoundException.department(departmentId));
            
        if (!department.getUniversityId().equals(universityId)) {
            throw BusinessException.invalidInput("Department does not belong to your university");
        }
        
        if (!department.getIsActive()) {
            throw BusinessException.invalidInput("Department is not active");
        }
        
        return department;
    }

    private User createUserFromRequest(UserRegistrationRequest request, University university, Department department) {
        User user = new User();
        user.setUniversityId(university.getId());
        user.setDepartment(department);
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setFirstNameAr(request.firstNameAr());
        user.setLastNameAr(request.lastNameAr());
        user.setRole(request.role());
        user.setStudentId(request.studentId());
        user.setPhone(request.phone());
        user.setPreferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage() : "ar");
        user.setIsActive(true);
        user.setIsEmailVerified(!requireEmailVerification);
        
        return user;
    }

    private void sendWelcomeEmail(User user, String verificationToken) {
        try {
            emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFullName(),
                user.getFullNameAr(),
                verificationToken,
                user.getPreferredLanguage()
            );
        } catch (Exception e) {
            logger.error("Failed to send welcome email to user: {}", user.getId(), e);
            // Don't fail registration if email fails
        }
    }

    private void sendWelcomeEmailWithoutVerification(User user) {
        try {
            emailService.sendWelcomeEmail(
                user.getEmail(),
                user.getFullName(),
                user.getFullNameAr(),
                null, // No verification token
                user.getPreferredLanguage()
            );
        } catch (Exception e) {
            logger.error("Failed to send welcome email to user: {}", user.getId(), e);
            // Don't fail registration if email fails
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private long getAccessTokenExpirationSeconds() {
        return 3600; // 1 hour
    }

    private long getRefreshTokenExpirationSeconds() {
        return getRefreshTokenExpirationSeconds(false);
    }

    private long getRefreshTokenExpirationSeconds(boolean rememberMe) {
        return rememberMe ? 2592000 : 604800; // 30 days if remember me, else 7 days
    }
}