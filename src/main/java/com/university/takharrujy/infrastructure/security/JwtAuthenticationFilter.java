package com.university.takharrujy.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Processes JWT tokens and sets authentication context for requests
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, 
                                 UserDetailsService userDetailsService,
                                 SessionService sessionService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Get username from token
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // Check if session is valid (not blacklisted)
                if (sessionService.isSessionValid(username, jwt)) {
                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails != null && userDetails.isEnabled()) {
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // Set additional details
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication context
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Set university context for RLS (Row Level Security)
                        setUniversityContext(userDetails);

                        logger.debug("Successfully authenticated user: {} with authorities: {}", 
                                   username, userDetails.getAuthorities());
                    }
                } else {
                    logger.warn("Invalid or blacklisted session for user: {}", username);
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot set user authentication: {}", ex.getMessage(), ex);
            // Clear any existing authentication
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Set university context for database Row-Level Security
     */
    private void setUniversityContext(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal customPrincipal) {
            Long universityId = customPrincipal.getUniversityId();
            if (universityId != null) {
                // This would be used by a database session manager to set PostgreSQL session variable
                // For now, we'll store it in thread-local storage
                TenantContext.setCurrentUniversityId(universityId);
                logger.debug("Set university context: {}", universityId);
            }
        }
    }

    /**
     * Thread-local storage for tenant context
     */
    public static class TenantContext {
        private static final ThreadLocal<Long> currentUniversityId = new ThreadLocal<>();

        public static void setCurrentUniversityId(Long universityId) {
            currentUniversityId.set(universityId);
        }

        public static Long getCurrentUniversityId() {
            return currentUniversityId.get();
        }

        public static void clear() {
            currentUniversityId.remove();
        }
    }
}