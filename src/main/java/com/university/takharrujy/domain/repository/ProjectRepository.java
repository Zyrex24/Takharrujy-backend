package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.Project;
import com.university.takharrujy.domain.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entities
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find project by team leader ID
     */
    @Query("SELECT p FROM Project p WHERE p.teamLeader.id = :userId AND p.status != 'ARCHIVED'")
    Optional<Project> findCurrentProjectByTeamLeaderId(@Param("userId") Long userId);

    /**
     * Find project where user is a member or leader
     */
    @Query("SELECT p FROM Project p LEFT JOIN p.members m WHERE " +
           "(p.teamLeader.id = :userId OR m.user.id = :userId) AND p.status != 'ARCHIVED'")
    Optional<Project> findCurrentProjectByUserId(@Param("userId") Long userId);

    /**
     * Find all projects by university ID
     */
    @Query("SELECT p FROM Project p WHERE p.universityId = :universityId")
    List<Project> findByUniversityId(@Param("universityId") Long universityId);

    /**
     * Find projects by supervisor ID
     */
    @Query("SELECT p FROM Project p WHERE p.supervisor.id = :supervisorId")
    List<Project> findBySupervisorId(@Param("supervisorId") Long supervisorId);

    /**
     * Find projects by status
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Find projects by status and university
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.universityId = :universityId")
    List<Project> findByStatusAndUniversityId(@Param("status") ProjectStatus status, 
                                              @Param("universityId") Long universityId);

    /**
     * Check if user has an active project (not DRAFT, ARCHIVED, COMPLETED, REJECTED)
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p LEFT JOIN p.members m WHERE " +
           "(p.teamLeader.id = :userId OR m.user.id = :userId) AND " +
           "p.status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'IN_PROGRESS')")
    boolean hasActiveProject(@Param("userId") Long userId);

    /**
     * Count active projects for dashboard
     */
    @Query("SELECT COUNT(p) FROM Project p LEFT JOIN p.members m WHERE " +
           "(p.teamLeader.id = :userId OR m.user.id = :userId) AND " +
           "p.status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'IN_PROGRESS')")
    long countActiveProjectsByUserId(@Param("userId") Long userId);

    /**
     * Find projects pending supervisor assignment
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'SUBMITTED' AND p.supervisor IS NULL AND p.universityId = :universityId")
    List<Project> findPendingSupervisorAssignment(@Param("universityId") Long universityId);
}
