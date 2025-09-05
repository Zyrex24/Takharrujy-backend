package com.university.takharrujy.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for Arabic text fields
 * Supports Arabic characters with optional English characters
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ArabicTextValidator.class)
@Documented
public @interface ValidArabicText {
    
    String message() default "Invalid Arabic text format";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Minimum length of the text
     */
    int minLength() default 1;
    
    /**
     * Maximum length of the text
     */
    int maxLength() default 255;
    
    /**
     * Allow English characters alongside Arabic
     */
    boolean allowEnglish() default true;
    
    /**
     * Allow numbers in the text
     */
    boolean allowNumbers() default true;
    
    /**
     * Allow special characters
     */
    boolean allowSpecialChars() default true;
    
    /**
     * Whether the field is required (non-null and non-empty)
     */
    boolean required() default true;
    
    /**
     * Allow whitespace characters
     */
    boolean allowWhitespace() default true;
}