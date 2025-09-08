package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.enums.UserActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for UserActivity entity
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    /**
     * Find user activities by user ID with pagination
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.createdAt DESC")
    Page<UserActivity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find user activities by user ID and activity type
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType = :activityType ORDER BY ua.createdAt DESC")
    Page<UserActivity> findByUserIdAndActivityType(@Param("userId") Long userId, 
                                                   @Param("activityType") UserActivityType activityType, 
                                                   Pageable pageable);
    
    /**
     * Find user activities within a date range
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt BETWEEN :startDate AND :endDate ORDER BY ua.createdAt DESC")
    Page<UserActivity> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") Instant startDate,
                                               @Param("endDate") Instant endDate,
                                               Pageable pageable);
    
    /**
     * Find activities related to a specific resource
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.resourceType = :resourceType AND ua.resourceId = :resourceId ORDER BY ua.createdAt DESC")
    List<UserActivity> findByUserIdAndResource(@Param("userId") Long userId,
                                              @Param("resourceType") String resourceType,
                                              @Param("resourceId") Long resourceId);
    
    /**
     * Get activity count for user
     */
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * Get recent activities (last 24 hours)
     */
    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt >= :since ORDER BY ua.createdAt DESC")
    List<UserActivity> findRecentActivities(@Param("userId") Long userId, @Param("since") Instant since);
    
    /**
     * Delete old activities for a user (older than specified date)
     */
    @Query("DELETE FROM UserActivity ua WHERE ua.user.id = :userId AND ua.createdAt < :beforeDate")
    void deleteOldActivities(@Param("userId") Long userId, @Param("beforeDate") Instant beforeDate);
}