package com.university.takharrujy.infrastructure.exception;

import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.common.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Global Exception Handler
 * Handles all exceptions across the application and returns consistent error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handle validation errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = getLocalizedMessage("validation.failed", "Validation failed");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .fieldErrors(errors)
                .build();

        logger.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = getLocalizedMessage("validation.constraint.failed", "Constraint validation failed");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("CONSTRAINT_VIOLATION")
                .message(message)
                .fieldErrors(errors)
                .build();

        logger.warn("Constraint violation: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex) {
        
        String message = getLocalizedMessage("authentication.failed", "Authentication failed");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("AUTHENTICATION_FAILED")
                .message(message)
                .build();

        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex) {
        
        String message = getLocalizedMessage("authentication.failed", "Authentication failed");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("AUTHENTICATION_FAILED")
                .message(message)
                .build();

        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        String message = getLocalizedMessage("access.denied", "Access denied");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .message(message)
                .build();

        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle data integrity violation exceptions
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        
        String message = getLocalizedMessage("data.integrity.violation", "Data integrity violation");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("DATA_INTEGRITY_VIOLATION")
                .message(message)
                .build();

        logger.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex) {
        
        String message = getLocalizedMessage(ex.getMessageKey(), ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(message)
                .build();

        logger.warn("Business exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        
        String message = getLocalizedMessage("resource.not.found", "Resource not found");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(message)
                .build();

        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        String message = getLocalizedMessage("invalid.argument", "Invalid argument");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("INVALID_ARGUMENT")
                .message(message)
                .build();

        logger.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        
        String message = getLocalizedMessage("internal.server.error", "Internal server error");
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message(message)
                .build();

        logger.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message, errorDetails));
    }

    /**
     * Get localized message based on current request locale
     */
    private String getLocalizedMessage(String key, String defaultMessage) {
        try {
            Locale locale = getCurrentLocale();
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            logger.debug("No message found for key: {}, using default: {}", key, defaultMessage);
            return defaultMessage != null ? defaultMessage : key;
        }
    }

    /**
     * Get current request locale
     */
    private Locale getCurrentLocale() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return RequestContextUtils.getLocale(request);
        } catch (Exception e) {
            // Fallback to Arabic if request context is not available
            return Locale.forLanguageTag("ar");
        }
    }
}