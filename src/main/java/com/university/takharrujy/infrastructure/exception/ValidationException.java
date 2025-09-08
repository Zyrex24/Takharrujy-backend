package com.university.takharrujy.infrastructure.exception;

import org.springframework.http.HttpStatus;

/**
 * Validation Exception
 * Thrown when input validation fails
 */
public class ValidationException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public ValidationException(String message) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VALIDATION_ERROR";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
    
    public ValidationException(String errorCode, String message, HttpStatus httpStatus) {
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