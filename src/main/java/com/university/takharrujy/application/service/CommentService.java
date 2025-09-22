package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Comment;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.repository.CommentRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.comment.CommentCreateRequest;
import com.university.takharrujy.presentation.dto.comment.CommentResponse;
import com.university.takharrujy.presentation.dto.comment.CommentUpdateRequest;
import com.university.takharrujy.presentation.mapper.CommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional
    public CommentResponse createComment(Long taskId, Long currentUserId, CommentCreateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setUniversityId(author.getUniversityId());
        comment.setContent(request.content());
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        Comment saved = commentRepository.save(comment);

        return commentMapper.toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long currentUserId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("You are not allowed to edit this comment");
        }

        commentMapper.updateCommentFromRequest(comment, request.content());
        Comment updated = commentRepository.save(comment);

        return commentMapper.toCommentResponse(updated);
    }

    @Transactional
    public String deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
        return "Comment deleted successfully";
    }

    @Transactional
    public List<CommentResponse> getCommentsByTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return commentRepository.findByTaskId(taskId).stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }
}
