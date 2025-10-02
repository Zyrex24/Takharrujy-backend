package com.university.takharrujy.presentation.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification statistics for a user")
public record NotificationStatsResponse(

        @Schema(description = "Total number of notifications", example = "45")
        long total,

        @Schema(description = "Number of read notifications", example = "20")
        long read,

        @Schema(description = "Number of unread notifications", example = "25")
        long unread
) {}
