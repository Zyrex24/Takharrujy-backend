package com.university.takharrujy.presentation.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Chat message response DTO
 */
@Schema(name = "ChatMessageResponse", description = "Represents a chat message in a project conversation")
public record ChatMessageResponse(

        @Schema(description = "Unique identifier of the message", example = "101")
        Long id,

        @Schema(description = "Associated project ID", example = "55")
        Long projectId,

        @Schema(description = "Sender user ID", example = "12")
        Long senderId,

        @Schema(description = "Message content", example = "Let's finalize the project proposal by tomorrow.")
        String content,

        @Schema(description = "Message timestamp in UTC", example = "2025-09-28T12:45:30Z")
        Instant timestamp,

        @Schema(description = "Read status of the message", example = "false")
        boolean read
) {
    // ✅ Manual builder implementation
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long projectId;
        private Long senderId;
        private String content;
        private Instant timestamp;
        private boolean read;

        private Builder() {}

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder projectId(Long projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder read(boolean read) {
            this.read = read;
            return this;
        }

        public ChatMessageResponse build() {
            return new ChatMessageResponse(id, projectId, senderId, content, timestamp, read);
        }
    }
}
