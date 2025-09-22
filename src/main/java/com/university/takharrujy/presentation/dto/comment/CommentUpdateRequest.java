package com.university.takharrujy.presentation.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO to update an existing comment
 */
@Schema(description = "Request to update a comment")
public record CommentUpdateRequest(
    @Schema(description = "Updated content of the comment", example = "Updated comment content", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment content is required")
    String content
) {}
