package com.university.takharrujy.presentation.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * DTO to return comment information
 */
@Schema(description = "Comment response")
public record CommentResponse(
    @Schema(description = "Comment ID", example = "1")
    Long id,

    @Schema(description = "Content of the comment", example = "This task needs more details")
    String content,

    @Schema(description = "Author name", example = "Abdelrahman Bakry")
    String authorName,

    @Schema(description = "Timestamp when the comment was created")
    Instant createdAt,

    @Schema(description = "Timestamp when the comment was last updated")
    Instant updatedAt
) {}
