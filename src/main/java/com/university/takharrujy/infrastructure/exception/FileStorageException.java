package com.university.takharrujy.infrastructure.exception;

import org.springframework.http.HttpStatus;

/**
 * File Storage Exception
 * Thrown when file storage operations fail
 */
public class FileStorageException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public FileStorageException(String message) {
        super(message);
        this.errorCode = "FILE_STORAGE_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FILE_STORAGE_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public FileStorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public FileStorageException(String errorCode, String message, HttpStatus httpStatus) {
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