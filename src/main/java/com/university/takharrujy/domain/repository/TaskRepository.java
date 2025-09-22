package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.Task;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdAndDueDateBeforeAndStatusNot(
            Long projectId,
            LocalDate date,
            TaskStatus status
    );
    List<Task> findByProjectIdAndDueDateAfterAndStatusNot(
            Long projectId,
            LocalDate date,
            TaskStatus status
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTotalTasks(Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'COMPLETED'")
    long countCompletedTasks(Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status <> 'COMPLETED'")
    long countPendingTasks(Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.dueDate < :today AND t.status <> 'COMPLETED'")
    long countOverdueTasks(Long projectId, LocalDate today);
}
