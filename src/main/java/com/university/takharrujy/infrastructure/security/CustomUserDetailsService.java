package com.university.takharrujy.infrastructure.security;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user details from database for authentication
 */
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> {
                    logger.warn("User not found or inactive: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        logger.debug("User found: {} with role: {}", user.getEmail(), user.getRole());

        return new CustomUserPrincipal(user);
    }

    /**
     * Custom UserDetails implementation
     */
    public static class CustomUserPrincipal implements UserDetails {

        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Convert user role to Spring Security authority
            String authority = user.getRole().getRoleName(); // Returns "ROLE_STUDENT", "ROLE_SUPERVISOR", etc.
            return Collections.singletonList(new SimpleGrantedAuthority(authority));
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // We don't implement account expiration
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getIsActive(); // Account is locked if inactive
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // We don't implement credential expiration
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive(); // Only check if user is active, email verification is handled in AuthenticationService
        }

        // Additional methods to access user data
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public Long getUniversityId() {
            return user.getUniversityId();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getPreferredLanguage() {
            return user.getPreferredLanguage();
        }

        public boolean isEmailVerified() {
            return user.getIsEmailVerified();
        }
    }
}