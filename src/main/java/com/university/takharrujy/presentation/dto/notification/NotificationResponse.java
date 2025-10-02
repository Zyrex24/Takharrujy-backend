package com.university.takharrujy.presentation.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Notification response object")
public record NotificationResponse(

        @Schema(description = "Notification unique identifier", example = "101")
        Long id,

        @Schema(description = "Short title of the notification", example = "New Task Assigned")
        String title,

        @Schema(description = "Detailed message of the notification", example = "You have been assigned to Task #24 in a Project")
        String message,

        @Schema(description = "Type of notification", example = "TASK")
        String type,

        @Schema(description = "Whether the notification has been read", example = "false")
        Boolean read
) {}
