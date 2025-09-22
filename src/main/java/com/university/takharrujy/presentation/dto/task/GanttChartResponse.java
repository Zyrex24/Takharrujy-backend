package com.university.takharrujy.presentation.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record GanttChartResponse(

    @Schema(description = "Task ID", example = "123")
    Long taskId,

    @Schema(description = "Task title", example = "Prepare AI model dataset")
    String title,

    @Schema(description = "Task start date", example = "2025-02-01")
    LocalDate startDate,

    @Schema(description = "Task end/due date", example = "2025-02-10")
    LocalDate endDate,

    @Schema(description = "Task status (TODO, IN_PROGRESS, COMPLETED, BLOCKED)", example = "TODO")
    String status
) {}