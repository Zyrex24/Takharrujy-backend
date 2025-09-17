package com.university.takharrujy.infrastructure.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.service.ServiceRegistry;

/**
 * Custom PostgreSQL dialect to handle enum types properly
 */
public class PostgreSQLCustomDialect extends PostgreSQLDialect {

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        
        // Register custom enum type
        typeContributions.contributeType(new PostgreSQLEnumType());
    }
}