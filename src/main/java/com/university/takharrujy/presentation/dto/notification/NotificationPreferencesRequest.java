package com.university.takharrujy.presentation.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User notification preferences update request")
public record NotificationPreferencesRequest(

        @Schema(description = "Receive email notifications", example = "true")
        Boolean emailNotifications,

        @Schema(description = "Receive push notifications", example = "true")
        Boolean pushNotifications,

        @Schema(description = "Receive SMS notifications", example = "false")
        Boolean smsNotifications,

        @Schema(description = "Notify about project updates", example = "true")
        Boolean projectUpdates,

        @Schema(description = "Notify when assigned to tasks", example = "true")
        Boolean taskAssignments,

        @Schema(description = "Notify about upcoming task deadlines", example = "true")
        Boolean taskDueReminders,

        @Schema(description = "Notify about project invitations", example = "true")
        Boolean projectInvitations,

        @Schema(description = "Notify about new messages", example = "true")
        Boolean newMessages,

        @Schema(description = "Notify when mentioned in a message", example = "true")
        Boolean messageMentions,

        @Schema(description = "Notify supervisors about submissions", example = "true")
        Boolean submissionNotifications,

        @Schema(description = "Notify supervisors about progress reports", example = "true")
        Boolean progressReports
) {}
