package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Department Repository Interface
 * Data access layer for Department entities
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find departments by university
     */
    @Query("SELECT d FROM Department d WHERE d.universityId = :universityId AND d.isActive = true ORDER BY d.name")
    List<Department> findByUniversityIdAndIsActiveTrue(@Param("universityId") Long universityId);
    
    /**
     * Find department by code and university
     */
    @Query("SELECT d FROM Department d WHERE d.code = :code AND d.universityId = :universityId")
    Optional<Department> findByCodeAndUniversityId(@Param("code") String code, @Param("universityId") Long universityId);
    
    /**
     * Check if department code exists within university
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.code = :code AND d.universityId = :universityId")
    boolean existsByCodeAndUniversityId(@Param("code") String code, @Param("universityId") Long universityId);
    
    /**
     * Search departments by name within university
     */
    @Query("""
        SELECT d FROM Department d 
        WHERE d.universityId = :universityId 
        AND d.isActive = true 
        AND (
            LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(d.nameAr) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY d.name
    """)
    List<Department> searchByNameAndUniversityId(@Param("searchTerm") String searchTerm, @Param("universityId") Long universityId);
}