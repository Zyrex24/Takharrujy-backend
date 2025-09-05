package com.university.takharrujy.domain.enums;

/**
 * Project status enumeration
 * Defines the lifecycle states of a graduation project
 */
public enum ProjectStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ARCHIVED("Archived"),
    REJECTED("Rejected");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this == IN_PROGRESS || this == APPROVED || this == UNDER_REVIEW;
    }
    
    public boolean isEditable() {
        return this == DRAFT || this == IN_PROGRESS;
    }
}