package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for UserPreferences entity
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    /**
     * Find user preferences by user ID
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.user.id = :userId")
    Optional<UserPreferences> findByUserId(@Param("userId") Long userId);
    
    /**
     * Check if user has preferences
     */
    @Query("SELECT COUNT(up) > 0 FROM UserPreferences up WHERE up.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * Delete user preferences by user ID
     */
    @Query("DELETE FROM UserPreferences up WHERE up.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}