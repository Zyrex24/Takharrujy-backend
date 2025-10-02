package com.university.takharrujy.presentation.dto.supervisor;

import com.university.takharrujy.presentation.dto.task.TaskResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Project overview response for supervisors")
public record ProjectOverviewResponse(

        @Schema(description = "Project ID", example = "10")
        Long id,

        @Schema(description = "Project title", example = "AI Graduation Project")
        String title,

        @Schema(description = "Project description", example = "Developing an AI model for image recognition")
        String description,

        @Schema(description = "Supervisor ID", example = "5")
        Long supervisorId,

        @Schema(description = "Team members assigned to this project")
        List<String> teamMembers,

        @Schema(description = "Tasks under this project")
        List<TaskResponse> tasks,

        @Schema(description = "Project progress percentage", example = "60")
        Integer progressPercentage
) {}
