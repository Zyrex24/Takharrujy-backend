package com.university.takharrujy.domain.enums;

/**
 * User roles in the system
 * Defines the role hierarchy: ADMIN > SUPERVISOR > STUDENT
 */
public enum UserRole {
    STUDENT("Student"),
    SUPERVISOR("Supervisor"), 
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}