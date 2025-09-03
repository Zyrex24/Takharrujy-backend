# Takharrujy Platform - Claude Commands Directory

## Overview
This `.claude/` directory contains specialized commands, agents, and workflows for the **Takharrujy (تخرجي)** platform - a university graduation project management system built with Spring Boot 3.4.x, Java 24, PostgreSQL 16.x, and Redis 7.x.

## Directory Structure

```
.claude/
├── config.json                 # Main configuration file
├── README.md                   # This file
├── agents/                     # Specialized AI agents
│   └── backend-developer.md    # Backend development agent
├── commands/                   # Command categories
│   ├── development/           # Development commands
│   │   └── spring-boot-dev.md # Spring Boot specific commands
│   ├── testing/              # Testing automation
│   │   ├── test-automation.md # Testing commands and strategies
│   │   └── postman-automation.md # Postman API testing requirements
│   ├── database/             # Database management
│   │   └── database-management.md # DB operations and migrations
│   └── deployment/           # Deployment and DevOps
│       └── docker-deployment.md # Docker and containerization
└── workflows/                # Development workflows
    └── development-workflow.md # Complete development process
```

## Project Context

### Technology Stack
- **Backend:** Spring Boot 3.4.x with Spring 6.2.x
- **Language:** Java 24 (Project Loom - Virtual Threads)
- **Database:** PostgreSQL 16.x with Row-Level Security
- **Cache:** Redis 7.x for session and data caching
- **Build:** Maven with modern Java features
- **Architecture:** Modular Monolithic with Domain-Driven Design

### Key Features
- **Multi-tenant SaaS** supporting multiple universities
- **Arabic language support** with RTL layout
- **JWT-based authentication** with role-based access control
- **Real-time messaging** using WebSocket
- **File management** with virus scanning
- **Email notifications** via Brevo SMTP
- **Performance optimization** using virtual threads and Redis caching

### Domain Model
- **Universities** - Multi-tenant isolation
- **Users** - Students, Supervisors, Admins
- **Projects** - Graduation projects with lifecycle management
- **Tasks** - Project tasks with assignments and deadlines
- **Files** - Document management with versioning
- **Messages** - Real-time communication
- **Notifications** - Email and in-app notifications

## Quick Start Commands

### Development Setup
```bash
# Setup development environment
claude-flow commands development setup-environment

# Start required services (PostgreSQL, Redis)
docker-compose up -d postgres redis

# Run database migrations
mvn flyway:migrate

# Start application in development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing Commands
```bash
# Run all tests
mvn clean test

# Run with coverage
mvn clean test jacoco:report

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Run performance tests
k6 run --env JWT_TOKEN=$JWT_TOKEN performance-tests/load-test.js
```

### Postman Testing Commands
```bash
# Install Newman for CLI testing
npm install -g newman

# Run single endpoint collection
newman run postman/auth-register/Auth-Register.postman_collection.json \
    -e postman/auth-register/Takharrujy-Dev.postman_environment.json

