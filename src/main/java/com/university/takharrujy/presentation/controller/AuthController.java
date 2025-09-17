package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.AuthenticationService;
import com.university.takharrujy.infrastructure.security.CustomUserDetailsService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.auth.*;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication, registration, and password management with Arabic language support
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${takharrujy.auth.require-email-verification:false}")
    private boolean requireEmailVerification;

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
        summary = "User Registration",
        description = "Register a new user with university email validation and Arabic language support"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid registration data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("User registration request for email: {}", request.email());
        
        UserResponse userResponse = authenticationService.registerUser(request, httpRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userResponse, "User registered successfully. Please check your email for verification instructions."));
    }

    @Operation(
        summary = "Simple User Registration",
        description = "Simplified registration endpoint for frontend forms with automatic name parsing"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid registration data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/register/simple")
    public ResponseEntity<ApiResponse<UserResponse>> registerSimple(
            @Valid @RequestBody SimpleRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Simple user registration request for email: {}", request.email());
        
        // Validate password confirmation
        if (!request.isPasswordConfirmed()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        UserResponse userResponse = authenticationService.registerUserSimple(
            request.email(),
            request.password(),
            request.getFirstName(),
            request.getLastName(),
            request.role(),
            request.universityId(),
            request.getPreferredLanguage(),
            httpRequest
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userResponse, "User registered successfully. Please check your email for verification instructions."));
    }

    @Operation(
        summary = "User Login",
        description = "Authenticate user and return JWT tokens"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Email not verified",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Login attempt for email: {}", request.email());
        
        AuthResponse authResponse = authenticationService.login(request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(authResponse, "Login successful")
        );
    }

    @Operation(
        summary = "Refresh Access Token",
        description = "Refresh JWT access token using refresh token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid refresh token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        logger.debug("Token refresh request");
        
        AuthResponse authResponse = authenticationService.refreshToken(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(authResponse, "Token refreshed successfully")
        );
    }

    @Operation(
        summary = "User Logout",
        description = "Logout user and invalidate session and tokens"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest request) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        String username = userPrincipal.getUsername();
        String accessToken = extractTokenFromRequest(request);
        
        logger.info("Logout request for user: {}", username);
        
        authenticationService.logout(username, accessToken);
        
        return ResponseEntity.ok(
            ApiResponse.success("Logout successful")
        );
    }

    @Operation(
        summary = "Forgot Password",
        description = "Request password reset email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset email sent if email exists",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        logger.info("Password reset request for email: {}", request.email());
        
        authenticationService.forgotPassword(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("If an account with that email exists, you will receive password reset instructions.")
        );
    }

    @Operation(
        summary = "Reset Password",
        description = "Reset password using reset token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset successful",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired reset token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        logger.info("Password reset attempt");
        
        authenticationService.resetPassword(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Password reset successful. You can now login with your new password.")
        );
    }

    @Operation(
        summary = "Verify Email",
        description = "Verify email address using verification token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        
        logger.info("Email verification attempt");
        
        if (!requireEmailVerification) {
            return ResponseEntity.ok(
                ApiResponse.success("Email verification is disabled. You can login directly.")
            );
        }
        
        authenticationService.verifyEmail(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Email verified successfully. You can now login to your account.")
        );
    }

    @Operation(
        summary = "Resend Email Verification",
        description = "Resend email verification instructions"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Verification email sent if account exists and is not verified",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        
        logger.info("Resend verification request for email: {}", request.email());
        
        if (!requireEmailVerification) {
            return ResponseEntity.ok(
                ApiResponse.success("Email verification is disabled. You can login directly.")
            );
        }
        
        authenticationService.resendEmailVerification(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("If your account exists and is not verified, you will receive new verification instructions.")
        );
    }

    /**
     * Extract JWT token from request header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}