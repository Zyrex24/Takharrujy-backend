package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * University Repository Interface
 * Data access layer for University entities
 */
@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    
    /**
     * Find university by domain
     */
    Optional<University> findByDomain(String domain);
    
    /**
     * Find university by domain (case insensitive)
     */
    @Query("SELECT u FROM University u WHERE LOWER(u.domain) = LOWER(:domain)")
    Optional<University> findByDomainIgnoreCase(@Param("domain") String domain);
    
    /**
     * Find all active universities
     */
    List<University> findByIsActiveTrue();
    
    /**
     * Check if domain exists
     */
    boolean existsByDomain(String domain);
    
    /**
     * Check if domain exists (case insensitive)
     */
    @Query("SELECT COUNT(u) > 0 FROM University u WHERE LOWER(u.domain) = LOWER(:domain)")
    boolean existsByDomainIgnoreCase(@Param("domain") String domain);
    
    /**
     * Search universities by name (supports Arabic)
     */
    @Query("""
        SELECT u FROM University u 
        WHERE u.isActive = true 
        AND (
            LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.nameAr) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY u.name
    """)
    List<University> searchByName(@Param("searchTerm") String searchTerm);
}