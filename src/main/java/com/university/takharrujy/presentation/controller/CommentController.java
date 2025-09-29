package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.CommentService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.comment.CommentResponse;
import com.university.takharrujy.presentation.dto.comment.CommentCreateRequest;
import com.university.takharrujy.presentation.dto.comment.CommentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@Tag(name = "Comments", description = "Endpoints for managing task comments")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "Create a new comment",
            description = "Allows authenticated users to add a comment to a specific task.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment created successfully",
                            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task or user not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to add a comment")
            }
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse commentResponse = commentService.createComment(taskId, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentResponse, "Comment created successfully"));
    }

    @Operation(
            summary = "Update an existing comment",
            description = "Allows the comment author to update their comment.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment updated successfully",
                            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to edit this comment")
            }
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentResponse updatedComment = commentService.updateComment(commentId, currentUserId, request);

        return ResponseEntity.ok(ApiResponse.success(updatedComment, "Comment updated successfully"));
    }

    @Operation(
            summary = "Delete a comment",
            description = "Allows the comment author (or authorized user) to delete a comment.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to delete this comment")
            }
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null, "Comment deleted successfully"));
    }

    @Operation(
            summary = "Get comments for a task",
            description = "Retrieve all comments related to a specific task. Supports pagination.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CommentResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(comments, "Comments retrieved successfully"));
    }
}
