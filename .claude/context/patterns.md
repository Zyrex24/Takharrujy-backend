# Takharrujy Platform - Coding and Architectural Patterns

## Authentication and Security Patterns

### JWT Authentication Pattern
**Implementation:** Spring Security 6.x with JWT Bearer tokens

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/system/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/universities").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/supervisor/**").hasRole("SUPERVISOR")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKey()).build();
    }
}
```

**Key Features:**
- Stateless authentication with JWT tokens
- Role-based authorization with method-level security
- Automatic token validation and user context injection
- Redis-based token blacklisting for logout

### Role-Based Access Control Pattern
**Implementation:** Custom authorization with university context

```java
@PreAuthorize("hasRole('STUDENT') and @securityService.canAccessProject(#projectId, authentication)")
@GetMapping("/projects/{projectId}")
public ResponseEntity<ProjectResponse> getProject(@PathVariable Long projectId) {
    // Implementation
}

@Component
public class SecurityService {
    
    public boolean canAccessProject(Long projectId, Authentication auth) {
        UserDetails user = (UserDetails) auth.getPrincipal();
        return projectService.isUserMemberOrSupervisor(projectId, user.getUsername());
    }
    
    public boolean isSameUniversity(Long userId, Authentication auth) {
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        return userService.sharesSameUniversity(userId, currentUser.getUsername());
    }
}
```

**Authorization Hierarchy:**
- **Admin:** Full system access across all universities
- **Supervisor:** Access to assigned projects and students within university
- **Student:** Access to own projects and team collaborations

### Password Security Pattern
**Implementation:** bcrypt with strength 12 and validation

```java
@Component
public class PasswordService {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final PasswordValidator validator = new PasswordValidator();
    
    public String hashPassword(String plainPassword) {
        validatePassword(plainPassword);
        return encoder.encode(plainPassword);
    }
    
    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new ValidationException("Password must contain at least 8 characters with uppercase, lowercase, number, and special character");
        }
    }
}
```

## Validation and Input Handling Patterns

### Arabic Text Validation Pattern
**Implementation:** Custom Bean Validation with Arabic character support

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ArabicTextValidator.class)
public @interface ValidArabicText {
    String message() default "Invalid Arabic text format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int minLength() default 1;
    int maxLength() default 255;
    boolean allowEnglish() default true;
    boolean required() default true;
}

public class ArabicTextValidator implements ConstraintValidator<ValidArabicText, String> {
    
    private static final String ARABIC_PATTERN = "[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s]+";
    private static final String MIXED_PATTERN = "[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\u0020-\\u007E\\s]+";
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return !annotation.required();
        }
        
        String normalizedText = normalizeArabicText(value);
        String pattern = annotation.allowEnglish() ? MIXED_PATTERN : ARABIC_PATTERN;
        
        return normalizedText.matches(pattern) && 
               normalizedText.length() >= annotation.minLength() && 
               normalizedText.length() <= annotation.maxLength();
    }
    
    private String normalizeArabicText(String text) {
        return text.replaceAll("[\\u064B-\\u0652]", "") // Remove diacritics
                   .replaceAll("\\s+", " ") // Normalize whitespace
                   .trim();
    }
}
```

### University Email Validation Pattern
**Implementation:** Domain-based email validation with university context

```java
@Component
public class UniversityEmailValidator implements ConstraintValidator<ValidUniversityEmail, String> {
    
    @Autowired
    private UniversityService universityService;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || !isValidEmailFormat(email)) {
            return false;
        }
        
        String domain = extractDomain(email);
        University university = universityService.findByDomain(domain);
        
        if (university == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Email domain '" + domain + "' is not associated with any registered university")
                .addConstraintViolation();
            return false;
        }
        
        return university.isActive();
    }
}
```

