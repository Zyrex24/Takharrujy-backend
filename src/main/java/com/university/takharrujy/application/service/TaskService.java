package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.MemberRole;
import com.university.takharrujy.domain.enums.TaskStatus;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.TaskRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.task.*;
import com.university.takharrujy.presentation.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       UniversityRepository universityRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.universityRepository = universityRepository;
        this.taskMapper = taskMapper;
    }

    // --- CREATE ----------------------------------------------------------------
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Long currentUserId) {
        if (request.universityId() != null) {
            universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new ResourceNotFoundException("University not found"));
        }

        Project project = getProjectOrThrow(request.projectId());
        assertProjectMember(project, currentUserId);

        User assignedUser = getOptionalAssignee(request.assignedToId(), project);

        validateUniqueTitle(project, request.title());
        Task parentTask = getValidParentTask(request.parentTaskId(), project);
        Set<Task> dependencies = getValidDependencies(request.dependencyIds(), project, null);

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
        task.setIsMilestone(Boolean.TRUE.equals(request.isMilestone()));
        task.setTaskOrder(request.taskOrder());
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setParentTask(parentTask);
        task.setDependencies(dependencies);
        task.setStatus(TaskStatus.TODO);
        task.setProgressPercentage(0);
        task.setCreatedBy(getUserEmailOrId(currentUserId));

        if (hasDependencyCycle(task)) {
            throw BusinessException.operationNotAllowed("Dependency cycle detected");
        }

        Task saved = taskRepository.save(task);
        log.info("Task [{}] created by user [{}] in project [{}]", saved.getId(), currentUserId, project.getId());
        return taskMapper.toTaskResponse(saved);
    }

    // --- READ ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForUser(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);
        if (user.getRole() != UserRole.STUDENT) {
            throw BusinessException.operationNotAllowed("Only students can view assigned tasks");
        }
        List<Task> tasks = taskRepository.findByAssignedTo(user);
        return tasks.stream().map(taskMapper::toTaskResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskDetails(Long taskId, Long currentUserId) {
        getUserOrThrow(currentUserId);
        Task task = getTaskOrThrow(taskId);
        assertProjectMember(task.getProject(), currentUserId);
        return taskMapper.toTaskResponse(task);
    }

    // --- UPDATE ----------------------------------------------------------------
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        authorizeUpdate(task, currentUserId);

        applyTaskUpdates(task, request);

        Task parentTask = getValidParentTask(request.parentTaskId(), task.getProject());
        task.setParentTask(parentTask);

        if (request.dependencyIds() != null) {
            Set<Task> dependencies = getValidDependencies(request.dependencyIds(), task.getProject(), task.getId());
            task.setDependencies(dependencies);
            if (hasDependencyCycle(task)) {
                throw BusinessException.operationNotAllowed("Dependency cycle detected");
            }
        }

        try {
            Task updated = taskRepository.save(task);
            log.info("Task [{}] updated by user [{}]", updated.getId(), currentUserId);
            return taskMapper.toTaskResponse(updated);
        } catch (OptimisticLockingFailureException ex) {
            log.warn("Optimistic lock error while updating task [{}] by user [{}]", taskId, currentUserId, ex);
            throw BusinessException.operationNotAllowed("Task was modified concurrently. Please reload and retry.");
        }
    }

    // --- DELETE ----------------------------------------------------------------
    @Transactional
    public void deleteTask(Long taskId, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        String currentUserEmail = getUserEmailOrId(currentUserId);

        if (!currentUserEmail.equals(task.getCreatedBy())) {
            throw BusinessException.operationNotAllowed("You are not allowed to delete this task");
        }

        if (taskRepository.existsByDependencies(task)) {
            throw BusinessException.resourceInUse("Task cannot be deleted while other tasks depend on it");
        }

        taskRepository.delete(task);
        log.info("Task [{}] deleted by user [{}]", taskId, currentUserId);
    }

    // --- STATUS / ASSIGNMENT ---------------------------------------------------
    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, TaskStatusUpdateRequest request, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        if (!isAssignedTo(task, currentUserId)) {
            throw BusinessException.operationNotAllowed("You are not allowed to update the status of this task");
        }

        TaskStatus newStatus = parseTaskStatus(request.status());
        if (!isValidStatusTransition(task.getStatus(), newStatus)) {
            throw BusinessException.operationNotAllowed("Invalid task status transition");
        }

        task.setStatus(newStatus);
        if (TaskStatus.COMPLETED.equals(newStatus)) {
            task.setCompletionDate(LocalDate.now());
            task.setProgressPercentage(100);
        } else if (task.getCompletionDate() != null) {
            task.setCompletionDate(null);
        }

        Task updated = taskRepository.save(task);
        log.info("Task [{}] status changed to [{}] by user [{}]", taskId, newStatus, currentUserId);
        return taskMapper.toTaskResponse(updated);
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, TaskAssignmentRequest request, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        Project project = task.getProject();
        assertProjectMember(project, currentUserId);
        if (!isTeamLeader(project, currentUserId)) {
            throw BusinessException.operationNotAllowed("Only Team Leaders can assign tasks");
        }

        User assignee = getUserOrThrow(request.assignedToId());
        if (!isProjectMember(project, assignee.getId())) {
            throw BusinessException.operationNotAllowed("Assignee must be a project member");
        }

        task.setAssignedTo(assignee);
        Task updatedTask = taskRepository.save(task);
        log.info("Task [{}] assigned to user [{}] by leader [{}]", taskId, assignee.getId(), currentUserId);
        return taskMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public TaskResponse completeTask(Long taskId, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        if (task.getAssignedTo() == null || !Objects.equals(task.getAssignedTo().getId(), currentUserId)) {
            throw BusinessException.operationNotAllowed("Only the assignee can mark this task as complete");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletionDate(LocalDate.now());
        task.setProgressPercentage(100);

        Task updatedTask = taskRepository.save(task);
        log.info("Task [{}] completed by user [{}]", taskId, currentUserId);
        return taskMapper.toTaskResponse(updatedTask);
    }

    // --- DEPENDENCIES ----------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TaskResponse> getTaskDependencies(Long taskId, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        assertProjectMember(task.getProject(), currentUserId);
        return task.getDependencies().stream().map(taskMapper::toTaskResponse).toList();
    }

    @Transactional
    public TaskDependencyResponse addDependency(Long taskId, TaskDependencyRequest request, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        assertProjectMember(task.getProject(), currentUserId);

        Task dependency = getTaskOrThrow(request.dependencyId());
        if (!task.getProject().getId().equals(dependency.getProject().getId())) {
            throw BusinessException.operationNotAllowed("Tasks must belong to the same project");
        }

        if (dependency.getId().equals(task.getId())) {
            throw BusinessException.invalidInput("Task cannot depend on itself");
        }

        Set<Task> deps = new HashSet<>(task.getDependencies());
        deps.add(dependency);
        task.setDependencies(deps);

        if (hasDependencyCycle(task)) {
            throw BusinessException.operationNotAllowed("Adding this dependency would create a cycle");
        }

        taskRepository.save(task);
        log.info("Dependency [{}] added to task [{}] by user [{}]", dependency.getId(), taskId, currentUserId);
        return new TaskDependencyResponse(dependency.getId(), dependency.getTitle(), dependency.getStatus().name());
    }

    @Transactional
    public void removeTaskDependency(Long taskId, Long depId, Long currentUserId) {
        Task task = getTaskOrThrow(taskId);
        assertProjectMember(task.getProject(), currentUserId);

        Task dependency = getTaskOrThrow(depId);
        if (!task.getDependencies().remove(dependency)) {
            throw new ResourceNotFoundException("Dependency not found for this task");
        }

        taskRepository.save(task);
        log.info("Dependency [{}] removed from task [{}] by user [{}]", depId, taskId, currentUserId);
    }

    // --- HELPERS ---------------------------------------------------------------
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void assertProjectMember(Project project, Long userId) {
        if (!isProjectMember(project, userId)) {
            throw BusinessException.operationNotAllowed("Access denied: not a project member");
        }
    }

    private boolean isProjectMember(Project project, Long userId) {
        return project.getMembers().stream().anyMatch(pm -> pm.getUser().getId().equals(userId));
    }

    private boolean isTeamLeader(Project project, Long userId) {
        return project.getMembers().stream()
                .anyMatch(pm -> pm.getUser().getId().equals(userId) && pm.getRole() == MemberRole.LEADER);
    }

    private User getOptionalAssignee(Long assignedToId, Project project) {
        if (assignedToId == null) return null;
        User user = getUserOrThrow(assignedToId);
        if (!isProjectMember(project, user.getId())) {
            throw BusinessException.operationNotAllowed("Assignee must be a project member");
        }
        return user;
    }

    private void validateUniqueTitle(Project project, String title) {
        if (title != null && project.getTasks() != null &&
                project.getTasks().stream().anyMatch(t -> title.equalsIgnoreCase(t.getTitle()))) {
            throw BusinessException.duplicateResource("Task title must be unique within the project");
        }
    }

    private Task getValidParentTask(Long parentId, Project project) {
        if (parentId == null) return null;
        Task parent = getTaskOrThrow(parentId);
        if (!Objects.equals(parent.getProject().getId(), project.getId())) {
            throw BusinessException.operationNotAllowed("Parent task must belong to the same project");
        }
        return parent;
    }

    private Set<Task> getValidDependencies(Set<Long> depIds, Project project, Long selfId) {
        if (depIds == null || depIds.isEmpty()) return new HashSet<>();
        Set<Task> deps = new HashSet<>();
        for (Long depId : depIds) {
            Task dep = getTaskOrThrow(depId);
            if (!Objects.equals(dep.getProject().getId(), project.getId())) {
                throw BusinessException.operationNotAllowed("All dependencies must belong to the same project");
            }
            if (selfId != null && depId.equals(selfId)) {
                throw BusinessException.invalidInput("Task cannot depend on itself");
            }
            deps.add(dep);
        }
        return deps;
    }

    private String getUserEmailOrId(Long userId) {
        return userRepository.findById(userId).map(User::getEmail).orElse(String.valueOf(userId));
    }

    private void authorizeUpdate(Task task, Long userId) {
        boolean isCreator = getUserEmailOrId(userId).equals(task.getCreatedBy());
        boolean isAssignee = task.getAssignedTo() != null && Objects.equals(task.getAssignedTo().getId(), userId);
        boolean isTeamLeader = isTeamLeader(task.getProject(), userId);

        if (!isCreator && !isAssignee && !isTeamLeader) {
            throw BusinessException.operationNotAllowed("You are not allowed to update this task");
        }
    }

    private boolean isAssignedTo(Task task, Long userId) {
        return task.getAssignedTo() != null && task.getAssignedTo().getId().equals(userId);
    }

    private TaskStatus parseTaskStatus(String status) {
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.invalidInput("Unknown task status: " + status);
        }
    }

    private boolean hasDependencyCycle(Task root) {
        Map<Long, Set<Long>> adj = new HashMap<>();
        List<Task> projectTasks = taskRepository.findByProjectId(root.getProject().getId());

        for (Task t : projectTasks) {
            adj.put(t.getId(), t.getDependencies().stream().map(Task::getId).collect(Collectors.toSet()));
        }

        adj.put(root.getId(), root.getDependencies().stream().map(Task::getId).collect(Collectors.toSet()));

        Set<Long> visited = new HashSet<>();
        Set<Long> stack = new HashSet<>();

        for (Long node : adj.keySet()) {
            if (dfsCycle(node, adj, visited, stack)) return true;
        }
        return false;
    }

    private boolean dfsCycle(Long node, Map<Long, Set<Long>> adj, Set<Long> visited, Set<Long> stack) {
        if (stack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        stack.add(node);

        for (Long neigh : adj.getOrDefault(node, Collections.emptySet())) {
            if (dfsCycle(neigh, adj, visited, stack)) return true;
        }

        stack.remove(node);
        return false;
    }

    private boolean isValidStatusTransition(TaskStatus from, TaskStatus to) {
        if (from == null) return true;
        if (from == to) return true;

        return switch (from) {
            case TODO -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.TODO).contains(to);
            case IN_PROGRESS -> EnumSet.of(TaskStatus.COMPLETED, TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS).contains(to);
            case BLOCKED -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.TODO).contains(to);
            case REVIEW -> false;
            case COMPLETED -> EnumSet.of(TaskStatus.IN_PROGRESS).contains(to);
            case CANCELLED -> false;
        };
    }

    private void applyTaskUpdates(Task task, TaskUpdateRequest request) {
        if (request.title() != null) task.setTitle(request.title());
        if (request.titleAr() != null) task.setTitleAr(request.titleAr());
        if (request.description() != null) task.setDescription(request.description());
        if (request.descriptionAr() != null) task.setDescriptionAr(request.descriptionAr());
        if (request.startDate() != null) task.setStartDate(request.startDate());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.estimatedHours() != null) task.setEstimatedHours(request.estimatedHours());
        if (request.notes() != null) task.setNotes(request.notes());
        if (request.notesAr() != null) task.setNotesAr(request.notesAr());
        if (request.isMilestone() != null) task.setIsMilestone(request.isMilestone());
        if (request.taskOrder() != null) task.setTaskOrder(request.taskOrder());
        if (request.progressPercentage() != null) task.setProgressPercentage(request.progressPercentage());
    }
}
