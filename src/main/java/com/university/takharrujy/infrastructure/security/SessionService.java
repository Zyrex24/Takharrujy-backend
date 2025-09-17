package com.university.takharrujy.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Session Service for managing user sessions with Redis
 * Handles session creation, validation, and invalidation for JWT tokens
 */
@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private static final String SESSION_PREFIX = "session:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(24);
    private static final Duration BLACKLIST_TIMEOUT = Duration.ofHours(48);

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Create a new user session
     */
    public void createSession(String username, String accessToken, String refreshToken, 
                            UserSessionData sessionData) {
        try {
            String sessionKey = SESSION_PREFIX + username;

            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("accessToken", accessToken);
            sessionMap.put("refreshToken", refreshToken);
            sessionMap.put("data", sessionData);
            sessionMap.put("lastAccess", Instant.now().toString());
            sessionMap.put("createdAt", Instant.now().toString());

            redisTemplate.opsForHash().putAll(sessionKey, sessionMap);
            redisTemplate.expire(sessionKey, SESSION_TIMEOUT);

            logger.debug("Created session for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to create session for user: {}", username, e);
            throw new RuntimeException("Failed to create session", e);
        }
    }

    /**
     * Validate if session exists and token matches
     */
    public boolean isSessionValid(String username, String accessToken) {
        try {
            String sessionKey = SESSION_PREFIX + username;

            // Check if token is blacklisted
            if (isTokenBlacklisted(accessToken)) {
                logger.warn("Token is blacklisted for user: {}", username);
                return false;
            }

            // Check if session exists
            if (!redisTemplate.hasKey(sessionKey)) {
                logger.debug("No session found for user: {}", username);
                return false;
            }

            // Get stored access token
            String storedToken = (String) redisTemplate.opsForHash().get(sessionKey, "accessToken");

            if (storedToken != null && storedToken.equals(accessToken)) {
                // Update last access time
                redisTemplate.opsForHash().put(sessionKey, "lastAccess", Instant.now().toString());
                redisTemplate.expire(sessionKey, SESSION_TIMEOUT);
                
                logger.debug("Valid session found for user: {}", username);
                return true;
            }

            logger.warn("Token mismatch for user: {}", username);
            return false;
        } catch (Exception e) {
            logger.error("Failed to validate session for user: {}", username, e);
            return false;
        }
    }

    /**
     * Update session with new tokens (during refresh)
     */
    public void updateSession(String username, String newAccessToken, String newRefreshToken) {
        try {
            String sessionKey = SESSION_PREFIX + username;

            if (redisTemplate.hasKey(sessionKey)) {
                redisTemplate.opsForHash().put(sessionKey, "accessToken", newAccessToken);
                redisTemplate.opsForHash().put(sessionKey, "refreshToken", newRefreshToken);
                redisTemplate.opsForHash().put(sessionKey, "lastAccess", Instant.now().toString());
                redisTemplate.expire(sessionKey, SESSION_TIMEOUT);

                logger.debug("Updated session tokens for user: {}", username);
            } else {
                logger.warn("Attempted to update non-existent session for user: {}", username);
            }
        } catch (Exception e) {
            logger.error("Failed to update session for user: {}", username, e);
            throw new RuntimeException("Failed to update session", e);
        }
    }

    /**
     * Get session data for a user
     */
    public UserSessionData getSessionData(String username) {
        try {
            String sessionKey = SESSION_PREFIX + username;
            return (UserSessionData) redisTemplate.opsForHash().get(sessionKey, "data");
        } catch (Exception e) {
            logger.error("Failed to get session data for user: {}", username, e);
            return null;
        }
    }

    /**
     * Invalidate user session (logout)
     */
    public void invalidateSession(String username, String accessToken) {
        try {
            String sessionKey = SESSION_PREFIX + username;
            
            // Add token to blacklist
            blacklistToken(accessToken);
            
            // Remove session
            redisTemplate.delete(sessionKey);

            logger.info("Invalidated session for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to invalidate session for user: {}", username, e);
            throw new RuntimeException("Failed to invalidate session", e);
        }
    }

    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllSessions(String username) {
        try {
            String sessionKey = SESSION_PREFIX + username;
            
            // Get access token before deleting session
            String accessToken = (String) redisTemplate.opsForHash().get(sessionKey, "accessToken");
            if (accessToken != null) {
                blacklistToken(accessToken);
            }
            
            redisTemplate.delete(sessionKey);

            logger.info("Invalidated all sessions for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to invalidate all sessions for user: {}", username, e);
            throw new RuntimeException("Failed to invalidate all sessions", e);
        }
    }

    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", BLACKLIST_TIMEOUT);
            
            logger.debug("Blacklisted token");
        } catch (Exception e) {
            logger.error("Failed to blacklist token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            logger.error("Failed to check token blacklist status", e);
            return false;
        }
    }

    /**
     * Get refresh token for user
     */
    public String getRefreshToken(String username) {
        try {
            String sessionKey = SESSION_PREFIX + username;
            return (String) redisTemplate.opsForHash().get(sessionKey, "refreshToken");
        } catch (Exception e) {
            logger.error("Failed to get refresh token for user: {}", username, e);
            return null;
        }
    }

    /**
     * Check if user has active session
     */
    public boolean hasActiveSession(String username) {
        try {
            String sessionKey = SESSION_PREFIX + username;
            return redisTemplate.hasKey(sessionKey);
        } catch (Exception e) {
            logger.error("Failed to check active session for user: {}", username, e);
            return false;
        }
    }

    /**
     * Session data model
     */
    public record UserSessionData(
        String userId,
        String universityId,
        String userAgent,
        String ipAddress,
        String preferredLanguage,
        Instant loginTime
    ) {}
}