package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.ProjectMember;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.NotificationType;
import com.university.takharrujy.domain.enums.ProjectStatus;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.project.ProjectResponse;
import com.university.takharrujy.presentation.dto.supervisor.*;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import com.university.takharrujy.presentation.mapper.ProjectMapper;
import com.university.takharrujy.presentation.mapper.OverviewMapper;
import com.university.takharrujy.presentation.mapper.SupervisorDashboardMapper;
import com.university.takharrujy.presentation.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupervisorService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SupervisorDashboardMapper dashboardMapper;
    private final ProjectMapper projectMapper;
    private final OverviewMapper overviewMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public SupervisorService(ProjectRepository projectRepository, UserRepository userRepository,
                             SupervisorDashboardMapper dashboardMapper, ProjectMapper projectMapper,
                             OverviewMapper overviewMapper, UserMapper userMapper, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.dashboardMapper = dashboardMapper;
        this.projectMapper = projectMapper;
        this.overviewMapper = overviewMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public SupervisorDashboardResponse getDashboard(Long supervisorId) {
        return dashboardMapper.toDto(projectRepository.findBySupervisorId(supervisorId));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAssignedProjects(Long supervisorId) {
        return projectRepository.findBySupervisorId(supervisorId)
                .stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectOverviewResponse getProjectOverview(Long projectId, Long supervisorId) {
        return overviewMapper.toOverview(getProjectForSupervisor(projectId, supervisorId));
    }

    @Transactional(readOnly = true)
    public WorkloadResponse getSupervisorWorkload(Long supervisorId) {
        List<Project> projects = projectRepository.findBySupervisorId(supervisorId);

        int totalProjects = projects.size();
        int totalTasks = 0;
        int completedTasks = 0;

        for (Project project : projects) {
            if (project.getTasks() != null) {
                totalTasks += project.getTasks().size();
                completedTasks += (int) project.getTasks().stream()
                        .filter(t -> t.getStatus() != null && t.getStatus().isCompleted())
                        .count();
            }
        }

        int pendingTasks = totalTasks - completedTasks;
        int completionPercentage = (totalTasks == 0) ? 0 : (completedTasks * 100 / totalTasks);

        return new WorkloadResponse(totalProjects, totalTasks, completedTasks, pendingTasks, completionPercentage);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getSupervisedStudents(Long supervisorId) {
        return projectRepository.findBySupervisorId(supervisorId)
                .stream()
                .flatMap(project -> project.getMembers().stream())
                .map(ProjectMember::getUser)
                .filter(User::isStudent)
                .distinct()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse approveProject(Long projectId, Long supervisorId, ApprovalRequest request) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.APPROVED && request.approved()) {
            throw BusinessException.operationNotAllowed("Project is already approved");
        }
        if (project.getStatus() == ProjectStatus.REJECTED && !request.approved()) {
            throw BusinessException.operationNotAllowed("Project is already rejected");
        }

        if (request.approved()) {
            project.setSupervisor(getUser(supervisorId));
            project.setStatus(ProjectStatus.APPROVED);
        } else {
            project.setStatus(ProjectStatus.REJECTED);
        }

        // Notification for project members
        project.getMembers().forEach(pm -> {
            notificationService.createNotification(
                    pm.getUser(),
                    "Project " + (project.getStatus() == ProjectStatus.APPROVED ? "Approved" : "Rejected"),
                    "Project '" + project.getTitle() + "' has been " +
                            (project.getStatus() == ProjectStatus.APPROVED ? "approved" : "rejected") + " by the supervisor.",
                    NotificationType.PROJECT_UPDATE
            );
        });

        return projectMapper.toProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public FeedbackResponse provideFeedback(Long projectId, Long supervisorId, FeedbackRequest request) {
        Project project = getProjectForSupervisor(projectId, supervisorId);

        ensureStatus(project, ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS);

        project.setSupervisorFeedback(request.supervisorFeedback());
        project.setSupervisorFeedbackAr(request.supervisorFeedbackAr());

        Project saved = projectRepository.save(project);

        // Notification for project members
        project.getMembers().forEach(pm -> {
            notificationService.createNotification(
                    pm.getUser(),
                    "New Feedback on Project",
                    "Supervisor provided feedback on project '" + project.getTitle() + "'.",
                    NotificationType.PROJECT_UPDATE
            );
        });

        return new FeedbackResponse(saved.getId(), saved.getSupervisorFeedback(), saved.getSupervisorFeedbackAr());
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getSupervisionAnalytics(Long supervisorId) {
        long total = projectRepository.countBySupervisorId(supervisorId);
        long approved = projectRepository.countBySupervisorIdAndStatus(supervisorId, ProjectStatus.APPROVED);
        long rejected = projectRepository.countBySupervisorIdAndStatus(supervisorId, ProjectStatus.REJECTED);
        long inProgress = projectRepository.countBySupervisorIdAndStatus(supervisorId, ProjectStatus.IN_PROGRESS);
        return new AnalyticsResponse(total, approved, rejected, inProgress);
    }

    // -------------------- PRIVATE HELPERS --------------------

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Project getProjectForSupervisor(Long projectId, Long supervisorId) {
        return projectRepository.findByIdAndSupervisorId(projectId, supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or not assigned to this supervisor"));
    }

    private void ensureStatus(Project project, ProjectStatus... allowedStatuses) {
        for (ProjectStatus s : allowedStatuses) {
            if (s.equals(project.getStatus())) return;
        }
        throw BusinessException.operationNotAllowed(
                "Operation not allowed unless project is in: " + String.join(", ",
                        java.util.Arrays.stream(allowedStatuses).map(Enum::name).toList())
        );
    }
}
