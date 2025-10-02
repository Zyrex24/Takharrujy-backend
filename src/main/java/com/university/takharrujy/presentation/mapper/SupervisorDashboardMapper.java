package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.enums.TaskStatus;
import com.university.takharrujy.presentation.dto.supervisor.ProjectSummaryDTO;
import com.university.takharrujy.presentation.dto.supervisor.SupervisorDashboardResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupervisorDashboardMapper {

    public SupervisorDashboardResponse toDto(List<Project> supervisedProjects) {
        long totalProjects = supervisedProjects.size();

        long totalStudents = supervisedProjects.stream()
                .mapToLong(p -> p.getMembers().size())
                .sum();

        List<Task> allTasks = supervisedProjects.stream()
                .flatMap(p -> p.getTasks().stream())
                .toList();

        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long blockedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();

        List<ProjectSummaryDTO> projectSummaries = supervisedProjects.stream()
                .map(p -> new ProjectSummaryDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getTitleAr(),
                        p.getTasks().size(),
                        p.getTasks().stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count(),
                        p.getMembers().size()
                ))
                .collect(Collectors.toList());

        return new SupervisorDashboardResponse(
                totalProjects,
                totalStudents,
                totalTasks,
                completedTasks,
                inProgressTasks,
                blockedTasks,
                projectSummaries
        );
    }
}
