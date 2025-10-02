package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.*;
import com.university.takharrujy.domain.enums.NotificationType;
import com.university.takharrujy.domain.enums.ProjectStatus;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.domain.repository.*;
import com.university.takharrujy.presentation.dto.admin.SupervisorAssignmentRequest;
import com.university.takharrujy.presentation.dto.project.*;
import com.university.takharrujy.presentation.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Project Service
 * Handles business logic for project operations
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository,
                          ProjectMapper projectMapper, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
        this.notificationService = notificationService;
    }

    /**
     * Create a new project
     */
    public ProjectResponse createProject(CreateProjectRequest request, Long currentUserId) {
        logger.info("Creating project: {} for user: {}", request.title(), currentUserId);

        // Validate request
        validateCreateProjectRequest(request, currentUserId);

        // Get current user (team leader)
        User teamLeader = userRepository.findById(currentUserId)
            .orElseThrow(() -> ResourceNotFoundException.user(currentUserId));

        // Validate user can create project
        if (teamLeader.getRole() != UserRole.STUDENT) {
            throw BusinessException.invalidInput("Only students can create projects");
        }

        // Check if user already has an active project
        if (projectRepository.hasActiveProject(currentUserId)) {
            throw BusinessException.invalidInput("You already have an active project");
        }

        // Create project entity
        Project project = new Project();
        project.setTitle(request.title());
        project.setTitleAr(request.titleAr());
        project.setDescription(request.description());
        project.setDescriptionAr(request.descriptionAr());
        project.setStartDate(request.expectedStartDate());
        project.setDueDate(request.expectedEndDate());
        project.setTeamLeader(teamLeader);
        project.setUniversityId(teamLeader.getUniversityId());
        
        // Set status based on saveAsDraft flag
        project.setStatus(request.isSaveAsDraft() ? ProjectStatus.DRAFT : ProjectStatus.SUBMITTED);

        // Set preferred supervisor if provided
        if (request.preferredSupervisorId() != null) {
            User supervisor = userRepository.findById(request.preferredSupervisorId())
                .orElseThrow(() -> ResourceNotFoundException.user(request.preferredSupervisorId()));
            
            if (supervisor.getRole() != UserRole.SUPERVISOR && supervisor.getRole() != UserRole.ADMIN) {
                throw BusinessException.invalidInput("Selected user is not a supervisor");
            }
            
            if (!supervisor.getUniversityId().equals(teamLeader.getUniversityId())) {
                throw BusinessException.invalidInput("Supervisor must be from the same university");
            }
            
            // For drafts, we can assign immediately. For submitted, admin will assign
            if (request.isSaveAsDraft()) {
                project.setSupervisor(supervisor);
            }
        }

        // Save project
        project = projectRepository.save(project);

        // Add team members
        if (!request.getTeamMemberIds().isEmpty()) {
            addTeamMembers(project, request.getTeamMemberIds(), teamLeader.getUniversityId());
        }

        logger.info("Successfully created project with ID: {} for user: {}", project.getId(), currentUserId);

        // Notify team members
        for (ProjectMember member : project.getMembers()) {
            notificationService.createNotification(
                    member.getUser(),
                    "New Project Added",
                    "The project '" + project.getTitle() + "' has been created and you are added as a team member.",
                    NotificationType.PROJECT_UPDATE
            );
        }

        return projectMapper.toProjectResponse(project);
    }

    /**
     * Get all projects (Only for admin)
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    /**
     * Assign supervisor to a project (Only for admin)
     */
    @Transactional
    public ProjectResponse assignSupervisor(Long projectId, SupervisorAssignmentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User supervisor = userRepository.findById(request.supervisorId())
                .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found"));

        // Check that user has SUPERVISOR role
        if (!supervisor.isSupervisor()) {
            throw BusinessException.invalidInput("User is not a supervisor");
        }

        project.setSupervisor(supervisor);
        Project saved = projectRepository.save(project);

        // Notify supervisor
        notificationService.createNotification(
                supervisor,
                "You Have Been Assigned",
                "You have been assigned as the supervisor for the project '" + project.getTitle() + "'.",
                NotificationType.PROJECT_UPDATE
        );

        // Notify team leader
        notificationService.createNotification(
                project.getTeamLeader(),
                "Supervisor Assigned",
                supervisor.getFullName() + " has been assigned as the supervisor for your project '" + project.getTitle() + "'.",
                NotificationType.PROJECT_UPDATE
        );

        return projectMapper.toProjectResponse(saved);
    }

    /**
     * Get current project dashboard for user
     */
    @Transactional(readOnly = true)
    public DashboardResponse getCurrentProjectDashboard(Long userId) {
        logger.debug("Getting dashboard for user: {}", userId);

        Optional<Project> projectOpt = projectRepository.findCurrentProjectByUserId(userId);
        
        if (projectOpt.isEmpty()) {
            // No current project - return empty dashboard
            return new DashboardResponse(null, null, getCounters(userId), getRecentActivity(userId));
        }

        Project project = projectOpt.get();
        
        // Build dashboard response
        DashboardResponse.CurrentProjectResponse currentProject = new DashboardResponse.CurrentProjectResponse(
            project.getId(),
            project.getTitle(),
            project.getTitleAr(),
            project.getStatus().getDisplayName(),
            project.getSupervisor() != null ? project.getSupervisor().getFullName() : null,
            project.getSupervisor() != null ? project.getSupervisor().getFullNameAr() : null,
            project.getTeamSize(),
            "FCAI-CU", // TODO: Get from actual faculty
            "كلية الحاسبات والذكاء الاصطناعي", // TODO: Get from actual faculty
            project.getDueDate()
        );

        DashboardResponse.ProgressResponse progress = new DashboardResponse.ProgressResponse(
            project.getProgressPercentage() != null ? project.getProgressPercentage() : 0,
            calculatePhaseProgress(project)
        );

        return new DashboardResponse(currentProject, progress, getCounters(userId), getRecentActivity(userId));
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, Long userId) {
        logger.debug("Getting project: {} for user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        // Check access permissions
        if (!canUserAccessProject(project, userId)) {
            throw BusinessException.operationNotAllowed("You don't have access to this project");
        }

        return projectMapper.toProjectResponse(project);
    }

    /**
     * Submit project (transition from DRAFT to SUBMITTED)
     */
    public ProjectResponse submitProject(Long projectId, Long userId) {
        logger.info("Submitting project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        // Validate user can submit
        if (!project.getTeamLeader().getId().equals(userId)) {
            throw BusinessException.operationNotAllowed("Only team leader can submit the project");
        }

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw BusinessException.invalidInput("Only draft projects can be submitted");
        }

        // Validate project is complete enough for submission
        validateProjectForSubmission(project);

        project.setStatus(ProjectStatus.SUBMITTED);
        project = projectRepository.save(project);

        logger.info("Successfully submitted project: {}", projectId);

        // Notify supervisor
        if (project.getSupervisor() != null) {
            notificationService.createNotification(
                    project.getSupervisor(),
                    "Project Submitted",
                    project.getTeamLeader().getFullName() + " submitted the project '" + project.getTitle() + "' for your review.",
                    NotificationType.PROJECT_UPDATE
            );
        }

        // Notify team members
        for (ProjectMember member : project.getMembers()) {
            notificationService.createNotification(
                    member.getUser(),
                    "Project Submitted",
                    "The project '" + project.getTitle() + "' has been submitted by the team leader.",
                    NotificationType.PROJECT_UPDATE
            );
        }


        return projectMapper.toProjectResponse(project);
    }

    /**
     * Update project (only for DRAFT or IN_PROGRESS)
     */
    public ProjectResponse updateProject(Long projectId, CreateProjectRequest request, Long userId) {
        logger.info("Updating project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.project(projectId));

        // Check permissions
        if (!project.canBeEditedBy(userRepository.findById(userId).orElse(null))) {
            throw BusinessException.operationNotAllowed("You don't have permission to edit this project");
        }

        // Update fields
        project.setTitle(request.title());
        project.setTitleAr(request.titleAr());
        project.setDescription(request.description());
        project.setDescriptionAr(request.descriptionAr());
        project.setStartDate(request.expectedStartDate());
        project.setDueDate(request.expectedEndDate());

        // Update team members if in DRAFT status
        if (project.getStatus() == ProjectStatus.DRAFT) {
            // Remove existing members
            projectMemberRepository.deleteAll(project.getMembers());
            project.getMembers().clear();
            
            // Add new members
            if (!request.getTeamMemberIds().isEmpty()) {
                addTeamMembers(project, request.getTeamMemberIds(), project.getUniversityId());
            }
        }

        project = projectRepository.save(project);

        logger.info("Successfully updated project: {}", projectId);

        // Notify team members
        for (ProjectMember member : project.getMembers()) {
            notificationService.createNotification(
                    member.getUser(),
                    "Project Updated",
                    "The project '" + project.getTitle() + "' has been updated.",
                    NotificationType.PROJECT_UPDATE
            );
        }

        return projectMapper.toProjectResponse(project);
    }

    // Private helper methods

    private void validateCreateProjectRequest(CreateProjectRequest request, Long currentUserId) {
        // Validate date range
        if (!request.isDateRangeValid()) {
            throw BusinessException.invalidInput("End date must be after start date");
        }

        // Validate team members
        if (!request.getTeamMemberIds().isEmpty()) {
            // Check team size (max 4 including leader)
            if (request.getTeamMemberIds().size() > 3) {
                throw BusinessException.invalidInput("Maximum 3 additional team members allowed");
            }

            // Validate team members exist and are students
            for (Long memberId : request.getTeamMemberIds()) {
                if (memberId.equals(currentUserId)) {
                    throw BusinessException.invalidInput("Cannot add yourself as a team member");
                }

                User member = userRepository.findById(memberId)
                    .orElseThrow(() -> ResourceNotFoundException.user(memberId));

                if (member.getRole() != UserRole.STUDENT) {
                    throw BusinessException.invalidInput("Team members must be students");
                }

                // Check if member already has an active project
                if (projectRepository.hasActiveProject(memberId)) {
                    throw BusinessException.invalidInput("Team member " + member.getFullName() + " already has an active project");
                }
            }
        }
    }

    private void addTeamMembers(Project project, List<Long> memberIds, Long universityId) {
        for (Long memberId : memberIds) {
            User member = userRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.user(memberId));

            if (!member.getUniversityId().equals(universityId)) {
                throw BusinessException.invalidInput("All team members must be from the same university");
            }

            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(project);
            projectMember.setUser(member);
            projectMember.setJoinedAt(Instant.now());
            
            project.getMembers().add(projectMember);
        }
    }

    private void validateProjectForSubmission(Project project) {
        if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
            throw BusinessException.invalidInput("Project title is required for submission");
        }
        
        if (project.getDescription() == null || project.getDescription().length() < 50) {
            throw BusinessException.invalidInput("Project description must be at least 50 characters for submission");
        }
        
        if (project.getStartDate() == null || project.getDueDate() == null) {
            throw BusinessException.invalidInput("Start and end dates are required for submission");
        }
    }

    private boolean canUserAccessProject(Project project, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        // Admin can access all projects in their university
        if (user.isAdmin() && user.getUniversityId().equals(project.getUniversityId())) {
            return true;
        }

        // Supervisor can access assigned projects
        if (user.isSupervisor() && project.getSupervisor() != null && 
            project.getSupervisor().getId().equals(userId)) {
            return true;
        }

        // Team leader can access
        if (project.getTeamLeader().getId().equals(userId)) {
            return true;
        }

        // Team members can access
        return project.getMembers().stream()
            .anyMatch(member -> member.getUser().getId().equals(userId));
    }

    private DashboardResponse.PhaseProgress calculatePhaseProgress(Project project) {
        // TODO: Calculate based on actual task completion by phase
        // For now, return sample data
        return new DashboardResponse.PhaseProgress(100, 45, 0);
    }

    private DashboardResponse.CountersResponse getCounters(Long userId) {
        long activeProjects = projectRepository.countActiveProjectsByUserId(userId);
        // TODO: Get actual counts from task and notification services
        return new DashboardResponse.CountersResponse(activeProjects, 8, 3, 5);
    }

    private List<DashboardResponse.ActivityResponse> getRecentActivity(Long userId) {
        // TODO: Get actual activity from activity service
        List<DashboardResponse.ActivityResponse> activities = new ArrayList<>();
        activities.add(new DashboardResponse.ActivityResponse(
            "TASK_COMPLETED", "Database Design", "تصميم قاعدة البيانات",
            "Task completed successfully", "تم إنجاز المهمة بنجاح",
            null, null, "2 hours ago", LocalDateTime.now().minusHours(2).toString()
        ));
        return activities;
    }
}