### Global Exception Handling Pattern
**Implementation:** Centralized error handling with i18n support

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        String message = getLocalizedMessage("validation.error", ex.getMessage(), request);
        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .error(ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .details(ex.getFieldErrors())
                .build())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String message = getLocalizedMessage("access.denied", null, request);
        ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .error(ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .message(message)
                .build())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    private String getLocalizedMessage(String key, String defaultMessage, HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            return defaultMessage != null ? defaultMessage : key;
        }
    }
}
```

## Database and Persistence Patterns

### Multi-tenant Row-Level Security Pattern
**Implementation:** PostgreSQL RLS with Spring Security context

```sql
-- Enable RLS on all tenant-scoped tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY users_tenant_policy ON users
    USING (university_id = current_setting('app.current_university_id')::bigint);

CREATE POLICY projects_tenant_policy ON projects  
    USING (university_id = current_setting('app.current_university_id')::bigint);

-- Set university context in application
SET app.current_university_id = 1;
```

```java
@Component
public class TenantContext {
    
    private static final String UNIVERSITY_ID_KEY = "app.current_university_id";
    
    @EventListener
    public void setTenantContext(AuthenticationSuccessEvent event) {
        UserDetails user = (UserDetails) event.getAuthentication().getPrincipal();
        Long universityId = userService.getUniversityId(user.getUsername());
        
        // Set PostgreSQL session variable
        jdbcTemplate.execute("SET " + UNIVERSITY_ID_KEY + " = " + universityId);
    }
    
    @PreDestroy
    public void clearTenantContext() {
        jdbcTemplate.execute("RESET " + UNIVERSITY_ID_KEY);
    }
}
```

### JPA Entity Pattern with Arabic Support
**Implementation:** Base entity with audit fields and Arabic text handling

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "university_id", nullable = false)
    private Long universityId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Version
    private Long version;
}

@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    
    @ValidArabicText(maxLength = 200, allowEnglish = true)
    @Column(name = "title", nullable = false)
    private String title;
    
    @ValidArabicText(maxLength = 2000, allowEnglish = true)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();
}
```

### Repository Pattern with Specifications
**Implementation:** Custom repositories with dynamic queries

```java
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    
    @Query("SELECT p FROM Project p WHERE p.universityId = :universityId AND p.status = :status")
    List<Project> findByUniversityAndStatus(@Param("universityId") Long universityId, 
                                          @Param("status") ProjectStatus status);
    
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);
}

@Component
public class ProjectSpecifications {
    
    public static Specification<Project> hasTitle(String title) {
        return (root, query, builder) -> 
            title == null ? null : builder.like(
                builder.lower(root.get("title")), 
                "%" + title.toLowerCase() + "%"
            );
    }
    
    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, builder) -> 
            status == null ? null : builder.equal(root.get("status"), status);
    }
    
    public static Specification<Project> belongsToUniversity(Long universityId) {
        return (root, query, builder) -> 
            builder.equal(root.get("universityId"), universityId);
    }
}
```

### Database Migration Pattern
**Implementation:** Flyway with environment-specific configurations

```sql
-- V1__Initial_Schema.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Universities table
CREATE TABLE universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255) NOT NULL,
    domain VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Users table with RLS
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Indexes for performance
CREATE INDEX idx_users_university_id ON users(university_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_projects_university_status ON projects(university_id, status);
```

## API Design Patterns

### RESTful Controller Pattern
**Implementation:** Consistent API design with validation and error handling

```java
@RestController
@RequestMapping("/api/v1/projects")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) ProjectStatus status,
            Authentication authentication) {
        
        ProjectSearchCriteria criteria = ProjectSearchCriteria.builder()
            .title(title)
            .status(status)
            .universityId(getCurrentUniversityId(authentication))
            .build();
            
        Page<ProjectResponse> projects = projectService.searchProjects(criteria, 
            PageRequest.of(page, size));
            
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved successfully"));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            Authentication authentication) {
        
        ProjectResponse project = projectService.createProject(request, 
            getCurrentUserId(authentication));
            
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(project, "Project created successfully"));
    }
}
```

