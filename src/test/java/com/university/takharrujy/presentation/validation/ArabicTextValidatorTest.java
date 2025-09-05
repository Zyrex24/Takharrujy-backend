package com.university.takharrujy.presentation.validation;

import com.university.takharrujy.infrastructure.validation.ArabicTextValidator;
import com.university.takharrujy.infrastructure.validation.ValidArabicText;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Arabic Text Validator Tests")
class ArabicTextValidatorTest {

    @Mock
    private ValidArabicText validArabicText;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    private ArabicTextValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArabicTextValidator();
    }

    @Test
    @DisplayName("Should validate pure Arabic text successfully")
    void shouldValidatePureArabicTextSuccessfully() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد محمد", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("جامعة القاهرة", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("نظام إدارة المشاريع", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should validate mixed Arabic and English text when allowed")
    void shouldValidateMixedArabicAndEnglishTextWhenAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد Ahmed", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("Computer Science علوم الحاسوب", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("مشروع Spring Boot", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should validate Arabic text with numbers when allowed")
    void shouldValidateArabicTextWithNumbersWhenAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("الطالب رقم 12345", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("مشروع 2024", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("قسم 101", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should validate Arabic text with special characters when allowed")
    void shouldValidateArabicTextWithSpecialCharsWhenAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد (طالب)", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("مشروع - نظام إدارة", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("البريد: ahmed@example.com", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should reject English text when not allowed")
    void shouldRejectEnglishTextWhenNotAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("Ahmed Mohamed", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("Computer Science", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("أحمد Ahmed", constraintValidatorContext)).isFalse();
    }

    @Test
    @DisplayName("Should reject numbers when not allowed")
    void shouldRejectNumbersWhenNotAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد 123", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("الطالب رقم 12345", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("2024", constraintValidatorContext)).isFalse();
    }

    @Test
    @DisplayName("Should reject special characters when not allowed")
    void shouldRejectSpecialCharsWhenNotAllowed() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد (طالب)", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("مشروع @ الجامعة", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("نظام - إدارة", constraintValidatorContext)).isFalse();
    }

    @Test
    @DisplayName("Should handle null values correctly based on required flag")
    void shouldHandleNullValuesCorrectlyBasedOnRequiredFlag() {
        // Given - Required field
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Required field should reject null
        assertThat(validator.isValid(null, constraintValidatorContext)).isFalse();

        // Given - Optional field
        when(validArabicText.required()).thenReturn(false);
        validator.initialize(validArabicText);

        // When/Then - Optional field should accept null
        assertThat(validator.isValid(null, constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should handle empty strings correctly based on required flag")
    void shouldHandleEmptyStringsCorrectlyBasedOnRequiredFlag() {
        // Given - Required field
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Required field should reject empty string
        assertThat(validator.isValid("", constraintValidatorContext)).isFalse();
        assertThat(validator.isValid("   ", constraintValidatorContext)).isFalse();

        // Given - Optional field
        when(validArabicText.required()).thenReturn(false);
        validator.initialize(validArabicText);

        // When/Then - Optional field should accept empty string
        assertThat(validator.isValid("", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("   ", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should enforce minimum length constraint")
    void shouldEnforceMinimumLengthConstraint() {
        // Given
        when(validArabicText.minLength()).thenReturn(5);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد", constraintValidatorContext)).isFalse(); // 4 characters
        assertThat(validator.isValid("أحمد محمد", constraintValidatorContext)).isTrue(); // 9 characters
        assertThat(validator.isValid("طالب", constraintValidatorContext)).isFalse(); // 4 characters
    }

    @Test
    @DisplayName("Should enforce maximum length constraint")
    void shouldEnforceMaximumLengthConstraint() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(10);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then
        assertThat(validator.isValid("أحمد محمد", constraintValidatorContext)).isTrue(); // 9 characters
        assertThat(validator.isValid("أحمد محمد علي", constraintValidatorContext)).isFalse(); // 13 characters
        assertThat(validator.isValid("طالب", constraintValidatorContext)).isTrue(); // 4 characters
    }

    @Test
    @DisplayName("Should normalize Arabic text by removing diacritics")
    void shouldNormalizeArabicTextByRemovingDiacritics() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Text with diacritics should be normalized and validated
        assertThat(validator.isValid("أَحْمَد مُحَمَّد", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("جَامِعَة القَاهِرَة", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("مَشْرُوع التَّخَرُّج", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should handle mixed RTL and LTR text correctly")
    void shouldHandleMixedRTLAndLTRTextCorrectly() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Mixed text should be handled correctly
        assertThat(validator.isValid("أحمد Ahmed محمد Mohamed", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("مشروع Spring Boot للتخرج", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("نظام إدارة Projects Management", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should normalize whitespace correctly")
    void shouldNormalizeWhitespaceCorrectly() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(true);
        when(validArabicText.allowNumbers()).thenReturn(true);
        when(validArabicText.allowSpecialChars()).thenReturn(true);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Multiple spaces should be normalized
        assertThat(validator.isValid("أحمد    محمد", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("  أحمد محمد  ", constraintValidatorContext)).isTrue();
        assertThat(validator.isValid("\t\nأحمد محمد\t\n", constraintValidatorContext)).isTrue();
    }

    @Test
    @DisplayName("Should handle Unicode Arabic ranges correctly")
    void shouldHandleUnicodeArabicRangesCorrectly() {
        // Given
        when(validArabicText.minLength()).thenReturn(1);
        when(validArabicText.maxLength()).thenReturn(255);
        when(validArabicText.allowEnglish()).thenReturn(false);
        when(validArabicText.allowNumbers()).thenReturn(false);
        when(validArabicText.allowSpecialChars()).thenReturn(false);
        when(validArabicText.required()).thenReturn(true);
        
        validator.initialize(validArabicText);

        // When/Then - Different Arabic Unicode ranges should be accepted
        assertThat(validator.isValid("أبت", constraintValidatorContext)).isTrue(); // Basic Arabic
        assertThat(validator.isValid("ڤگچ", constraintValidatorContext)).isTrue(); // Arabic Extended-A
        assertThat(validator.isValid("ﺍﺒﺗ", constraintValidatorContext)).isTrue(); // Arabic Presentation Forms
    }
}