# Run all endpoint collections
for dir in postman/*/; do
    collection=$(find "$dir" -name "*.postman_collection.json")
    env=$(find "$dir" -name "*Dev.postman_environment.json")
    newman run "$collection" -e "$env" --reporters cli
done

# Generate test report
newman run collection.json -e environment.json --reporters json \
    --reporter-json-export results.json
```

### Database Operations
```bash
# Check migration status
mvn flyway:info

# Apply migrations
mvn flyway:migrate

# Connect to database
psql -h localhost -U takharrujy_user -d takharrujy_db
```

### Deployment Commands
```bash
# Build Docker image
docker build -t takharrujy/backend:latest .

# Deploy to staging
./scripts/deploy.sh latest staging

# Deploy to production
./scripts/deploy.sh latest production
```

## Agent Usage

### Backend Developer Agent
The backend developer agent specializes in Spring Boot development with Arabic language support:

```bash
# Generate new controller
claude-flow agent backend-dev generate-controller --entity=Task --operations=CRUD

# Create service with caching
claude-flow agent backend-dev generate-service --entity=Task --with-cache=true

# Generate repository with Arabic search
claude-flow agent backend-dev generate-repository --entity=Task --with-search=true

# Create comprehensive tests
claude-flow agent backend-dev generate-tests --type=integration --entity=Task
```

## Workflow Integration

### Development Workflow
1. **Planning** - Requirements analysis and technical design
2. **TDD Development** - Test-driven development approach
3. **Integration Testing** - Comprehensive testing strategy
4. **Code Review** - Quality assurance and peer review
5. **Deployment** - Automated deployment pipeline

### Quality Gates
- Unit test coverage > 80%
- Integration test coverage > 70%
- Security scan passes (0 vulnerabilities)
- Performance benchmarks met (< 500ms response time)
- Code quality metrics satisfied

## Best Practices

### 1. Arabic Language Support
```java
// Proper Arabic content validation
@Pattern(regexp = "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\d\\p{Punct}]+$")
private String arabicContent;

// Localization configuration
@Bean
public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(new Locale("ar", "SA"));
    return resolver;
}
```

### 2. Virtual Threads Usage
```java
// Async service operations with virtual threads
@Async("virtualThreadExecutor")
public CompletableFuture<ProjectDTO> createProject(CreateProjectRequest request) {
    // Implementation using virtual threads
}

// Virtual thread executor configuration
@Bean("virtualThreadExecutor")
public Executor taskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
}
```

### 3. Security Implementation
```java
// Method-level security
@PreAuthorize("hasRole('STUDENT') or hasRole('SUPERVISOR')")
public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectRequest request) {
    // Implementation
}

// Row-level security in PostgreSQL
CREATE POLICY users_university_isolation ON users
    FOR ALL TO takharrujy_user
    USING (university_id = current_setting('app.current_university_id')::bigint);
```

### 4. Performance Optimization
```java
// Redis caching
@Cacheable(value = "projects", key = "#projectId")
public ProjectDTO getProject(Long projectId) {
    // Implementation
}

// Database query optimization
@Query("""
    SELECT p FROM Project p 
    WHERE p.universityId = :universityId 
    AND (:search IS NULL OR 
         to_tsvector('arabic', p.title || ' ' || p.description) @@ plainto_tsquery('arabic', :search))
    """)
List<Project> searchProjectsInArabic(@Param("universityId") Long universityId, @Param("search") String search);
```

## Monitoring and Observability

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database connectivity
curl http://localhost:8080/actuator/health/db

# Redis connectivity
curl http://localhost:8080/actuator/health/redis
```

### Metrics
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Custom business metrics
curl http://localhost:8080/actuator/metrics/projects.created
```

## Configuration Management

### Environment Variables
```bash
# Database configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/takharrujy_db
SPRING_DATASOURCE_USERNAME=takharrujy_user
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Redis configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# External services
AZURE_STORAGE_CONNECTION_STRING=${AZURE_STORAGE_CONNECTION_STRING}
BREVO_SMTP_KEY=${BREVO_SMTP_KEY}
JWT_SECRET=${JWT_SECRET}
```

### Profile-specific Configuration
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/takharrujy_dev
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
logging:
  level:
    com.university.takharrujy: DEBUG
    org.springframework.security: DEBUG
```

## Troubleshooting

### Common Issues
1. **Database Connection**: Check PostgreSQL service and credentials
2. **Redis Connection**: Verify Redis service is running
3. **Arabic Text Issues**: Ensure proper UTF-8 encoding and Arabic locale
4. **Virtual Thread Issues**: Check Java 24 compatibility and configuration
5. **Memory Issues**: Monitor JVM heap usage and adjust settings

### Debugging Commands
```bash
# Check application logs
docker-compose logs -f app

# Monitor database connections
docker-compose exec postgres pg_stat_activity

# Check Redis cache status
docker-compose exec redis redis-cli info memory

# Generate thread dump
jcmd <pid> Thread.print

# Generate heap dump
jcmd <pid> GC.run_finalization
```

## Contributing

When adding new commands or agents to this directory:

1. **Follow naming conventions** - Use kebab-case for files
2. **Include comprehensive documentation** - Explain purpose and usage
3. **Provide working examples** - Include code snippets and commands
4. **Test thoroughly** - Ensure all examples work correctly
5. **Update this README** - Keep the directory structure current

## Support

For issues or questions about the Takharrujy platform development:

1. Check the documentation in this directory
2. Review the context files in the `context/` directory
3. Run diagnostic commands for troubleshooting
4. Consult the development team for complex issues

---

**Last Updated:** December 2024  
**Version:** 1.0  
**Project:** Takharrujy (تخرجي) Platform
