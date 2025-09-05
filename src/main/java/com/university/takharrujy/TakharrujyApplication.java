package com.university.takharrujy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Takharrujy (تخرجي) - University Graduation Project Management Platform
 * 
 * Main application class for the Spring Boot application.
 * Enables caching, JPA auditing, async processing, and transaction management.
 * 
 * Features:
 * - Multi-tenant university system with row-level security
 * - Arabic language support with RTL layout
 * - JWT authentication with role-based access control
 * - File management with virus scanning
 * - Real-time notifications and messaging
 * 
 * @author Takharrujy Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class TakharrujyApplication {

    public static void main(String[] args) {
        // Enable virtual threads for better concurrency handling
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        SpringApplication.run(TakharrujyApplication.class, args);
    }
}