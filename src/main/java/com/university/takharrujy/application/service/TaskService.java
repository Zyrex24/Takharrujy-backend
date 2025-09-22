package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.enums.TaskStatus;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.presentation.dto.task.*;
import com.university.takharrujy.presentation.mapper.TaskMapper;
import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.MemberRole;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final TaskMapper taskMapper;

    public TaskService(ProjectRepository projectRepository,
                       TaskRepository taskRepository,
                       UserRepository userRepository,
                       UniversityRepository universityRepository,
                       TaskMapper taskMapper) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.universityRepository = universityRepository;
        this.taskMapper = taskMapper;
    }

    // Create Task
    public TaskResponse createTask(TaskCreateRequest request, Long currentUserId) {
        // Validate date range
        if (!request.isDateRangeValid()) {
            throw BusinessException.invalidInput("Start date and due date are not valid");
        }

        universityRepository.findById(request.universityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));

        User assignedUser = null;
        if (request.assignedToId() != null) {
            assignedUser = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
        }

        Task task = new Task();
        task.setUniversityId(request.universityId());
        task.setTitle(request.title());
        task.setTitleAr(request.titleAr());
        task.setDescription(request.description());
        task.setDescriptionAr(request.descriptionAr());
        task.setStartDate(request.startDate());
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority());
        task.setEstimatedHours(request.estimatedHours());
        task.setNotes(request.notes());
        task.setNotesAr(request.notesAr());
        task.setIsMilestone(request.isMilestone());
        task.setTaskOrder(request.taskOrder());

        task.setProject(project);
        task.setAssignedTo(assignedUser);

        if (request.parentTaskId() != null) {
            Task parent = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent task not found"));
            task.setParentTask(parent);
        }

        if (request.dependencyIds() != null && !request.dependencyIds().isEmpty()) {
            Set<Task> dependencies = request.dependencyIds().stream()
                    .map(depId -> taskRepository.findById(depId)
                            .orElseThrow(() -> new ResourceNotFoundException("Dependency task not found")))
                    .collect(Collectors.toSet());
            task.setDependencies(dependencies);
        }

        task.setStatus(TaskStatus.TODO);
        task.setProgressPercentage(0);

        Task saved = taskRepository.save(task);
        return taskMapper.toTaskResponse(saved);
    }

    // Get tasks for authenticated user
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForUser(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + currentUserId));

        if (user.getRole() != UserRole.STUDENT) {
            throw BusinessException.operationNotAllowed("Only students can view assigned tasks");
        }

        List<Task> tasks = taskRepository.findByAssignedTo(user);

        return tasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    // Get task details
    @Transactional(readOnly = true)
    public TaskResponse getTaskDetails(Long taskId, Long currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Project project = task.getProject();
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));

        if (!isMember) throw BusinessException.operationNotAllowed("Access denied: not a project member");

        return taskMapper.toTaskResponse(task);
    }

    // Update task
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, Long currentUserId) {
        // Validate date range
        if (!request.isDateRangeValid()) {
            throw BusinessException.invalidInput("Start date and due date are not valid");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String currentUserEmail = userRepository.findById(currentUserId)
                .map(User::getEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCreator = currentUserEmail.equals(task.getCreatedBy());
        boolean isAssignee = task.getAssignedTo() != null &&
                Objects.equals(task.getAssignedTo().getId(), currentUserId);

        if (!isCreator && !isAssignee) {
            throw BusinessException.operationNotAllowed("You are not allowed to update this task");
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.titleAr() != null) task.setTitleAr(request.titleAr());
        if (request.description() != null) task.setDescription(request.description());
        if (request.descriptionAr() != null) task.setDescriptionAr(request.descriptionAr());
        if (request.startDate() != null) task.setStartDate(request.startDate());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.estimatedHours() != null) task.setEstimatedHours(request.estimatedHours());
        if (request.actualHours() != null) task.setActualHours(request.actualHours());
        if (request.progressPercentage() != null) task.setProgressPercentage(request.progressPercentage());
        if (request.notes() != null) task.setNotes(request.notes());
        if (request.notesAr() != null) task.setNotesAr(request.notesAr());
        if (request.isMilestone() != null) task.setIsMilestone(request.isMilestone());
        if (request.taskOrder() != null) task.setTaskOrder(request.taskOrder());

        if (request.parentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent task not found"));
            task.setParentTask(parentTask);
        }

        if (request.dependencyIds() != null) {
            Set<Task> dependencies = new HashSet<>(taskRepository.findAllById(request.dependencyIds()));
            task.setDependencies(dependencies);
        }

        Task updated = taskRepository.save(task);

        return taskMapper.toTaskResponse(updated);
    }

    // Delete task
    @Transactional
    public String deleteTask(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String currentUserEmail = userRepository.findById(currentUserId)
                .map(User::getEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCreator = currentUserEmail.equals(task.getCreatedBy());
        if (!isCreator) throw BusinessException.operationNotAllowed("You are not allowed to delete this task");

        taskRepository.delete(task);

        return "Task deleted successfully";
    }

    // Update task status
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatusUpdateRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("You are not allowed to update the status of this task");
        }

        task.setStatus(TaskStatus.valueOf(request.status()));
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletionDate(LocalDate.now());
        } else {
            task.setCompletionDate(null);
        }

        Task updated = taskRepository.save(task);
        return taskMapper.toTaskResponse(updated);
    }

    // Assign task
    @Transactional
    public TaskResponse assignTask(Long taskId, TaskAssignmentRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = task.getProject();
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        if (!isMember) throw BusinessException.operationNotAllowed("Access denied: not a project member");

        boolean isTeamLeader = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId)
                        && member.getRole().equals(MemberRole.LEADER));
        if (!isTeamLeader) throw BusinessException.operationNotAllowed("Only Team Leaders can assign tasks");

        User assignee = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        boolean isAssigneeMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(assignee.getId()));
        if (!isAssigneeMember) throw BusinessException.operationNotAllowed("Assignee must be a project member");

        task.setAssignedTo(assignee);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toTaskResponse(updatedTask);
    }

    // Complete task
    @Transactional
    public TaskResponse completeTask(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getAssignedTo() == null) {
            throw BusinessException.operationNotAllowed("Task is not assigned to anyone yet");
        }
        if (!task.getAssignedTo().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("Only the assignee can mark this task as complete");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletionDate(LocalDate.now());
        task.setProgressPercentage(100);

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toTaskResponse(updatedTask);
    }

    // TODO: Task history
