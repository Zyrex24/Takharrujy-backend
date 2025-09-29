package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Deliverable;
import com.university.takharrujy.presentation.dto.deliverable.DeliverableResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeliverableMapper {

    public DeliverableResponse toResponse(Deliverable deliverable) {
        if (deliverable == null) return null;

        return new DeliverableResponse(
                deliverable.getId(),
                deliverable.getTitle(),
                deliverable.getDescription(),
                deliverable.getStatus(),
                deliverable.getDueDate(),
                deliverable.getSubmittedAt(),
                deliverable.getProject() != null ? deliverable.getProject().getId() : null,
                deliverable.getCreatedBy(),
                deliverable.getSupervisorFeedback(),
                deliverable.getSupervisorFeedbackAr(),
                deliverable.getSubmissionNotes(),
                deliverable.getSubmissionFileUrl(),
                deliverable.getCreatedAt() != null ? deliverable.getCreatedAt() : Instant.now(),
                deliverable.getUpdatedAt() != null ? deliverable.getUpdatedAt() : Instant.now()
        );
    }
}
