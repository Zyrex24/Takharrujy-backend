package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.Deliverable;
import com.university.takharrujy.domain.enums.DeliverableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {
    List<Deliverable> findByProjectId(Long projectId);

    List<Deliverable> findByProjectIdAndStatus(Long projectId, DeliverableStatus status);

    List<Deliverable> findByProjectIdAndStatusAndDueDateBefore(
            Long projectId,
            DeliverableStatus status,
            Instant date
    );

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, com.university.takharrujy.domain.enums.DeliverableStatus status);

    long countByProjectIdAndStatusAndDueDateBefore(Long projectId, com.university.takharrujy.domain.enums.DeliverableStatus status, Instant date);
}
