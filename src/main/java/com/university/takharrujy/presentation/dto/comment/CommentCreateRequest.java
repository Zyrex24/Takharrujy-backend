package com.university.takharrujy.presentation.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO to create a new comment
 * Task ID is provided in the URL
 */
@Schema(description = "Request to create a comment")
public record CommentCreateRequest(
    @Schema(description = "Content of the comment", example = "This task needs more details", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Comment content is required")
    String content
) {}
