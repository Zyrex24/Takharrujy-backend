package com.university.takharrujy.presentation.mapper;

import com.university.takharrujy.domain.entity.Notification;
import com.university.takharrujy.domain.entity.UserPreferences;
import com.university.takharrujy.presentation.dto.notification.NotificationPreferencesResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().toString(),
                notification.getRead()
        );
    }

    public NotificationPreferencesResponse toPreferencesResponse(UserPreferences preferences) {
        return new NotificationPreferencesResponse(
                preferences.getEmailNotifications(),
                preferences.getPushNotifications(),
                preferences.getSmsNotifications(),
                preferences.getProjectUpdates(),
                preferences.getTaskAssignments(),
                preferences.getTaskDueReminders(),
                preferences.getProjectInvitations(),
                preferences.getNewMessages(),
                preferences.getMessageMentions(),
                preferences.getSubmissionNotifications(),
                preferences.getProgressReports()
        );
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }
}
