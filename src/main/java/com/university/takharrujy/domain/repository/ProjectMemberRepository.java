package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ProjectMember entities
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Find all members of a project
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * Find all projects where user is a member (not leader)
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<ProjectMember> findByUserId(@Param("userId") Long userId);

    /**
     * Check if user is already a member of the project
     */
    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * Delete member from project
     */
    void deleteByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * Count members in a project (excluding leader)
     */
    long countByProjectId(Long projectId);
}
