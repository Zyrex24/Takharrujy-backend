package com.university.takharrujy.infrastructure.exception;

/**
 * Resource Not Found Exception
 * Thrown when a requested resource cannot be found
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        this(resourceType, String.valueOf(resourceId));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    // Common resource not found exceptions
    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("User", userId);
    }

    public static ResourceNotFoundException project(Long projectId) {
        return new ResourceNotFoundException("Project", projectId);
    }

    public static ResourceNotFoundException task(Long taskId) {
        return new ResourceNotFoundException("Task", taskId);
    }

    public static ResourceNotFoundException university(Long universityId) {
        return new ResourceNotFoundException("University", universityId);
    }

    public static ResourceNotFoundException department(Long departmentId) {
        return new ResourceNotFoundException("Department", departmentId);
    }

    public static ResourceNotFoundException file(Long fileId) {
        return new ResourceNotFoundException("File", fileId);
    }
}