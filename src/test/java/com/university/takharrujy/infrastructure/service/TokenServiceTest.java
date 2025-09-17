package com.university.takharrujy.infrastructure.service;

import com.university.takharrujy.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Token Service Tests")
class TokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    private static final String TEST_EMAIL = "ahmed@cu.edu.eg";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should generate email verification token successfully")
    void shouldGenerateEmailVerificationToken() {
        // When
        String token = tokenService.generateEmailVerificationToken(TEST_EMAIL, TEST_USER_ID);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(UUID.fromString(token)).isNotNull(); // Should be valid UUID

        // Verify Redis storage
        verify(valueOperations).set(
            eq("email_verification:" + token),
            eq(TEST_EMAIL + ":" + TEST_USER_ID),
            eq(Duration.ofHours(24))
        );
    }

    @Test
    @DisplayName("Should generate password reset token successfully")
    void shouldGeneratePasswordResetToken() {
        // When
        String token = tokenService.generatePasswordResetToken(TEST_EMAIL, TEST_USER_ID);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(UUID.fromString(token)).isNotNull(); // Should be valid UUID

        // Verify Redis storage
        verify(valueOperations).set(
            eq("password_reset:" + token),
            eq(TEST_EMAIL + ":" + TEST_USER_ID),
            eq(Duration.ofHours(1))
        );
    }

    @Test
    @DisplayName("Should validate email verification token successfully")
    void shouldValidateEmailVerificationTokenSuccessfully() {
        // Given
        String token = UUID.randomUUID().toString();
        String storedValue = TEST_EMAIL + ":" + TEST_USER_ID;
        
        when(valueOperations.get("email_verification:" + token)).thenReturn(storedValue);

        // When
        TokenService.TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getErrorMessage()).isNull();

        // Verify token is consumed (deleted from Redis)
        verify(redisTemplate).delete("email_verification:" + token);
    }

    @Test
    @DisplayName("Should validate password reset token successfully")
    void shouldValidatePasswordResetTokenSuccessfully() {
        // Given
        String token = UUID.randomUUID().toString();
        String storedValue = TEST_EMAIL + ":" + TEST_USER_ID;
        
        when(valueOperations.get("password_reset:" + token)).thenReturn(storedValue);

        // When
        TokenService.TokenValidationResult result = tokenService.validatePasswordResetToken(token);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getErrorMessage()).isNull();

        // Verify token is consumed (deleted from Redis)
        verify(redisTemplate).delete("password_reset:" + token);
    }

    @Test
    @DisplayName("Should reject invalid email verification token format")
    void shouldRejectInvalidEmailVerificationTokenFormat() {
        // Given
        String invalidToken = "invalid-token-format";

        // When
        TokenService.TokenValidationResult result = tokenService.validateEmailVerificationToken(invalidToken);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Invalid token format");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();

        // Verify no Redis interaction
        verifyNoInteractions(valueOperations);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("Should reject expired email verification token")
    void shouldRejectExpiredEmailVerificationToken() {
        // Given
        String token = UUID.randomUUID().toString();
        
        when(valueOperations.get("email_verification:" + token)).thenReturn(null);

        // When
        TokenService.TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Token not found or expired");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should reject invalid password reset token format")
    void shouldRejectInvalidPasswordResetTokenFormat() {
        // Given
        String invalidToken = "invalid-token-format";

        // When
        TokenService.TokenValidationResult result = tokenService.validatePasswordResetToken(invalidToken);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Invalid token format");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();

        // Verify no Redis interaction
        verifyNoInteractions(valueOperations);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("Should reject expired password reset token")
    void shouldRejectExpiredPasswordResetToken() {
        // Given
        String token = UUID.randomUUID().toString();
        
        when(valueOperations.get("password_reset:" + token)).thenReturn(null);

        // When
        TokenService.TokenValidationResult result = tokenService.validatePasswordResetToken(token);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Token not found or expired");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should handle corrupted token data gracefully")
    void shouldHandleCorruptedTokenDataGracefully() {
        // Given
        String token = UUID.randomUUID().toString();
        String corruptedValue = "corrupted-data-without-colon";
        
        when(valueOperations.get("email_verification:" + token)).thenReturn(corruptedValue);

        // When
        TokenService.TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Invalid token data format");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();

        // Verify corrupted token is cleaned up
        verify(redisTemplate).delete("email_verification:" + token);
    }

    @Test
    @DisplayName("Should handle invalid user ID in token data")
    void shouldHandleInvalidUserIdInTokenData() {
        // Given
        String token = UUID.randomUUID().toString();
        String invalidValue = TEST_EMAIL + ":invalid-user-id";
        
        when(valueOperations.get("password_reset:" + token)).thenReturn(invalidValue);

        // When
        TokenService.TokenValidationResult result = tokenService.validatePasswordResetToken(token);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Invalid token data format");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getUserId()).isNull();

        // Verify corrupted token is cleaned up
        verify(redisTemplate).delete("password_reset:" + token);
    }

    @Test
    @DisplayName("Should prevent token reuse for email verification")
    void shouldPreventTokenReuseForEmailVerification() {
        // Given
        String token = UUID.randomUUID().toString();
        String storedValue = TEST_EMAIL + ":" + TEST_USER_ID;
        
        when(valueOperations.get("email_verification:" + token))
            .thenReturn(storedValue)  // First call succeeds
            .thenReturn(null);        // Second call returns null (token consumed)

        // When - First validation
        TokenService.TokenValidationResult firstResult = tokenService.validateEmailVerificationToken(token);
        
        // When - Second validation (token reuse attempt)
        TokenService.TokenValidationResult secondResult = tokenService.validateEmailVerificationToken(token);

        // Then
        assertThat(firstResult.isValid()).isTrue();
        assertThat(secondResult.isValid()).isFalse();
        assertThat(secondResult.getErrorMessage()).isEqualTo("Token not found or expired");

        // Verify token was deleted after first use
        verify(redisTemplate, times(1)).delete("email_verification:" + token);
    }

    @Test
    @DisplayName("Should prevent token reuse for password reset")
    void shouldPreventTokenReuseForPasswordReset() {
        // Given
        String token = UUID.randomUUID().toString();
        String storedValue = TEST_EMAIL + ":" + TEST_USER_ID;
        
        when(valueOperations.get("password_reset:" + token))
            .thenReturn(storedValue)  // First call succeeds
            .thenReturn(null);        // Second call returns null (token consumed)

        // When - First validation
        TokenService.TokenValidationResult firstResult = tokenService.validatePasswordResetToken(token);
        
        // When - Second validation (token reuse attempt)  
        TokenService.TokenValidationResult secondResult = tokenService.validatePasswordResetToken(token);

        // Then
        assertThat(firstResult.isValid()).isTrue();
        assertThat(secondResult.isValid()).isFalse();
        assertThat(secondResult.getErrorMessage()).isEqualTo("Token not found or expired");

        // Verify token was deleted after first use
        verify(redisTemplate, times(1)).delete("password_reset:" + token);
    }

    @Test
    @DisplayName("Should handle Redis connection failures gracefully")
    void shouldHandleRedisConnectionFailuresGracefully() {
        // Given
        String token = UUID.randomUUID().toString();
        
        when(valueOperations.get("email_verification:" + token))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // When/Then
        assertThatThrownBy(() -> tokenService.validateEmailVerificationToken(token))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Token validation service temporarily unavailable");
    }

    @Test
    @DisplayName("Should handle Arabic email addresses correctly")
    void shouldHandleArabicEmailAddressesCorrectly() {
        // Given
        String arabicEmail = "أحمد@cu.edu.eg";
        String token = tokenService.generateEmailVerificationToken(arabicEmail, TEST_USER_ID);
        String storedValue = arabicEmail + ":" + TEST_USER_ID;
        
        when(valueOperations.get("email_verification:" + token)).thenReturn(storedValue);

        // When
        TokenService.TokenValidationResult result = tokenService.validateEmailVerificationToken(token);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getEmail()).isEqualTo(arabicEmail);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should generate unique tokens for concurrent requests")
    void shouldGenerateUniqueTokensForConcurrentRequests() {
        // When - Generate multiple tokens rapidly
        String token1 = tokenService.generateEmailVerificationToken(TEST_EMAIL, TEST_USER_ID);
        String token2 = tokenService.generateEmailVerificationToken(TEST_EMAIL, TEST_USER_ID);
        String token3 = tokenService.generatePasswordResetToken(TEST_EMAIL, TEST_USER_ID);
        String token4 = tokenService.generatePasswordResetToken(TEST_EMAIL, TEST_USER_ID);

        // Then - All tokens should be unique
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(token4);
        assertThat(token2).isNotEqualTo(token3);
        assertThat(token2).isNotEqualTo(token4);
        assertThat(token3).isNotEqualTo(token4);

        // Verify all tokens are valid UUIDs
        assertThat(UUID.fromString(token1)).isNotNull();
        assertThat(UUID.fromString(token2)).isNotNull();
        assertThat(UUID.fromString(token3)).isNotNull();
        assertThat(UUID.fromString(token4)).isNotNull();
    }
}