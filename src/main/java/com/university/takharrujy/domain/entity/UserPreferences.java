package com.university.takharrujy.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * UserPreferences Entity
 * Represents user notification and application preferences
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferences extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // Notification preferences
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;
    
    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications = true;
    
    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications = false;
    
    // Project notifications
    @Column(name = "project_updates", nullable = false)
    private Boolean projectUpdates = true;
    
    @Column(name = "task_assignments", nullable = false)
    private Boolean taskAssignments = true;
    
    @Column(name = "task_due_reminders", nullable = false)
    private Boolean taskDueReminders = true;
    
    @Column(name = "project_invitations", nullable = false)
    private Boolean projectInvitations = true;
    
    // Communication notifications
    @Column(name = "new_messages", nullable = false)
    private Boolean newMessages = true;
    
    @Column(name = "message_mentions", nullable = false)
    private Boolean messageMentions = true;
    
    // Supervisor-specific notifications
    @Column(name = "submission_notifications", nullable = false)
    private Boolean submissionNotifications = true;
    
    @Column(name = "progress_reports", nullable = false)
    private Boolean progressReports = true;
    
    // UI preferences
    @Column(name = "theme", length = 20)
    private String theme = "auto"; // auto, light, dark
    
    @Column(name = "language", length = 10)
    private String language = "ar"; // ar, en
    
    @Column(name = "timezone", length = 50)
    private String timezone = "Africa/Cairo";
    
    // Privacy preferences
    @Column(name = "profile_visibility", length = 20)
    private String profileVisibility = "university"; // public, university, private
    
    @Column(name = "show_email", nullable = false)
    private Boolean showEmail = false;
    
    @Column(name = "show_phone", nullable = false)
    private Boolean showPhone = false;
    
    // Constructors
    public UserPreferences() {
        super();
    }
    
    public UserPreferences(User user, Long universityId) {
        super(universityId);
        this.user = user;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Boolean getEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public Boolean getPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    public Boolean getSmsNotifications() {
        return smsNotifications;
    }
    
    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }
    
    public Boolean getProjectUpdates() {
        return projectUpdates;
    }
    
    public void setProjectUpdates(Boolean projectUpdates) {
        this.projectUpdates = projectUpdates;
    }
    
    public Boolean getTaskAssignments() {
        return taskAssignments;
    }
    
    public void setTaskAssignments(Boolean taskAssignments) {
        this.taskAssignments = taskAssignments;
    }
    
    public Boolean getTaskDueReminders() {
        return taskDueReminders;
    }
    
    public void setTaskDueReminders(Boolean taskDueReminders) {
        this.taskDueReminders = taskDueReminders;
    }
    
    public Boolean getProjectInvitations() {
        return projectInvitations;
    }
    
    public void setProjectInvitations(Boolean projectInvitations) {
        this.projectInvitations = projectInvitations;
    }
    
    public Boolean getNewMessages() {
        return newMessages;
    }
    
    public void setNewMessages(Boolean newMessages) {
        this.newMessages = newMessages;
    }
    
    public Boolean getMessageMentions() {
        return messageMentions;
    }
    
    public void setMessageMentions(Boolean messageMentions) {
        this.messageMentions = messageMentions;
    }
    
    public Boolean getSubmissionNotifications() {
        return submissionNotifications;
    }
    
    public void setSubmissionNotifications(Boolean submissionNotifications) {
        this.submissionNotifications = submissionNotifications;
    }
    
    public Boolean getProgressReports() {
        return progressReports;
    }
    
    public void setProgressReports(Boolean progressReports) {
        this.progressReports = progressReports;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public String getProfileVisibility() {
        return profileVisibility;
    }
    
    public void setProfileVisibility(String profileVisibility) {
        this.profileVisibility = profileVisibility;
    }
    
    public Boolean getShowEmail() {
        return showEmail;
    }
    
    public void setShowEmail(Boolean showEmail) {
        this.showEmail = showEmail;
    }
    
    public Boolean getShowPhone() {
        return showPhone;
    }
    
    public void setShowPhone(Boolean showPhone) {
        this.showPhone = showPhone;
    }
}