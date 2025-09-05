package com.university.takharrujy.domain.enums;

/**
 * Member status in a project
 */
public enum MemberStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    REJECTED("Rejected"),
    REMOVED("Removed");

    private final String displayName;

    MemberStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}