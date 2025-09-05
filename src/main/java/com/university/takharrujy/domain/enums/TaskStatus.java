package com.university.takharrujy.domain.enums;

/**
 * Task status enumeration
 */
public enum TaskStatus {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    REVIEW("In Review"),
    COMPLETED("Completed"),
    BLOCKED("Blocked"),
    CANCELLED("Cancelled");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    public boolean isActive() {
        return this == TODO || this == IN_PROGRESS || this == REVIEW;
    }
}