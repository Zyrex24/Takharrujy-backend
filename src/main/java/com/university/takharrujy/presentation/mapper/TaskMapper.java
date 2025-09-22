package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Task Mapper
 * Maps between Task entities and DTOs
 */
@Component
public class TaskMapper {

    /**
     * Convert Task entity to TaskResponse DTO
     */
    public TaskResponse toTaskResponse(Task task) {
        if (task == null) {
            return null;
        }

        // Map single assignee
        TaskResponse.AssigneeResponse assignee = task.getAssignedTo() != null
                ? toAssigneeResponse(task.getAssignedTo())
                : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getPriority(),
                task.getPriorityText(), // from Task entity
                task.getStartDate(),
                task.getDueDate(),
                task.getCompletionDate(),
                task.getProgressPercentage(),
                assignee,
                task.getProject() != null ? task.getProject().getId() : null,
                task.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(task.getCreatedAt(), java.time.ZoneId.systemDefault())
                        : null,
                task.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(task.getUpdatedAt(), java.time.ZoneId.systemDefault())
                        : null
        );
    }

    /**
     * Convert User entity to AssigneeResponse DTO
     */
    private TaskResponse.AssigneeResponse toAssigneeResponse(User user) {
        if (user == null) {
            return null;
        }

        return new TaskResponse.AssigneeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFirstNameAr(),
                user.getLastNameAr(),
                user.getEmail()
        );
    }
}
