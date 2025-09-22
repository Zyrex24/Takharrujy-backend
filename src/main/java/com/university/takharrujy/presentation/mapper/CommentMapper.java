package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Comment;
import com.university.takharrujy.presentation.dto.comment.CommentResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CommentMapper {

    public CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getFullName(),
                comment.getCreatedAt() != null ? comment.getCreatedAt() : Instant.now(),
                comment.getUpdatedAt() != null ? comment.getUpdatedAt() : Instant.now()
        );
    }

    public void updateCommentFromRequest(Comment comment, String content) {
        comment.setContent(content);
        comment.setUpdatedAt(Instant.now());
    }
}
