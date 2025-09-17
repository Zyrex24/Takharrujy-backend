package com.university.takharrujy.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Token Service for managing verification and reset tokens
 * Handles email verification tokens, password reset tokens with Redis storage
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private static final String EMAIL_VERIFICATION_PREFIX = "email_verify:";
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final String TOKEN_USAGE_PREFIX = "token_used:";
    
    private static final Duration EMAIL_VERIFICATION_EXPIRY = Duration.ofHours(48); // 48 hours
    private static final Duration PASSWORD_RESET_EXPIRY = Duration.ofHours(24); // 24 hours
    private static final Duration TOKEN_USAGE_TRACKING = Duration.ofDays(7); // Track used tokens for 7 days

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom;

    public TokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate and store email verification token
     */
    public String generateEmailVerificationToken(String email, Long userId) {
        try {
            String token = generateSecureToken();
            String key = EMAIL_VERIFICATION_PREFIX + token;

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("email", email);
            tokenData.put("userId", userId);
            tokenData.put("type", "EMAIL_VERIFICATION");
            tokenData.put("createdAt", Instant.now().toString());
            tokenData.put("expiresAt", Instant.now().plus(EMAIL_VERIFICATION_EXPIRY).toString());

            redisTemplate.opsForHash().putAll(key, tokenData);
            redisTemplate.expire(key, EMAIL_VERIFICATION_EXPIRY);

            logger.debug("Generated email verification token for user: {}", userId);
            return token;

        } catch (Exception e) {
            logger.error("Failed to generate email verification token for email: {}", email, e);
            throw new RuntimeException("Failed to generate verification token", e);
        }
    }

    /**
     * Generate and store password reset token
     */
    public String generatePasswordResetToken(String email, Long userId) {
        try {
            // Invalidate any existing password reset tokens for this user
            invalidateExistingPasswordResetTokens(userId);

            String token = generateSecureToken();
            String key = PASSWORD_RESET_PREFIX + token;

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("email", email);
            tokenData.put("userId", userId);
            tokenData.put("type", "PASSWORD_RESET");
            tokenData.put("createdAt", Instant.now().toString());
            tokenData.put("expiresAt", Instant.now().plus(PASSWORD_RESET_EXPIRY).toString());

            redisTemplate.opsForHash().putAll(key, tokenData);
            redisTemplate.expire(key, PASSWORD_RESET_EXPIRY);

            // Store reverse mapping for token invalidation
            String userTokenKey = "user_reset_token:" + userId;
            redisTemplate.opsForValue().set(userTokenKey, token, PASSWORD_RESET_EXPIRY);

            logger.debug("Generated password reset token for user: {}", userId);
            return token;

        } catch (Exception e) {
            logger.error("Failed to generate password reset token for email: {}", email, e);
            throw new RuntimeException("Failed to generate reset token", e);
        }
    }

    /**
     * Validate and consume email verification token
     */
    public TokenValidationResult validateEmailVerificationToken(String token) {
        try {
            String key = EMAIL_VERIFICATION_PREFIX + token;

            if (!redisTemplate.hasKey(key)) {
                logger.warn("Email verification token not found or expired: {}", token);
                return TokenValidationResult.invalid("Token not found or expired");
            }

            if (isTokenUsed(token)) {
                logger.warn("Email verification token already used: {}", token);
                return TokenValidationResult.invalid("Token has already been used");
            }

            Map<Object, Object> tokenData = redisTemplate.opsForHash().entries(key);
            String email = (String) tokenData.get("email");
            Long userId = Long.parseLong(tokenData.get("userId").toString());

            // Mark token as used
            markTokenAsUsed(token);
            
            // Delete the token
            redisTemplate.delete(key);

            logger.info("Successfully validated email verification token for user: {}", userId);
            return TokenValidationResult.valid(email, userId, "EMAIL_VERIFICATION");

        } catch (Exception e) {
            logger.error("Failed to validate email verification token: {}", token, e);
            return TokenValidationResult.invalid("Token validation failed");
        }
    }

    /**
     * Validate and consume password reset token
     */
    public TokenValidationResult validatePasswordResetToken(String token) {
        try {
            String key = PASSWORD_RESET_PREFIX + token;

            if (!redisTemplate.hasKey(key)) {
                logger.warn("Password reset token not found or expired: {}", token);
                return TokenValidationResult.invalid("Token not found or expired");
            }

            if (isTokenUsed(token)) {
                logger.warn("Password reset token already used: {}", token);
                return TokenValidationResult.invalid("Token has already been used");
            }

            Map<Object, Object> tokenData = redisTemplate.opsForHash().entries(key);
            String email = (String) tokenData.get("email");
            Long userId = Long.parseLong(tokenData.get("userId").toString());

            // Mark token as used
            markTokenAsUsed(token);
            
            // Delete the token and user token mapping
            redisTemplate.delete(key);
            redisTemplate.delete("user_reset_token:" + userId);

            logger.info("Successfully validated password reset token for user: {}", userId);
            return TokenValidationResult.valid(email, userId, "PASSWORD_RESET");

        } catch (Exception e) {
            logger.error("Failed to validate password reset token: {}", token, e);
            return TokenValidationResult.invalid("Token validation failed");
        }
    }

    /**
     * Check if email verification token exists for user
     */
    public boolean hasActiveEmailVerificationToken(Long userId) {
        try {
            // This is a simple check - in production, you might want to maintain a reverse index
            // For now, we'll assume tokens are managed properly
            return false; // Simplified for this implementation
        } catch (Exception e) {
            logger.error("Failed to check active email verification token for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Check if password reset token exists for user
     */
    public boolean hasActivePasswordResetToken(Long userId) {
        try {
            String userTokenKey = "user_reset_token:" + userId;
            return redisTemplate.hasKey(userTokenKey);
        } catch (Exception e) {
            logger.error("Failed to check active password reset token for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Invalidate all existing password reset tokens for a user
     */
    public void invalidateExistingPasswordResetTokens(Long userId) {
        try {
            String userTokenKey = "user_reset_token:" + userId;
            String existingToken = (String) redisTemplate.opsForValue().get(userTokenKey);
            
            if (existingToken != null) {
                String tokenKey = PASSWORD_RESET_PREFIX + existingToken;
                redisTemplate.delete(tokenKey);
                redisTemplate.delete(userTokenKey);
                
                logger.debug("Invalidated existing password reset token for user: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to invalidate existing password reset tokens for user: {}", userId, e);
        }
    }

    /**
     * Generate a cryptographically secure token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[48]; // 48 bytes = 384 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Mark token as used to prevent replay attacks
     */
    private void markTokenAsUsed(String token) {
        try {
            String usageKey = TOKEN_USAGE_PREFIX + token;
            redisTemplate.opsForValue().set(usageKey, "used", TOKEN_USAGE_TRACKING);
        } catch (Exception e) {
            logger.error("Failed to mark token as used: {}", token, e);
        }
    }

    /**
     * Check if token has been used
     */
    private boolean isTokenUsed(String token) {
        try {
            String usageKey = TOKEN_USAGE_PREFIX + token;
            return redisTemplate.hasKey(usageKey);
        } catch (Exception e) {
            logger.error("Failed to check token usage: {}", token, e);
            return false;
        }
    }

    /**
     * Token validation result
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String email;
        private final Long userId;
        private final String tokenType;
        private final String errorMessage;

        private TokenValidationResult(boolean valid, String email, Long userId, 
                                    String tokenType, String errorMessage) {
            this.valid = valid;
            this.email = email;
            this.userId = userId;
            this.tokenType = tokenType;
            this.errorMessage = errorMessage;
        }

        public static TokenValidationResult valid(String email, Long userId, String tokenType) {
            return new TokenValidationResult(true, email, userId, tokenType, null);
        }

        public static TokenValidationResult invalid(String errorMessage) {
            return new TokenValidationResult(false, null, null, null, errorMessage);
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getEmail() { return email; }
        public Long getUserId() { return userId; }
        public String getTokenType() { return tokenType; }
        public String getErrorMessage() { return errorMessage; }
    }
}