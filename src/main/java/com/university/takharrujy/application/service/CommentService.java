package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Comment;
import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.CommentRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.comment.CommentCreateRequest;
import com.university.takharrujy.presentation.dto.comment.CommentResponse;
import com.university.takharrujy.presentation.dto.comment.CommentUpdateRequest;
import com.university.takharrujy.presentation.mapper.CommentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Handles creation, update, deletion, and retrieval of task comments.
 * Enforces access control: only project members, supervisor, or admins can comment.
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    // ------------------ Public APIs ------------------

    @Transactional
    public CommentResponse createComment(Long taskId, Long currentUserId, CommentCreateRequest request) {
        validateContent(request.content());

        Task task = requireTask(taskId);
        User author = requireUser(currentUserId);
        ensureTaskAccess(task, author);

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setUniversityId(author.getUniversityId());
        comment.setContent(request.content().trim());
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        Comment saved = commentRepository.save(comment);

        log.info("User {} created comment {} on task {}", currentUserId, saved.getId(), taskId);
        return commentMapper.toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long currentUserId, CommentUpdateRequest request) {
        validateContent(request.content());

        Comment comment = requireComment(commentId);
        User user = requireUser(currentUserId);

        if (!isAuthor(comment, user) && !canModerate(comment.getTask(), user)) {
            throw BusinessException.operationNotAllowed("You are not allowed to edit this comment");
        }

        commentMapper.updateCommentFromRequest(comment, request.content().trim());
        comment.setUpdatedAt(Instant.now());

        Comment updated = commentRepository.save(comment);

        log.info("User {} updated comment {}", currentUserId, commentId);
        return commentMapper.toCommentResponse(updated);
    }

    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = requireComment(commentId);
        User user = requireUser(currentUserId);

        if (!isAuthor(comment, user) && !canModerate(comment.getTask(), user)) {
            throw BusinessException.operationNotAllowed("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);

        log.info("User {} deleted comment {}", currentUserId, commentId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(Long taskId, Long currentUserId) {
        Task task = requireTask(taskId);
        User user = requireUser(currentUserId);

        ensureTaskAccess(task, user);

        List<Comment> comments = commentRepository.findByTaskId(taskId);
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    // ------------------ Helpers ------------------

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw BusinessException.invalidInput("Comment content cannot be empty");
        }
        if (content.length() > 2000) {
            throw BusinessException.invalidInput("Comment content too long (max 2000 chars)");
        }
    }

    private Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Comment requireComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void ensureTaskAccess(Task task, User user) {
        Project project = task.getProject();
        if (project == null) {
            throw BusinessException.operationNotAllowed("Task is not linked to a project");
        }

        boolean isMember = project.getMembers().stream()
                .anyMatch(m -> Objects.equals(m.getUser().getId(), user.getId()));
        boolean isSupervisor = project.getSupervisor() != null &&
                Objects.equals(project.getSupervisor().getId(), user.getId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isMember && !isSupervisor && !isAdmin) {
            throw BusinessException.operationNotAllowed("Access denied: not project member, supervisor, or admin");
        }
    }

    private boolean isAuthor(Comment comment, User user) {
        return comment.getAuthor() != null &&
                Objects.equals(comment.getAuthor().getId(), user.getId());
    }

    private boolean canModerate(Task task, User user) {
        Project project = task.getProject();
        if (project == null) return false;

        boolean supervisor = project.getSupervisor() != null &&
                Objects.equals(project.getSupervisor().getId(), user.getId());
        boolean admin = user.getRole() == UserRole.ADMIN;

        return supervisor || admin;
    }
}
