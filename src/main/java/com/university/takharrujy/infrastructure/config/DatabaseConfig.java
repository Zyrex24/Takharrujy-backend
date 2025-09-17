package com.university.takharrujy.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database Configuration
 * Configures JPA repositories and transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.university.takharrujy.domain.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // JPA and database configuration will be added here
    
}