//    @Transactional
//    public List<TaskHistoryResponse> getTaskHistory(Long taskId) {}

    // Get dependencies
    @Transactional(readOnly = true)
    public List<TaskResponse> getTaskDependencies(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Project project = task.getProject();
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        if (!isMember) throw BusinessException.operationNotAllowed("Access denied: not a project member");

        return task.getDependencies().stream()
                .map(taskMapper::toTaskResponse)
                .toList();
    }

    // Add dependency
    @Transactional
    public TaskDependencyResponse addDependency(Long taskId, TaskDependencyRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Project project = task.getProject();
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        if (!isMember) throw BusinessException.operationNotAllowed("Access denied: not a project member");

        Task dependency = taskRepository.findById(request.dependencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Dependency task not found"));

        if (!task.getProject().getId().equals(dependency.getProject().getId())) {
            throw BusinessException.operationNotAllowed("Tasks must belong to the same project");
        }
        if (dependency.getId().equals(task.getId())) {
            throw BusinessException.operationNotAllowed("Task cannot depend on itself");
        }

        task.getDependencies().add(dependency);
        taskRepository.save(task);

        return new TaskDependencyResponse(
                dependency.getId(), dependency.getTitle(), dependency.getStatus().toString());
    }

    // Remove dependency
    @Transactional
    public String removeTaskDependency(Long taskId, Long depId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Task dependency = taskRepository.findById(depId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency task not found"));

        Project project = task.getProject();
        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUserId));
        if (!isMember) throw BusinessException.operationNotAllowed("Access denied: not a project member");

        if (!task.getDependencies().remove(dependency)) {
            throw new ResourceNotFoundException("Dependency not found for this task");
        }

        taskRepository.save(task);
        return "Dependency removed successfully";
    }

}