### DTO Pattern with Validation
**Implementation:** Request/Response DTOs with comprehensive validation

```java
public record ProjectCreateRequest(
    @ValidArabicText(minLength = 5, maxLength = 200, allowEnglish = true)
    @NotBlank(message = "Project title is required")
    String title,
    
    @ValidArabicText(minLength = 20, maxLength = 2000, allowEnglish = true)
    @NotBlank(message = "Project description is required")
    String description,
    
    @NotNull(message = "Project type is required")
    ProjectType projectType,
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    LocalDate startDate,
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    LocalDate dueDate,
    
    @Valid
    @Size(min = 1, max = 3, message = "Team must have 1-3 additional members")
    List<TeamMemberRequest> teamMembers,
    
    Long preferredSupervisorId
) {
    
    @AssertTrue(message = "Due date must be after start date")
    public boolean isDueDateAfterStartDate() {
        return dueDate == null || startDate == null || dueDate.isAfter(startDate);
    }
}

public record ProjectResponse(
    Long id,
    String title,
    String description,
    ProjectType projectType,
    ProjectStatus status,
    UniversityResponse university,
    UserResponse teamLeader,
    List<ProjectMemberResponse> members,
    LocalDate startDate,
    LocalDate dueDate,
    Double progressPercentage,
    Integer totalTasks,
    Integer completedTasks,
    Instant createdAt,
    Instant updatedAt
) {}
```

## Caching Patterns

### Redis Caching Pattern
**Implementation:** Multi-level caching with TTL and invalidation

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}

@Service
public class ProjectService {
    
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProject(Long projectId) {
        // Implementation
    }
    
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        // Implementation
    }
    
    @Caching(evict = {
        @CacheEvict(value = "projects", key = "#result.id"),
        @CacheEvict(value = "user-projects", allEntries = true)
    })
    public ProjectResponse createProject(ProjectCreateRequest request, Long userId) {
        // Implementation
    }
}
```

### Session Management Pattern
**Implementation:** Redis-based session storage with JWT

```java
@Component
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SESSION_PREFIX = "session:";
    private static final Duration SESSION_TIMEOUT = Duration.ofHours(24);
    
    public void createSession(String userId, String jwtToken, UserSessionData sessionData) {
        String sessionKey = SESSION_PREFIX + userId;
        
        Map<String, Object> sessionMap = Map.of(
            "token", jwtToken,
            "data", sessionData,
            "lastAccess", Instant.now()
        );
        
        redisTemplate.opsForHash().putAll(sessionKey, sessionMap);
        redisTemplate.expire(sessionKey, SESSION_TIMEOUT);
    }
    
    public boolean isSessionValid(String userId, String jwtToken) {
        String sessionKey = SESSION_PREFIX + userId;
        String storedToken = (String) redisTemplate.opsForHash().get(sessionKey, "token");
        
        if (storedToken != null && storedToken.equals(jwtToken)) {
            // Update last access time
            redisTemplate.opsForHash().put(sessionKey, "lastAccess", Instant.now());
            redisTemplate.expire(sessionKey, SESSION_TIMEOUT);
            return true;
        }
        
        return false;
    }
}
```

## File Management Patterns

### Secure File Upload Pattern
**Implementation:** Multi-stage validation with virus scanning

```java
@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT') or hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) throws IOException {
        
        // Validate file
        validateFileUpload(file);
        
        // Check project access
        if (!projectService.canUserAccessProject(projectId, getCurrentUserId(authentication))) {
            throw new AccessDeniedException("Cannot upload file to this project");
        }
        
        FileUploadRequest request = FileUploadRequest.builder()
            .file(file)
            .projectId(projectId)
            .description(description)
            .uploadedBy(getCurrentUserId(authentication))
            .build();
        
        FileResponse fileResponse = fileService.uploadFile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(fileResponse, "File uploaded successfully"));
    }
    
    private void validateFileUpload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds 100MB limit");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ValidationException("File type not allowed: " + contentType);
        }
    }
}

