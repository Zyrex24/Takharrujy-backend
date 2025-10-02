package com.university.takharrujy.presentation.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request to send a chat message
 */
@Schema(name = "MessageRequest", description = "Payload to send a new chat message")
public record MessageRequest(
        @Schema(description = "Associated project ID", example = "101")
        Long projectId,

        @Schema(description = "Message content", example = "Please review the latest update.")
        String content
) { }
