package com.university.takharrujy.infrastructure.exception;

import org.springframework.http.HttpStatus;

/**
 * Business Exception
 * Custom exception for business logic violations
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String messageKey;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message, String messageKey) {
        super(message);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String errorCode, String message, String messageKey, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorCode, String message, String messageKey, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    // Common business exceptions
    public static BusinessException invalidInput(String message) {
        return new BusinessException("INVALID_INPUT", message, "business.invalid.input");
    }
    
    public static BusinessException passwordMismatch(String message) {
        return new BusinessException("INVALID_INPUT", message, "business.password.mismatch");
    }

    public static BusinessException tokenInvalid(String message) {
        return new BusinessException("INVALID_INPUT", message, "business.token.invalid");
    }

    public static BusinessException duplicateResource(String message) {
        return new BusinessException("DUPLICATE_RESOURCE", message, "business.duplicate.resource");
    }

    public static BusinessException operationNotAllowed(String message) {
        return new BusinessException("OPERATION_NOT_ALLOWED", message, "business.operation.not.allowed");
    }

    public static BusinessException resourceInUse(String message) {
        return new BusinessException("RESOURCE_IN_USE", message, "business.resource.in.use");
    }

    public static BusinessException quotaExceeded(String message) {
        return new BusinessException("QUOTA_EXCEEDED", message, "business.quota.exceeded");
    }
}