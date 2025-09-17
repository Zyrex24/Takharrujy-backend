package com.university.takharrujy.domain.enums;

/**
 * Member role in a project
 */
public enum MemberRole {
    LEADER("Team Leader"),
    MEMBER("Member"),
    COLLABORATOR("Collaborator");

    private final String displayName;

    MemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}