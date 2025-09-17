package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.entity.ProjectMember;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.presentation.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project Mapper
 * Maps between Project entities and DTOs
 */
@Component
public class ProjectMapper {

    /**
     * Convert Project entity to ProjectResponse DTO
     */
    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) {
            return null;
        }

        // Map team leader
        ProjectResponse.TeamMemberResponse teamLeader = toTeamMemberResponse(project.getTeamLeader(), true);

        // Map supervisor
        ProjectResponse.SupervisorResponse supervisor = project.getSupervisor() != null 
            ? toSupervisorResponse(project.getSupervisor()) 
            : null;

        // Map team members
        List<ProjectResponse.TeamMemberResponse> teamMembers = project.getMembers().stream()
            .map(member -> toTeamMemberResponse(member.getUser(), false))
            .collect(Collectors.toList());

        return new ProjectResponse(
            project.getId(),
            project.getTitle(),
            project.getTitleAr(),
            project.getDescription(),
            project.getDescriptionAr(),
            project.getStatus(),
            project.getStartDate(),
            project.getDueDate(),
            project.getCompletionDate(),
            project.getProgressPercentage(),
            project.getFinalGrade(),
            teamLeader,
            supervisor,
            teamMembers,
            "FCAI-CU", // TODO: Get actual faculty name
            "Web Development", // TODO: Get actual category name
            project.getCreatedAt() != null ? 
                LocalDateTime.ofInstant(project.getCreatedAt(), java.time.ZoneId.systemDefault()) : null,
            project.getUpdatedAt() != null ? 
                LocalDateTime.ofInstant(project.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null
        );
    }

    /**
     * Convert User to TeamMemberResponse
     */
    private ProjectResponse.TeamMemberResponse toTeamMemberResponse(User user, boolean isLeader) {
        if (user == null) {
            return null;
        }

        return new ProjectResponse.TeamMemberResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getFirstNameAr(),
            user.getLastNameAr(),
            user.getEmail(),
            isLeader
        );
    }

    /**
     * Convert User to SupervisorResponse
     */
    private ProjectResponse.SupervisorResponse toSupervisorResponse(User supervisor) {
        if (supervisor == null) {
            return null;
        }

        return new ProjectResponse.SupervisorResponse(
            supervisor.getId(),
            supervisor.getFirstName(),
            supervisor.getLastName(),
            supervisor.getFirstNameAr(),
            supervisor.getLastNameAr(),
            supervisor.getEmail(),
            "Dr." // TODO: Get actual title from user profile
        );
    }
}