@Service
public class FileService {
    
    public FileResponse uploadFile(FileUploadRequest request) throws IOException {
        // 1. Virus scan
        VirusScanResult scanResult = virusScanService.scanFile(request.getFile());
        if (!scanResult.isClean()) {
            throw new SecurityException("File contains malicious content");
        }
        
        // 2. Generate unique filename
        String uniqueFilename = generateUniqueFilename(request.getFile().getOriginalFilename());
        
        // 3. Upload to storage
        String storageUrl = storageService.uploadFile(request.getFile(), uniqueFilename);
        
        // 4. Save metadata
        FileEntity fileEntity = FileEntity.builder()
            .originalFilename(request.getFile().getOriginalFilename())
            .uniqueFilename(uniqueFilename)
            .contentType(request.getFile().getContentType())
            .fileSize(request.getFile().getSize())
            .storageUrl(storageUrl)
            .projectId(request.getProjectId())
            .uploadedBy(request.getUploadedBy())
            .virusScanResult(scanResult.getStatus())
            .build();
        
        fileEntity = fileRepository.save(fileEntity);
        return fileMapper.toResponse(fileEntity);
    }
}
```

## Testing Patterns

### Unit Testing Pattern
**Implementation:** Comprehensive testing with mocks and Arabic data

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private ProjectService projectService;
    
    @Test
    @DisplayName("Should create project with Arabic title successfully")
    void shouldCreateProjectWithArabicTitle() {
        // Given
        ProjectCreateRequest request = ProjectCreateRequest.builder()
            .title("نظام إدارة المكتبات الذكي")
            .description("تطوير نظام إدارة مكتبات ذكي باستخدام الذكاء الاصطناعي")
            .projectType(ProjectType.DEVELOPMENT)
            .startDate(LocalDate.now().plusDays(7))
            .dueDate(LocalDate.now().plusMonths(6))
            .teamMembers(List.of(
                TeamMemberRequest.builder()
                    .email("teammate@cu.edu.eg")
                    .role(MemberRole.MEMBER)
                    .build()
            ))
            .build();
        
        User teamLeader = createTestUser("أحمد محمد", "ahmed@cu.edu.eg", UserRole.STUDENT);
        Project savedProject = createTestProject(request.title(), ProjectStatus.DRAFT);
        
        when(userService.getCurrentUser()).thenReturn(teamLeader);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        
        // When
        ProjectResponse response = projectService.createProject(request, teamLeader.getId());
        
        // Then
        assertThat(response.title()).isEqualTo("نظام إدارة المكتبات الذكي");
        assertThat(response.status()).isEqualTo(ProjectStatus.DRAFT);
        verify(notificationService).sendProjectCreatedNotification(any(Project.class));
    }
}
```

### Integration Testing Pattern
**Implementation:** TestContainers with real database

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class ProjectIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("takharrujy_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Test
    @Order(1)
    void shouldCreateProjectWithAuthenticatedUser() {
        // Given
        String authToken = authenticateTestUser();
        ProjectCreateRequest request = createTestProjectRequest();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<ProjectCreateRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<ApiResponse<ProjectResponse>> response = restTemplate.exchange(
            "/api/v1/projects", 
            HttpMethod.POST, 
            entity, 
            new ParameterizedTypeReference<>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().title()).isEqualTo(request.title());
        
        // Verify database
        List<Project> projects = projectRepository.findAll();
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getTitle()).isEqualTo(request.title());
    }
}
```

---

**Patterns Documentation Status:** ✅ Complete  
**Coverage:** Authentication, validation, database, API, caching, file management, testing  
**Implementation:** Production-ready patterns with Arabic language support  
**Testing:** Comprehensive unit and integration test patterns  
**Last Updated:** December 2024

These patterns provide a comprehensive foundation for implementing the Takharrujy platform with consistent, maintainable, and secure code following Spring Boot and Java best practices.
