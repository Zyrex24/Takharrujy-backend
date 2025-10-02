package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.NotificationService;
import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.presentation.dto.common.ApiResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationPreferencesRequest;
import com.university.takharrujy.presentation.dto.notification.NotificationPreferencesResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationResponse;
import com.university.takharrujy.presentation.dto.notification.NotificationStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Manage user notifications and preferences")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get user notifications", description = "Fetch all notifications for the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Fetched user notifications"));
    }

    @Operation(summary = "Get unread notifications", description = "Fetch unread notifications for the authenticated user")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(notifications, "Fetched unread notifications"));
    }

    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read by ID")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        NotificationResponse response = notificationService.markAsRead(notificationId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @Operation(summary = "Mark all notifications as read", description = "Mark all user notifications as read")
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        int updated = notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Marked " + updated + " notifications as read"));
    }

    @Operation(summary = "Delete notification", description = "Delete a specific notification by ID")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        notificationService.deleteNotification(notificationId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }

    @Operation(summary = "Clear all notifications", description = "Delete all notifications for the authenticated user")
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<String>> clearAllNotifications(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        int deleted = notificationService.clearAllNotifications(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Cleared " + deleted + " notifications successfully"));
    }

    @Operation(summary = "Get notification preferences", description = "Fetch user notification preferences")
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getPreferences(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        NotificationPreferencesResponse response = notificationService.getPreferences(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fetched notification preferences"));
    }

    @Operation(summary = "Update notification preferences", description = "Update user notification preferences")
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updatePreferences(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId,
            @RequestBody NotificationPreferencesRequest request) {
        NotificationPreferencesResponse response = notificationService.updatePreferences(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification preferences updated successfully"));
    }

    @Operation(summary = "Get notification statistics", description = "Get statistics about user notifications (total, read, unread)")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<NotificationStatsResponse>> getStats(
            @AuthenticationPrincipal(expression = "userId") Long currentUserId
    ) {
        NotificationStatsResponse stats = notificationService.getNotificationStats(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Fetched notification statistics"));
    }

}
