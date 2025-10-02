package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.Notification;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserPreferences;
import com.university.takharrujy.domain.enums.NotificationType;
import com.university.takharrujy.domain.repository.NotificationRepository;
import com.university.takharrujy.domain.repository.UserPreferencesRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.notification.NotificationPreferencesRequest;
import com.university.takharrujy.presentation.dto.notification.NotificationPreferencesResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationStatsResponse;
import com.university.takharrujy.presentation.mapper.NotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper, UserPreferencesRepository userPreferencesRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create notifications for a specific user.
     */
    public void createNotification(User targetUser, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(targetUser);
        notification.setUniversityId(targetUser.getUniversityId());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    /**
     * Get notifications for a specific user.
     * Returns an empty list if the user has no notifications.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        List<Notification> notifications =
                notificationRepository.findByUserOrderByCreatedAtDesc(user);

        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Get notifications that are not readed for a specific user.
     * Returns an empty list if the user has no notifications.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        List<Notification> notifications =
                notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);

        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public int markAllAsRead(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        return notificationRepository.markAllAsRead(user);
    }

    /**
     * Clear all notifications
     */
    @Transactional
    public int clearAllNotifications(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        return notificationRepository.deleteAllByUser(user);
    }

    /**
     * Mark a notification as read by its ID and user.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (notification.getUser() == null || notification.getUser().getId() == null || !notification.getUser().getId().equals(user.getId())) {
            throw BusinessException.operationNotAllowed("You are not allowed to access this notification");
        }


        notification.setRead(true);
        notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    /**
     * Delete a notification for a user.
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (notification.getUser() == null || !notification.getUser().getId().equals(currentUserId)) {
            throw BusinessException.operationNotAllowed("You are not allowed to delete this notification");
        }


        notificationRepository.delete(notification);
    }

    /**
     * Get notification preferences
     */
    @Transactional
    public NotificationPreferencesResponse getPreferences(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        UserPreferences prefs = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> userPreferencesRepository.save(defaultPreferences(user)));

        return notificationMapper.toPreferencesResponse(prefs);
    }

    /**
     * Update notification preferences
     */
    @Transactional
    public NotificationPreferencesResponse updatePreferences(Long currentUserId, NotificationPreferencesRequest request) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        UserPreferences prefs = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> userPreferencesRepository.save(defaultPreferences(user)));

        updateNotificationPreferences(prefs, request);
        userPreferencesRepository.save(prefs);

        return notificationMapper.toPreferencesResponse(prefs);
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats(Long currentUserId) {
        User user = getUserOrThrow(currentUserId);

        validateUser(user);

        long total = notificationRepository.countByUser(user);
        long unread = notificationRepository.countByUserAndReadFalse(user);
        long read = total - unread;

        return new NotificationStatsResponse(total, read, unread);
    }

    /**
     * Helper methods
     */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    private void validateUser(User user) {
        Assert.notNull(user, "User must not be null");
        Assert.notNull(user.getId(), "User ID must not be null");
    }

    private UserPreferences defaultPreferences(User user) {
        UserPreferences prefs = new UserPreferences();

        prefs.setUser(user);
        prefs.setUniversityId(user.getUniversityId());

        prefs.setEmailNotifications(true);
        prefs.setPushNotifications(true);
        prefs.setSmsNotifications(false);
        prefs.setProjectUpdates(true);
        prefs.setTaskAssignments(true);
        prefs.setTaskDueReminders(true);
        prefs.setProjectInvitations(true);
        prefs.setNewMessages(true);
        prefs.setMessageMentions(true);
        prefs.setSubmissionNotifications(true);
        prefs.setProgressReports(true);

        return prefs;
    }

    public void updateNotificationPreferences(UserPreferences prefs, NotificationPreferencesRequest request) {
        if (request.emailNotifications() != null) prefs.setEmailNotifications(request.emailNotifications());
        if (request.pushNotifications() != null) prefs.setPushNotifications(request.pushNotifications());
        if (request.smsNotifications() != null) prefs.setSmsNotifications(request.smsNotifications());
        if (request.projectUpdates() != null) prefs.setProjectUpdates(request.projectUpdates());
        if (request.taskAssignments() != null) prefs.setTaskAssignments(request.taskAssignments());
        if (request.taskDueReminders() != null) prefs.setTaskDueReminders(request.taskDueReminders());
        if (request.projectInvitations() != null) prefs.setProjectInvitations(request.projectInvitations());
        if (request.newMessages() != null) prefs.setNewMessages(request.newMessages());
        if (request.messageMentions() != null) prefs.setMessageMentions(request.messageMentions());
        if (request.submissionNotifications() != null) prefs.setSubmissionNotifications(request.submissionNotifications());
        if (request.progressReports() != null) prefs.setProgressReports(request.progressReports());
    }
}
