package com.university.takharrujy.domain.repository;

import com.university.takharrujy.domain.entity.Notification;
import com.university.takharrujy.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Notification Repository Interface
 * Data access layer for notification entities
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Find notifications by user
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find notifications that are not readed by user
     */
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Mark all notifications as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    int markAllAsRead(User user);


    /**
     * Clear all notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user")
    int deleteAllByUser(User user);

    /**
     * Get total number of notifications by user
     */
    long countByUser(User user);

    /**
     * Get total number of unread notifications by user
     */
    long countByUserAndReadFalse(User user);

}
