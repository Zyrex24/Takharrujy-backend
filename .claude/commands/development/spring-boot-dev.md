# Spring Boot Development Commands

## Project Context
- **Framework:** Spring Boot 3.4.x with Spring 6.2.x
- **Java Version:** Java 24 (Project Loom - Virtual Threads)
- **Architecture:** Modular Monolithic with DDD
- **Database:** PostgreSQL 16.x with Row-Level Security
- **Cache:** Redis 7.x
- **Build Tool:** Maven

## Development Commands

### 1. Project Structure Setup
```bash
# Create modular structure
mkdir -p src/main/java/com/university/takharrujy/{presentation,application,domain,infrastructure}
mkdir -p src/main/java/com/university/takharrujy/presentation/{controllers,dto,security}
mkdir -p src/main/java/com/university/takharrujy/application/{services,config}
mkdir -p src/main/java/com/university/takharrujy/domain/{entities,repositories,enums}
mkdir -p src/main/java/com/university/takharrujy/infrastructure/{persistence,external,cache}
```

### 2. Entity Development
```java
// Generate JPA entities with proper validation
@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Project title is required")
    @Size(max = 200)
    private String title;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 3. Service Layer Pattern
```java
@Service
@Transactional
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Async("virtualThreadExecutor")
    public CompletableFuture<ProjectDTO> createProject(CreateProjectRequest request) {
        // Implementation with virtual threads
    }
}
```

### 4. Controller Development
```java
@RestController
@RequestMapping("/api/v1/projects")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Validated
public class ProjectController {
    
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
        @Valid @RequestBody CreateProjectRequest request,
        Authentication authentication) {
        // Implementation
    }
}
```

### 5. Testing Commands
```bash
# Run unit tests
mvn test

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Run with coverage
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=ProjectServiceTest
```

### 6. Database Migration
```bash
# Create new migration
mvn flyway:info
mvn flyway:migrate
mvn flyway:validate
```

### 7. Redis Cache Operations
```java
// Cache configuration
@Cacheable(value = "projects", key = "#projectId")
public ProjectDTO getProject(Long projectId) {
    // Implementation
}

@CacheEvict(value = "projects", key = "#projectId")
public void updateProject(Long projectId, UpdateProjectRequest request) {
    // Implementation
}
```

## Development Workflow

1. **Feature Development:**
   - Create feature branch from main
   - Implement following TDD approach
   - Write unit and integration tests
   - Update documentation

2. **Code Quality:**
   - Follow Spring Boot best practices
   - Use proper validation annotations
   - Implement proper exception handling
   - Add comprehensive logging

3. **Security Implementation:**
   - JWT token validation
   - Method-level security with @PreAuthorize
   - Input sanitization
   - SQL injection prevention

4. **Performance Optimization:**
   - Use virtual threads for async operations
   - Implement Redis caching
   - Optimize database queries
   - Add proper indexing

## Arabic Language Support

```java
// Localization configuration
@Configuration
public class LocalizationConfig {
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("ar", "SA"));
        resolver.setSupportedLocales(Arrays.asList(
            new Locale("ar", "SA"),
            new Locale("en", "US")
        ));
        return resolver;
    }
}

// Arabic content validation
@Pattern(regexp = "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\d\\p{Punct}]+$", 
         message = "Content must be in Arabic")
private String arabicContent;
```

## Command Aliases

```bash
# Quick development commands
alias takharrujy-test="mvn clean test"
alias takharrujy-run="mvn spring-boot:run -Dspring-boot.run.profiles=dev"
alias takharrujy-build="mvn clean package -DskipTests"
alias takharrujy-docker="docker-compose up -d postgres redis"
```
