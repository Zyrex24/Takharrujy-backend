package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.CommentService;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.comment.CommentResponse;
import com.university.takharrujy.presentation.dto.comment.CommentCreateRequest;
import com.university.takharrujy.presentation.dto.comment.CommentUpdateRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long taskId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse commentResponse = commentService.createComment(taskId, currentUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentResponse, "Comment created successfully"));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentResponse updatedComment = commentService.updateComment(commentId, currentUserId, request);

        return ResponseEntity.ok(ApiResponse.success(updatedComment, "Comment updated Successfully"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        return ResponseEntity.ok(ApiResponse.success(commentService.deleteComment(commentId, currentUserId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long taskId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);

        return ResponseEntity.ok(ApiResponse.success(comments, "Comments retrieved successfully"));
    }
}
