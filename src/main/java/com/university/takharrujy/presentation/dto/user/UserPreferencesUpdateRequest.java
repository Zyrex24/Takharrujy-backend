package com.university.takharrujy.presentation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating user preferences
 */
@Schema(description = "User preferences update request")
public record UserPreferencesUpdateRequest(
    
    // Notification preferences
    @Schema(description = "Email notifications enabled")
    Boolean emailNotifications,
    
    @Schema(description = "Push notifications enabled")
    Boolean pushNotifications,
    
    @Schema(description = "SMS notifications enabled")
    Boolean smsNotifications,
    
    // Project notifications
    @Schema(description = "Project updates notifications")
    Boolean projectUpdates,
    
    @Schema(description = "Task assignments notifications")
    Boolean taskAssignments,
    
    @Schema(description = "Task due reminders notifications")
    Boolean taskDueReminders,
    
    @Schema(description = "Project invitations notifications")
    Boolean projectInvitations,
    
    // Communication notifications
    @Schema(description = "New messages notifications")
    Boolean newMessages,
    
    @Schema(description = "Message mentions notifications")
    Boolean messageMentions,
    
    // Supervisor-specific notifications
    @Schema(description = "Submission notifications")
    Boolean submissionNotifications,
    
    @Schema(description = "Progress reports notifications")
    Boolean progressReports,
    
    // UI preferences
    @Schema(description = "Theme preference", allowableValues = {"auto", "light", "dark"})
    @Pattern(regexp = "^(auto|light|dark)$", message = "Theme must be 'auto', 'light', or 'dark'")
    String theme,
    
    @Schema(description = "Language preference", allowableValues = {"ar", "en"})
    @Pattern(regexp = "^(ar|en)$", message = "Language must be 'ar' or 'en'")
    String language,
    
    @Schema(description = "Timezone", example = "Africa/Cairo")
    String timezone,
    
    // Privacy preferences
    @Schema(description = "Profile visibility", allowableValues = {"public", "university", "private"})
    @Pattern(regexp = "^(public|university|private)$", message = "Profile visibility must be 'public', 'university', or 'private'")
    String profileVisibility,
    
    @Schema(description = "Show email in profile")
    Boolean showEmail,
    
    @Schema(description = "Show phone in profile")
    Boolean showPhone
) {}