package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProjectFileRepository
 * Repository interface for ProjectFile entity operations
 */
@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    
    /**
     * Find all files belonging to a specific project, ordered by upload date descending
     */
    List<ProjectFile> findByProjectIdOrderByUploadedAtDesc(Long projectId);
    
    /**
     * Find all files uploaded by a specific user
     */
    List<ProjectFile> findByUploadedByUserId(Long userId);
    
    /**
     * Count total files in a project
     */
    long countByProjectId(Long projectId);
}

