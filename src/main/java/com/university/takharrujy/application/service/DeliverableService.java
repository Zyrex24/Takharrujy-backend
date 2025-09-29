package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Deliverable;
import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.ProjectMember;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.DeliverableStatus;
import com.university.takharrujy.domain.enums.MemberRole;
import com.university.takharrujy.domain.repository.DeliverableRepository;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.deliverable.*;
import com.university.takharrujy.presentation.dto.supervisor.ApprovalRequest;
import com.university.takharrujy.presentation.dto.supervisor.FeedbackRequest;
import com.university.takharrujy.presentation.dto.supervisor.FeedbackResponse;
import com.university.takharrujy.presentation.mapper.DeliverableMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DeliverableMapper deliverableMapper;

    public DeliverableService(DeliverableRepository deliverableRepository,
                              ProjectRepository projectRepository,
                              UserRepository userRepository,
                              DeliverableMapper deliverableMapper) {
        this.deliverableRepository = deliverableRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.deliverableMapper = deliverableMapper;
    }

    // ---------------------- Helpers ----------------------

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    private Deliverable requireDeliverable(Long deliverableId) {
        return deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable not found with id: " + deliverableId));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void ensureProjectAccess(Project project, Long userId) {
        boolean isMember = project.getMembers() != null && project.getMembers().stream()
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .anyMatch(id -> id.equals(userId));

        if (!isMember) {
            throw BusinessException.operationNotAllowed("Access denied: must be a project member");
        }
    }

    private void ensureTeamLeader(Project project, Long userId) {
        boolean isLeader = project.getMembers() != null && project.getMembers().stream()
                .anyMatch(pm -> pm.getUser() != null &&
                        pm.getUser().getId().equals(userId) &&
                        pm.getRole() == MemberRole.LEADER);

        if (!isLeader) {
            throw BusinessException.operationNotAllowed("Operation denied: Only Team Leaders can perform this action");
        }
    }

    private void ensureValidSubmit(Deliverable deliverable) {
        if (deliverable.getStatus() != DeliverableStatus.PENDING) {
            throw BusinessException.operationNotAllowed(
                    "Deliverable cannot be submitted. Current status: " + deliverable.getStatus());
        }
    }

    private void ensureValidApproval(Deliverable deliverable) {
        if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
            throw BusinessException.operationNotAllowed(
                    "Only submitted deliverables can be approved or rejected. Current status: " + deliverable.getStatus());
        }
    }

    // ---------------------- Public APIs ----------------------

    @Transactional(readOnly = true)
    public DeliverableResponse getDeliverable(Long deliverableId, Long currentUserId) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        ensureProjectAccess(deliverable.getProject(), currentUserId);
        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional
    public DeliverableResponse createDeliverable(Long projectId, DeliverableRequest request, Long currentUserId) {
        Project project = requireProject(projectId);
        User user = requireUser(currentUserId);

        ensureTeamLeader(project, currentUserId);

        validateDeliverableRequest(request);

        Deliverable deliverable = new Deliverable();
        deliverable.setTitle(request.title());
        deliverable.setDescription(request.description());
        deliverable.setDueDate(request.dueDate());
        deliverable.setProject(project);
        deliverable.setCreatedBy(user.getEmail() != null ? user.getEmail() : "Unknown");
        deliverable.setUniversityId(project.getUniversityId());
        deliverable.setStatus(DeliverableStatus.PENDING);

        deliverableRepository.save(deliverable);
        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional
    public DeliverableResponse updateDeliverable(Long deliverableId, Long currentUserId, DeliverableUpdateRequest request) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        ensureTeamLeader(deliverable.getProject(), currentUserId);

        if (request.title() != null) deliverable.setTitle(request.title());
        if (request.description() != null) deliverable.setDescription(request.description());
        if (request.dueDate() != null) deliverable.setDueDate(request.dueDate());

        deliverable.setUpdatedAt(Instant.now());
        deliverableRepository.save(deliverable);

        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional
    public void deleteDeliverable(Long deliverableId, Long currentUserId) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        ensureTeamLeader(deliverable.getProject(), currentUserId);
        deliverableRepository.delete(deliverable);
    }

    @Transactional
    public DeliverableResponse submitDeliverable(Long deliverableId, DeliverableSubmissionRequest request, Long currentUserId) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        ensureProjectAccess(deliverable.getProject(), currentUserId);
        ensureTeamLeader(deliverable.getProject(), currentUserId);
        ensureValidSubmit(deliverable);

        validateSubmissionRequest(request);

        deliverable.setStatus(DeliverableStatus.SUBMITTED);
        deliverable.setSubmittedAt(Instant.now());
        deliverable.setSubmissionNotes(request.notes());
        deliverable.setSubmissionFileUrl(request.fileUrl());

        deliverableRepository.save(deliverable);
        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional
    public DeliverableResponse provideFeedback(Long deliverableId, FeedbackRequest request) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        if (request.supervisorFeedback() == null && request.supervisorFeedbackAr() == null) {
            throw BusinessException.invalidInput("Feedback cannot be empty");
        }

        deliverable.setSupervisorFeedback(request.supervisorFeedback());
        deliverable.setSupervisorFeedbackAr(request.supervisorFeedbackAr());

        deliverableRepository.save(deliverable);
        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long deliverableId) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        return new FeedbackResponse(deliverableId, deliverable.getSupervisorFeedback(), deliverable.getSupervisorFeedbackAr());
    }

    @Transactional
    public DeliverableResponse approveDeliverable(Long deliverableId, ApprovalRequest request) {
        Deliverable deliverable = requireDeliverable(deliverableId);
        ensureValidApproval(deliverable);

        deliverable.setStatus(request.approved() ? DeliverableStatus.APPROVED : DeliverableStatus.REJECTED);
        deliverableRepository.save(deliverable);

        return deliverableMapper.toResponse(deliverable);
    }

    @Transactional(readOnly = true)
    public List<DeliverableResponse> getProjectDeliverables(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        return deliverableRepository.findByProjectId(projectId).stream()
                .map(deliverableMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliverableResponse> getPendingDeliverablesByProject(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        return deliverableRepository.findByProjectIdAndStatus(projectId, DeliverableStatus.PENDING).stream()
                .map(deliverableMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliverableResponse> getOverdueDeliverablesByProject(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        return deliverableRepository.findByProjectIdAndStatusAndDueDateBefore(projectId, DeliverableStatus.PENDING, Instant.now()).stream()
                .map(deliverableMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeliverableStatsResponse getDeliverableStats(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        long total = deliverableRepository.countByProjectId(projectId);
        long pending = deliverableRepository.countByProjectIdAndStatus(projectId, DeliverableStatus.PENDING);
        long submitted = deliverableRepository.countByProjectIdAndStatus(projectId, DeliverableStatus.SUBMITTED);
        long approved = deliverableRepository.countByProjectIdAndStatus(projectId, DeliverableStatus.APPROVED);
        long rejected = deliverableRepository.countByProjectIdAndStatus(projectId, DeliverableStatus.REJECTED);
        long overdue = deliverableRepository.countByProjectIdAndStatusAndDueDateBefore(projectId, DeliverableStatus.PENDING, Instant.now());

        return new DeliverableStatsResponse(total, pending, submitted, approved, rejected, overdue);
    }

    // ---------------------- Validation ----------------------

    private void validateDeliverableRequest(DeliverableRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw BusinessException.invalidInput("Title is required");
        }
        if (request.dueDate() == null) {
            throw BusinessException.invalidInput("Due date is required");
        }
        if (request.dueDate().isBefore(Instant.now())) {
            throw BusinessException.invalidInput("Due date cannot be in the past");
        }
    }

    private void validateSubmissionRequest(DeliverableSubmissionRequest request) {
        if ((request.notes() == null || request.notes().isBlank()) &&
                (request.fileUrl() == null || request.fileUrl().isBlank())) {
            throw BusinessException.invalidInput("Submission must have notes or file");
        }
    }
}
