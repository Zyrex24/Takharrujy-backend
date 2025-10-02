package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.presentation.dto.supervisor.ProjectOverviewResponse;
import com.university.takharrujy.presentation.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverviewMapper {

    private final TaskMapper taskMapper;

    public OverviewMapper(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public ProjectOverviewResponse toOverview(Project project) {
        List<String> teamMembers = project.getMembers().stream()
                .map(member -> {
                    var user = member.getUser();
                    return user.getFullName();
                })
                .toList();

        // Map each task individually
        List<TaskResponse> tasks = project.getTasks().stream()
                .map(taskMapper::toTaskResponse)
                .toList();

        int progress = calculateProgress(project);

        return new ProjectOverviewResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getSupervisor() != null ? project.getSupervisor().getId() : null,
                teamMembers,
                tasks,
                progress
        );
    }

    private int calculateProgress(Project project) {
        if (project.getTasks() == null || project.getTasks().isEmpty()) {
            return 0;
        }
        long completed = project.getTasks().stream()
                .filter(t -> t.getStatus() != null && t.getStatus().isCompleted())
                .count();
        return (int) ((completed * 100) / project.getTasks().size());
    }
}
