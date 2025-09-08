package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.UserActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * UserActivity Entity
 * Represents user activity log entries
 */
@Entity
@Table(name = "user_activities", indexes = {
    @Index(name = "idx_user_activities_user_id", columnList = "user_id"),
    @Index(name = "idx_user_activities_activity_type", columnList = "activity_type"),
    @Index(name = "idx_user_activities_created_at", columnList = "created_at")
})
public class UserActivity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "Activity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private UserActivityType activityType;
    
    @NotBlank(message = "Activity description is required")
    @Size(max = 500, message = "Activity description cannot exceed 500 characters")
    @Column(name = "description", nullable = false)
    private String description;
    
    @Size(max = 500, message = "Arabic description cannot exceed 500 characters")
    @Column(name = "description_ar")
    private String descriptionAr;
    
    @Column(name = "resource_type", length = 50)
    private String resourceType; // project, task, file, etc.
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "additional_data", columnDefinition = "JSONB")
    private String additionalData; // JSON data for complex activity information
    
    // Constructors
    public UserActivity() {
        super();
    }
    
    public UserActivity(User user, UserActivityType activityType, String description, Long universityId) {
        super(universityId);
        this.user = user;
        this.activityType = activityType;
        this.description = description;
    }
    
    // Business methods
    public void addResourceReference(String resourceType, Long resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public void addRequestInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public UserActivityType getActivityType() {
        return activityType;
    }
    
    public void setActivityType(UserActivityType activityType) {
        this.activityType = activityType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescriptionAr() {
        return descriptionAr;
    }
    
    public void setDescriptionAr(String descriptionAr) {
        this.descriptionAr = descriptionAr;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}