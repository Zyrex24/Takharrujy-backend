package com.university.takharrujy.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator for Arabic text with customizable options
 * Supports proper Arabic Unicode ranges and text normalization
 */
public class ArabicTextValidator implements ConstraintValidator<ValidArabicText, String> {

    // Arabic Unicode ranges
    private static final String ARABIC_MAIN = "\\u0600-\\u06FF";           // Arabic block
    private static final String ARABIC_SUPPLEMENT = "\\u0750-\\u077F";      // Arabic Supplement
    private static final String ARABIC_EXTENDED_A = "\\u08A0-\\u08FF";      // Arabic Extended-A
    private static final String ARABIC_PRESENTATION_A = "\\uFB50-\\uFDFF";  // Arabic Presentation Forms-A
    private static final String ARABIC_PRESENTATION_B = "\\uFE70-\\uFEFF";  // Arabic Presentation Forms-B
    
    private static final String ENGLISH_CHARS = "a-zA-Z";
    private static final String NUMBERS = "0-9\\u0660-\\u0669";             // Latin and Arabic-Indic digits
    private static final String WHITESPACE = "\\s";
    private static final String SPECIAL_CHARS = "\\p{Punct}";
    
    private ValidArabicText annotation;

    @Override
    public void initialize(ValidArabicText constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null and empty values
        if (value == null || value.trim().isEmpty()) {
            return !annotation.required();
        }

        // Normalize the text
        String normalizedText = normalizeArabicText(value);
        
        // Check length constraints
        if (normalizedText.length() < annotation.minLength() || 
            normalizedText.length() > annotation.maxLength()) {
            addConstraintViolation(context, 
                String.format("Text length must be between %d and %d characters", 
                    annotation.minLength(), annotation.maxLength()));
            return false;
        }

        // Build regex pattern based on configuration
        String pattern = buildValidationPattern();
        
        // Validate against pattern
        boolean isValid = normalizedText.matches(pattern);
        
        if (!isValid) {
            addConstraintViolation(context, "Text contains invalid characters for Arabic text field");
        }
        
        return isValid;
    }

    /**
     * Normalize Arabic text by removing diacritics and extra whitespace
     */
    private String normalizeArabicText(String text) {
        if (text == null) return null;
        
        return text
            // Remove Arabic diacritics (harakat)
            .replaceAll("[\\u064B-\\u0652\\u0670\\u0640]", "")
            // Normalize whitespace
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * Build validation regex pattern based on annotation configuration
     */
    private String buildValidationPattern() {
        StringBuilder pattern = new StringBuilder("^[");
        
        // Always include Arabic characters
        pattern.append(ARABIC_MAIN)
               .append(ARABIC_SUPPLEMENT)
               .append(ARABIC_EXTENDED_A)
               .append(ARABIC_PRESENTATION_A)
               .append(ARABIC_PRESENTATION_B);
        
        // Add English characters if allowed
        if (annotation.allowEnglish()) {
            pattern.append(ENGLISH_CHARS);
        }
        
        // Add numbers if allowed
        if (annotation.allowNumbers()) {
            pattern.append(NUMBERS);
        }
        
        // Add whitespace if allowed
        if (annotation.allowWhitespace()) {
            pattern.append(WHITESPACE);
        }
        
        // Add special characters if allowed
        if (annotation.allowSpecialChars()) {
            pattern.append(SPECIAL_CHARS);
        }
        
        pattern.append("]+$");
        
        return pattern.toString();
    }

    /**
     * Add custom constraint violation message
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}