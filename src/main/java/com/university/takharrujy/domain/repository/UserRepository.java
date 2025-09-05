package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 * Data access layer for User entities with FERPA-compliant queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    /**
     * Find user by email (active users only)
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    /**
     * Find user by email (including inactive users for admin purposes)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by student ID within a university
     */
    @Query("SELECT u FROM User u WHERE u.studentId = :studentId AND u.universityId = :universityId AND u.isActive = true")
    Optional<User> findByStudentIdAndUniversityId(@Param("studentId") String studentId, @Param("universityId") Long universityId);
    
    /**
     * Check if email exists in the system
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if student ID exists within a university
     */
    boolean existsByStudentIdAndUniversityId(String studentId, Long universityId);
    
    /**
     * Find users by role within a university
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.universityId = :universityId AND u.isActive = true")
    List<User> findByRoleAndUniversityId(@Param("role") UserRole role, @Param("universityId") Long universityId);
    
    /**
     * Find users by role within a university with pagination
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.universityId = :universityId AND u.isActive = true")
    Page<User> findByRoleAndUniversityId(@Param("role") UserRole role, @Param("universityId") Long universityId, Pageable pageable);
    
    /**
     * Find users by department
     */
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.isActive = true")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * Search users by name within a university (supports Arabic text search)
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.universityId = :universityId 
        AND u.isActive = true 
        AND (
            LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(CONCAT(COALESCE(u.firstNameAr, u.firstName), ' ', COALESCE(u.lastNameAr, u.lastName))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(COALESCE(u.studentId, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
    """)
    Page<User> searchByNameOrEmail(@Param("searchTerm") String searchTerm, @Param("universityId") Long universityId, Pageable pageable);
    
    /**
     * Find unverified users older than specified days
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsersOlderThan(@Param("cutoffDate") java.time.Instant cutoffDate);
    
    /**
     * Count users by role within a university
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.universityId = :universityId AND u.isActive = true")
    long countByRoleAndUniversityId(@Param("role") UserRole role, @Param("universityId") Long universityId);
    
    /**
     * Find supervisors with project count (for workload balancing)
     */
    @Query("""
        SELECT u, COUNT(p) as projectCount FROM User u 
        LEFT JOIN u.supervisedProjects p 
        WHERE u.role = 'SUPERVISOR' 
        AND u.universityId = :universityId 
        AND u.isActive = true 
        GROUP BY u.id 
        HAVING COUNT(p) < :maxProjects 
        ORDER BY COUNT(p) ASC
    """)
    List<Object[]> findAvailableSupervisors(@Param("universityId") Long universityId, @Param("maxProjects") int maxProjects);
}