package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.entity.UserActivity;
import com.university.takharrujy.domain.enums.UserActivityType;
import com.university.takharrujy.domain.repository.UserActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Activity Logging Service
 * Handles user activity logging for audit and tracking purposes
 */
@Service
@Transactional
public class ActivityLoggingService {
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
    /**
     * Log user activity with basic information
     */
    @Async
    public void logActivity(User user, UserActivityType activityType, String description, String descriptionAr, HttpServletRequest request) {
        UserActivity activity = new UserActivity(user, activityType, description, user.getUniversityId());
        activity.setDescriptionAr(descriptionAr);
        
        if (request != null) {
            activity.addRequestInfo(
                getClientIpAddress(request),
                request.getHeader("User-Agent")
            );
        }
        
        userActivityRepository.save(activity);
    }
    
    /**
     * Log user activity with resource reference
     */
    @Async
    public void logActivity(User user, UserActivityType activityType, String description, String descriptionAr, 
                          String resourceType, Long resourceId, HttpServletRequest request) {
        UserActivity activity = new UserActivity(user, activityType, description, user.getUniversityId());
        activity.setDescriptionAr(descriptionAr);
        activity.addResourceReference(resourceType, resourceId);
        
        if (request != null) {
            activity.addRequestInfo(
                getClientIpAddress(request),
                request.getHeader("User-Agent")
            );
        }
        
        userActivityRepository.save(activity);
    }
    
    /**
     * Log user activity with additional data
     */
    @Async
    public void logActivity(User user, UserActivityType activityType, String description, String descriptionAr, 
                          String resourceType, Long resourceId, String additionalData, HttpServletRequest request) {
        UserActivity activity = new UserActivity(user, activityType, description, user.getUniversityId());
        activity.setDescriptionAr(descriptionAr);
        activity.addResourceReference(resourceType, resourceId);
        activity.setAdditionalData(additionalData);
        
        if (request != null) {
            activity.addRequestInfo(
                getClientIpAddress(request),
                request.getHeader("User-Agent")
            );
        }
        
        userActivityRepository.save(activity);
    }
    
    /**
     * Cleanup old activities (older than specified days)
     */
    @Async
    public void cleanupOldActivities(Long userId, int daysToKeep) {
        Instant cutoffDate = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        userActivityRepository.deleteOldActivities(userId, cutoffDate);
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        return request.getRemoteAddr();
    }
}