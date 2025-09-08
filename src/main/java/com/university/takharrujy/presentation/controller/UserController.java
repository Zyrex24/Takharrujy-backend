package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.UserService;
import com.university.takharrujy.presentation.dto.common.ApiResponse;
import com.university.takharrujy.presentation.dto.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * User Controller
 * Handles user profile management endpoints
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User profile and preferences management")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "https://takharrujy.com"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", 
              description = "Retrieve the current authenticated user's profile information")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentProfile() {
        Long userId = getCurrentUserId();
        UserResponse user = userService.getCurrentUserProfile(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "Profile retrieved successfully", "تم استرداد الملف الشخصي بنجاح")
        );
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update user profile", 
              description = "Update the current user's profile information with Arabic language support")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Validation error - Invalid input data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        UserResponse updatedUser = userService.updateProfile(userId, request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedUser, "Profile updated successfully", "تم تحديث الملف الشخصي بنجاح")
        );
    }
    
    /**
     * Upload user avatar
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user avatar", 
              description = "Upload and set user profile picture with virus scanning and image processing")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Avatar uploaded successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid file format or size"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413", 
            description = "File too large"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @Parameter(description = "Avatar image file (JPEG, PNG, WebP, max 5MB)")
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        UserResponse updatedUser = userService.uploadAvatar(userId, file, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedUser, "Avatar uploaded successfully", "تم رفع الصورة الشخصية بنجاح")
        );
    }
    
    /**
     * Change user password
     */
    @PutMapping("/me/password")
    @Operation(summary = "Change user password", 
              description = "Change the current user's password with current password verification")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Password changed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Validation error - Passwords don't match or current password incorrect"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        userService.changePassword(userId, request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Password changed successfully", "تم تغيير كلمة المرور بنجاح")
        );
    }
    
    /**
     * Get user preferences
     */
    @GetMapping("/me/preferences")
    @Operation(summary = "Get user preferences", 
              description = "Retrieve user notification and application preferences")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Preferences retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserPreferencesResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getPreferences() {
        Long userId = getCurrentUserId();
        UserPreferencesResponse preferences = userService.getUserPreferences(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(preferences, "Preferences retrieved successfully", "تم استرداد التفضيلات بنجاح")
        );
    }
    
    /**
     * Update user preferences
     */
    @PutMapping("/me/preferences")
    @Operation(summary = "Update user preferences", 
              description = "Update user notification and application preferences")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Preferences updated successfully",
            content = @Content(schema = @Schema(implementation = UserPreferencesResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Validation error - Invalid preference values"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updatePreferences(
            @Valid @RequestBody UserPreferencesUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getCurrentUserId();
        UserPreferencesResponse updatedPreferences = userService.updatePreferences(userId, request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedPreferences, "Preferences updated successfully", "تم تحديث التفضيلات بنجاح")
        );
    }
    
    /**
     * Get user activity history
     */
    @GetMapping("/me/activity")
    @Operation(summary = "Get user activity history", 
              description = "Retrieve paginated list of user activity history")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Activity history retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserActivityResponse>>> getActivity(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter recent activities only")
            @RequestParam(defaultValue = "false") boolean recent) {
        
        Long userId = getCurrentUserId();
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivityResponse> activities;
        
        if (recent) {
            activities = userService.getRecentActivity(userId, pageable);
        } else {
            activities = userService.getUserActivity(userId, pageable);
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(activities, "Activity history retrieved successfully", "تم استرداد سجل الأنشطة بنجاح")
        );
    }
    
    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming the principal contains user ID
            // This will depend on your JWT token implementation
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    throw new SecurityException("Invalid user ID in token");
                }
            }
        }
        throw new SecurityException("No authenticated user found");
    }
}