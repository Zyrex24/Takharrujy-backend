package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.ProjectMember;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.TaskStatus;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.presentation.dto.task.GanttChartResponse;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import com.university.takharrujy.presentation.dto.task.TaskStatsResponse;
import com.university.takharrujy.presentation.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service to provide project-level task APIs (task listing, overdue, upcoming, stats, gantt data).
 * - Enforces project membership / supervisor access
 * - Validates inputs and throws meaningful BusinessException / ResourceNotFoundException
 */
@Service
public class ProjectTaskService {

    private static final Logger log = LoggerFactory.getLogger(ProjectTaskService.class);

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;

    public ProjectTaskService(TaskRepository taskRepository, TaskMapper taskMapper, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
    }

    // ---------------------- helpers ----------------------

    /**
     * Throws ResourceNotFoundException if project missing
     */
    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    /**
     * Check access: project members OR assigned supervisor can access project-level task endpoints.
     * Throws BusinessException.operationNotAllowed when access denied.
     */
    private void ensureProjectAccess(Project project, Long currentUserId) {
        boolean isMember = project.getMembers() != null && project.getMembers().stream()
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .anyMatch(id -> id.equals(currentUserId));

        boolean isSupervisor = project.getSupervisor() != null
                && Objects.equals(project.getSupervisor().getId(), currentUserId);

        if (!isMember && !isSupervisor) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or assigned supervisor");
        }
    }

    // ---------------------- public APIs ----------------------

    /**
     * Returns all tasks for project (only project members or supervisor).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        return tasks.stream()
                .map(taskMapper::toTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns overdue tasks (dueDate < today and not completed).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        LocalDate today = LocalDate.now();
        List<Task> overdue = taskRepository.findByProjectIdAndDueDateBeforeAndStatusNot(projectId, today, TaskStatus.COMPLETED);

        if (overdue == null || overdue.isEmpty()) return List.of();

        return overdue.stream()
                .map(taskMapper::toTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns upcoming tasks (dueDate > today and not completed).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingTasks(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        LocalDate today = LocalDate.now();
        List<Task> upcoming = taskRepository.findByProjectIdAndDueDateAfterAndStatusNot(projectId, today, TaskStatus.COMPLETED);

        if (upcoming == null || upcoming.isEmpty()) return List.of();

        return upcoming.stream()
                .map(taskMapper::toTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Return aggregated task statistics for a project.
     */
    @Transactional(readOnly = true)
    public TaskStatsResponse getTaskStats(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        long total = taskRepository.countTotalTasks(projectId);
        long completed = taskRepository.countCompletedTasks(projectId);
        long pending = taskRepository.countPendingTasks(projectId);
        long overdue = taskRepository.countOverdueTasks(projectId, LocalDate.now());

        return new TaskStatsResponse(total, completed, pending, overdue);
    }

    /**
     * Gantt chart payload. Contains start & due date and status.
     */
    @Transactional(readOnly = true)
    public List<GanttChartResponse> getGanttChartData(Long projectId, Long currentUserId) {
        Project project = requireProject(projectId);
        ensureProjectAccess(project, currentUserId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (tasks == null || tasks.isEmpty()) return List.of();

        return tasks.stream()
                .map(t -> new GanttChartResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getStartDate(),
                        t.getDueDate(),
                        t.getStatus() == null ? null : t.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
