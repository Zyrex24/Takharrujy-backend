package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.enums.TaskStatus;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.task.GanttChartResponse;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import com.university.takharrujy.presentation.dto.task.TaskStatsResponse;
import com.university.takharrujy.presentation.mapper.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectTaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;

    public ProjectTaskService(TaskRepository taskRepository, TaskMapper taskMapper, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user is project member or supervisor
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember && !project.getSupervisor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or supervisor");
        }

        // Fetch tasks
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        return tasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user is project member or supervisor
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember && !project.getSupervisor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or supervisor");
        }

        LocalDate today = LocalDate.now();

        List<Task> overdueTasks = taskRepository.findByProjectIdAndDueDateBeforeAndStatusNot(
                projectId, today, TaskStatus.COMPLETED
        );

        return overdueTasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingTasks(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user is project member or supervisor
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember && !project.getSupervisor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or supervisor");
        }

        LocalDate today = LocalDate.now();

        List<Task> upcomingTasks = taskRepository.findByProjectIdAndDueDateAfterAndStatusNot(
                projectId, today, TaskStatus.COMPLETED
        );

        return upcomingTasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskStatsResponse getTaskStats(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user is project member or supervisor
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember && !project.getSupervisor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or supervisor");
        }

        long total = taskRepository.countTotalTasks(projectId);
        long completed = taskRepository.countCompletedTasks(projectId);
        long pending = taskRepository.countPendingTasks(projectId);
        long overdue = taskRepository.countOverdueTasks(projectId, LocalDate.now());

        return new TaskStatsResponse(total, completed, pending, overdue);
    }

    @Transactional(readOnly = true)
    public List<GanttChartResponse> getGanttChartData(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check if user is project member or supervisor
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember && !project.getSupervisor().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member or supervisor");
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        return tasks.stream()
                .map(task -> new GanttChartResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getStartDate(),
                        task.getDueDate(),
                        task.getStatus().toString()
                ))
                .toList();
    }
}
