package com.university.takharrujy.infrastructure.exception;

import org.springframework.http.HttpStatus;

/**
 * Security Exception
 * Thrown when security violations occur
 */
public class SecurityException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public SecurityException(String message) {
        super(message);
        this.errorCode = "SECURITY_ERROR";
        this.httpStatus = HttpStatus.FORBIDDEN;
    }
    
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SECURITY_ERROR";
        this.httpStatus = HttpStatus.FORBIDDEN;
    }
    
    public SecurityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.FORBIDDEN;
    }
    
    public SecurityException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}