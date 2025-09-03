# Takharrujy Platform - Technical Design Document

**Version:** 1.0  
**Date:** September 2025  
**Authors:** Backend Development Team  
**Reviewers:** Technical Lead, Product Owner  
**Status:** Active Development  

## 1. Document Overview

### 1.1 Purpose
This Technical Design Document specifies the detailed technical implementation for Takharrujy, a university graduation project management platform. It provides concrete implementation guidance for the 2-person backend development team working within a 3-week timeline (2 weeks Sprint 1 + 1 week Sprint 1.5).

### 1.2 Scope
This document covers backend architecture, database design, API specifications, security implementation, and deployment strategies. Frontend and mobile client implementations are referenced where they impact backend design decisions.

### 1.3 Audience
- Backend developers implementing the system
- Frontend and mobile teams requiring API integration details
- DevOps engineers responsible for deployment and infrastructure
- QA engineers designing test strategies

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │   Mobile App    │    │   Admin Panel   │
│   (React)       │    │   (Flutter)     │    │   (React)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Load Balancer │
                    │   (DigitalOcean)│
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Spring Boot   │
                    │   Application   │
                    │   (Port 8080)   │
                    └─────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │   Redis Cache   │    │   File Storage  │
│   Database      │    │   (Session +    │    │   (Azure Blob / │
│                 │    │    Cache)       │    │    DO Spaces)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2.2 Modular Monolithic Structure

The application follows a domain-driven design within a single deployable artifact:

```
src/main/java/com/university/takharrujy/
├── config/                 # Spring configuration classes
├── security/              # Authentication & authorization
├── common/                # Shared utilities and exceptions
├── user/                  # User management domain
├── project/               # Project management domain
├── task/                  # Task management domain
├── file/                  # File management domain
├── notification/          # Notification system
├── messaging/            # Real-time messaging
└── integration/          # External service integrations
```

### 2.3 Technology Stack

**Core Framework:**
- Java 24 with Virtual Threads (Project Loom)
- Spring Boot 3.4.x (Spring 6.2.x)
- Spring Security 6.3.x with OAuth2
- Spring Data JPA with Hibernate 6.4.x
- Spring WebSocket for real-time features

**Data Layer:**
- PostgreSQL 16.x (primary database)
- Redis 7.x (caching and session storage)
- Flyway (database migrations)

**Integration:**
- Azure Blob Storage / DigitalOcean Spaces (file storage)
- SendGrid / AWS SES (email notifications)
- Azure Communication Services (SMS notifications)

**Build & Deployment:**
- Maven 3.9.x
- Docker containerization
- GitHub Actions (CI/CD)

## 3. Database Design

### 3.1 Entity Relationship Diagram

```sql
-- Core Entities Schema
CREATE TABLE universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(100) UNIQUE NOT NULL,
    country_code VARCHAR(3),
    timezone VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL,
    university_id BIGINT REFERENCES universities(id),
    student_id VARCHAR(50),
    department VARCHAR(100),
    phone_number VARCHAR(20),
    preferred_language VARCHAR(10) DEFAULT 'en',
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    project_type project_type NOT NULL,
    status project_status DEFAULT 'DRAFT',
    category VARCHAR(100),
    university_id BIGINT REFERENCES universities(id),
    supervisor_id BIGINT REFERENCES users(id),
    team_leader_id BIGINT REFERENCES users(id),
    start_date DATE,
    due_date DATE,
    submission_date TIMESTAMP,
    metadata JSONB, -- Flexible storage for university-specific fields
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT unique_project_title_per_university 
        UNIQUE(title, university_id)
);

CREATE TABLE project_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    role member_role DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT NOW(),
    invitation_status invitation_status DEFAULT 'PENDING',
    
    UNIQUE(project_id, user_id)
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_to BIGINT REFERENCES users(id),
    created_by BIGINT REFERENCES users(id),
    status task_status DEFAULT 'TODO',
    priority task_priority DEFAULT 'MEDIUM',
    due_date TIMESTAMP,
    estimated_hours INTEGER,
    actual_hours INTEGER,
    parent_task_id BIGINT REFERENCES tasks(id),
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE TABLE deliverables (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    deliverable_type deliverable_type NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    submitted_at TIMESTAMP,
    submitted_by BIGINT REFERENCES users(id),
    status deliverable_status DEFAULT 'PENDING',
    feedback TEXT,
    grade DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    file_hash VARCHAR(64) UNIQUE,
    storage_path VARCHAR(500),
    storage_provider VARCHAR(50),
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    deliverable_id BIGINT REFERENCES deliverables(id),
    uploaded_by BIGINT REFERENCES users(id),
    version INTEGER DEFAULT 1,
    parent_file_id BIGINT REFERENCES files(id),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type notification_type NOT NULL,
    priority notification_priority DEFAULT 'MEDIUM',
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    read_at TIMESTAMP,
    sent_via JSONB, -- Track delivery channels (email, push, sms)
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    sender_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    message_type message_type DEFAULT 'TEXT',
    reply_to_id BIGINT REFERENCES messages(id),
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 3.2 Database Enums and Types

```sql
-- Custom Types
CREATE TYPE user_role AS ENUM ('STUDENT', 'SUPERVISOR', 'ADMIN');
CREATE TYPE project_type AS ENUM ('THESIS', 'CAPSTONE', 'RESEARCH', 'DEVELOPMENT');
CREATE TYPE project_status AS ENUM ('DRAFT', 'ACTIVE', 'SUBMITTED', 'APPROVED', 'REJECTED', 'COMPLETED');
CREATE TYPE member_role AS ENUM ('LEADER', 'MEMBER');
CREATE TYPE invitation_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED', 'BLOCKED');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE deliverable_type AS ENUM ('PROPOSAL', 'PROGRESS_REPORT', 'FINAL_REPORT', 'PRESENTATION', 'CODE', 'DOCUMENTATION');
CREATE TYPE deliverable_status AS ENUM ('PENDING', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REVISION_REQUIRED', 'REJECTED');
CREATE TYPE notification_type AS ENUM ('TASK_ASSIGNED', 'DEADLINE_REMINDER', 'SUBMISSION_RECEIVED', 'FEEDBACK_AVAILABLE', 'PROJECT_UPDATE');
CREATE TYPE notification_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE message_type AS ENUM ('TEXT', 'FILE_SHARE', 'SYSTEM_NOTIFICATION');
```

### 3.3 Indexes and Performance Optimization

```sql
-- Performance Indexes
CREATE INDEX idx_projects_supervisor ON projects(supervisor_id);
CREATE INDEX idx_projects_status_university ON projects(status, university_id);
CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_files_project_deliverable ON files(project_id, deliverable_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE read_at IS NULL;
CREATE INDEX idx_messages_project_created ON messages(project_id, created_at DESC);

-- Full-text search indexes
CREATE INDEX idx_projects_search ON projects USING gin(to_tsvector('english', title || ' ' || description));
CREATE INDEX idx_tasks_search ON tasks USING gin(to_tsvector('english', title || ' ' || description));
```

## 4. API Design

### 4.1 RESTful API Structure

**Base URL:** `https://api.takharrujy.com/v1`

**Authentication:** Bearer token (JWT) in Authorization header

### 4.2 Core API Endpoints

#### 4.2.1 Authentication & User Management

```yaml
# Authentication
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
POST /auth/forgot-password
POST /auth/reset-password

# User Profile
GET /users/me
PUT /users/me
GET /users/{id}
PUT /users/{id}/avatar
```

#### 4.2.2 Project Management

```yaml
# Projects
GET /projects                    # List user's projects
POST /projects                   # Create new project
GET /projects/{id}               # Get project details
PUT /projects/{id}               # Update project
DELETE /projects/{id}            # Delete project
POST /projects/{id}/members      # Invite team member
PUT /projects/{id}/members/{userId}  # Update member role
DELETE /projects/{id}/members/{userId}  # Remove member

# Project Statistics
GET /projects/{id}/stats         # Get project analytics
GET /projects/{id}/timeline      # Get project timeline
```

#### 4.2.3 Task Management

```yaml
# Tasks
GET /projects/{projectId}/tasks          # List project tasks
POST /projects/{projectId}/tasks         # Create task
GET /tasks/{id}                          # Get task details  
PUT /tasks/{id}                          # Update task
DELETE /tasks/{id}                       # Delete task
POST /tasks/{id}/complete                # Mark complete
GET /tasks/assigned-to-me                # Get user's tasks
```

#### 4.2.4 File Management

```yaml
# File Operations
POST /projects/{projectId}/files         # Upload file
GET /projects/{projectId}/files          # List project files
GET /files/{id}                          # Download file
DELETE /files/{id}                       # Delete file
GET /files/{id}/versions                 # Get file versions
POST /files/{id}/share                   # Generate share link
```

### 4.3 Data Transfer Objects (DTOs)

#### 4.3.1 Core DTOs

```java
// User DTOs
@Data
public class UserRegistrationRequest {
    @Email @NotBlank private String email;
    @Size(min = 8, max = 64) private String password;
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @NotNull private UserRole role;
    @NotBlank private String universityDomain;
    private String studentId;
    private String department;
    private String phoneNumber;
    private String preferredLanguage = "en";
}

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String university;
    private String department;
    private String preferredLanguage;
    private String avatarUrl;
    private LocalDateTime lastLoginAt;
}

// Project DTOs
@Data
public class ProjectCreateRequest {
    @NotBlank @Size(max = 255) private String title;
    @NotBlank @Size(max = 2000) private String description;
    @NotNull private ProjectType projectType;
    private String category;
    private Long preferredSupervisorId;
    private LocalDate startDate;
    private LocalDate dueDate;
    @Valid private List<TeamMemberInvitation> teamMembers;
}

@Data
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private ProjectType projectType;
    private ProjectStatus status;
    private String category;
    private UserResponse supervisor;
    private UserResponse teamLeader;
    private List<ProjectMemberResponse> members;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer totalTasks;
    private Integer completedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Task DTOs
@Data
public class TaskCreateRequest {
    @NotBlank @Size(max = 255) private String title;
    @Size(max = 2000) private String description;
    private Long assignedTo;
    @NotNull private TaskPriority priority;
    private LocalDateTime dueDate;
    private Integer estimatedHours;
    private Long parentTaskId;
}

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private UserResponse assignedTo;
    private UserResponse createdBy;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private Integer estimatedHours;
    private Integer actualHours;
    private List<TaskResponse> subtasks;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
```

### 4.4 Error Handling

```java
// Standardized Error Response
@Data
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private String path;
    private String timestamp;
    private Map<String, String> validationErrors;
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        return ErrorResponse.builder()
            .error("VALIDATION_ERROR")
            .message("Input validation failed")
            .status(400)
            .path(request.getRequestURI())
            .timestamp(Instant.now().toString())
            .validationErrors(ex.getFieldErrors())
            .build();
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return ErrorResponse.builder()
            .error("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .status(404)
            .path(request.getRequestURI())
            .timestamp(Instant.now().toString())
            .build();
    }
}
```

## 5. Security Implementation

### 5.1 Authentication Architecture

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/projects/**")
                    .hasAnyRole("STUDENT", "SUPERVISOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/projects")
                    .hasRole("STUDENT")
                .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())))
            .build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
            .withJwkSetUri("https://auth.takharrujy.com/.well-known/jwks.json")
            .build();
    }
}
```

### 5.2 Role-Based Access Control

```java
// Custom Security Annotations
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('SUPERVISOR') or (hasRole('STUDENT') and @projectService.isProjectMember(#projectId, authentication.name))")
public @interface ProjectMemberOrSupervisor {}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN') or @projectService.isProjectSupervisor(#projectId, authentication.name)")
public @interface ProjectSupervisorOrAdmin {}

// Usage in Controllers
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    
    @GetMapping("/{projectId}")
    @ProjectMemberOrSupervisor
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long projectId) {
        // Implementation
    }
    
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('STUDENT') and @projectService.isTeamLeader(#projectId, authentication.name)")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        // Implementation
    }
}
```

### 5.3 Data Validation and Sanitization

```java
// Custom Validation Annotations
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniversityEmailValidator.class)
public @interface UniversityEmail {
    String message() default "Email must be from a registered university domain";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validation Implementation
public class UniversityEmailValidator implements ConstraintValidator<UniversityEmail, String> {
    
    @Autowired
    private UniversityService universityService;
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String domain = email.substring(email.indexOf("@") + 1);
        return universityService.isValidUniversityDomain(domain);
    }
}
```

## 6. Service Layer Architecture

### 6.1 Domain Service Implementation

```java
// Core Service Interfaces
public interface ProjectService {
    ProjectResponse createProject(ProjectCreateRequest request, String userEmail);
    ProjectResponse getProject(Long projectId, String userEmail);
    List<ProjectResponse> getUserProjects(String userEmail);
    ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String userEmail);
    void deleteProject(Long projectId, String userEmail);
    void addTeamMember(Long projectId, String memberEmail, String userEmail);
    void removeTeamMember(Long projectId, Long memberId, String userEmail);
    boolean isProjectMember(Long projectId, String userEmail);
    boolean isTeamLeader(Long projectId, String userEmail);
    ProjectStatsResponse getProjectStats(Long projectId, String userEmail);
}

// Service Implementation
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ProjectMapper projectMapper;
    
    @Override
    public ProjectResponse createProject(ProjectCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
            
        validateProjectCreation(request, user);
        
        Project project = Project.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .projectType(request.getProjectType())
            .status(ProjectStatus.DRAFT)
            .category(request.getCategory())
            .universityId(user.getUniversityId())
            .teamLeaderId(user.getId())
            .startDate(request.getStartDate())
            .dueDate(request.getDueDate())
            .build();
            
        project = projectRepository.save(project);
        
        // Add team leader as first member
        addProjectMember(project, user, MemberRole.LEADER);
        
        // Send invitations to team members
        inviteTeamMembers(project, request.getTeamMembers(), user);
        
        // Notify preferred supervisor
        if (request.getPreferredSupervisorId() != null) {
            notifyPreferredSupervisor(project, request.getPreferredSupervisorId());
        }
        
        return projectMapper.toResponse(project);
    }
    
    private void validateProjectCreation(ProjectCreateRequest request, User user) {
        if (!UserRole.STUDENT.equals(user.getRole())) {
            throw new AccessDeniedException("Only students can create projects");
        }
        
        // Check for duplicate project titles
        if (projectRepository.existsByTitleAndUniversityId(
                request.getTitle(), user.getUniversityId())) {
            throw new DuplicateResourceException("Project title already exists");
        }
        
        // Validate team size
        if (request.getTeamMembers().size() > 4) {
            throw new ValidationException("Maximum team size is 4 members");
        }
    }
}
```

### 6.2 Task Management Service

```java
@Service
@Transactional
public class TaskServiceImpl implements TaskService {
    
    @Override
    public TaskResponse createTask(Long projectId, TaskCreateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        
        validateTaskCreation(project, user);
        
        Task task = Task.builder()
            .projectId(projectId)
            .title(request.getTitle())
            .description(request.getDescription())
            .assignedTo(request.getAssignedTo())
            .createdBy(user.getId())
            .status(TaskStatus.TODO)
            .priority(request.getPriority())
            .dueDate(request.getDueDate())
            .estimatedHours(request.getEstimatedHours())
            .parentTaskId(request.getParentTaskId())
            .build();
            
        task = taskRepository.save(task);
        
        // Send notification to assigned user
        if (task.getAssignedTo() != null && !task.getAssignedTo().equals(user.getId())) {
            notificationService.sendTaskAssignedNotification(task, user);
        }
        
        // Update project activity
        projectActivityService.recordActivity(projectId, 
            ActivityType.TASK_CREATED, user.getId(), task.getId());
            
        return taskMapper.toResponse(task);
    }
    
    @Override
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus status, String userEmail) {
        Task task = getTaskById(taskId);
        User user = getUserByEmail(userEmail);
        
        validateTaskStatusUpdate(task, status, user);
        
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        
        if (TaskStatus.COMPLETED.equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
            // Calculate actual hours if time tracking is enabled
            calculateActualHours(task);
        }
        
        task = taskRepository.save(task);
        
        // Send notifications for status changes
        notificationService.sendTaskStatusChangeNotification(task, oldStatus, user);
        
        // Update project progress
        updateProjectProgress(task.getProjectId());
        
        return taskMapper.toResponse(task);
    }
}
```

### 6.3 File Management Service

```java
@Service
@Transactional
public class FileServiceImpl implements FileService {
    
    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;
    private final VirusScanningService virusScanningService;
    
    @Override
    public FileResponse uploadFile(Long projectId, MultipartFile file, 
            String userEmail, Long deliverableId) {
        
        validateFileUpload(file, projectId, userEmail);
        
        // Scan for viruses
        ScanResult scanResult = virusScanningService.scanFile(file);
        if (!scanResult.isClean()) {
            throw new SecurityException("File contains malicious content");
        }
        
        // Calculate file hash for deduplication
        String fileHash = calculateFileHash(file);
        
        // Check for existing file with same hash
        Optional<File> existingFile = fileRepository.findByFileHash(fileHash);
        if (existingFile.isPresent()) {
            return createFileReference(existingFile.get(), projectId, deliverableId);
        }
        
        // Upload to storage provider
        String storagePath = fileStorageService.uploadFile(file, projectId);
        
        File fileEntity = File.builder()
            .filename(generateUniqueFilename(file.getOriginalFilename()))
            .originalFilename(file.getOriginalFilename())
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .fileHash(fileHash)
            .storagePath(storagePath)
            .storageProvider(fileStorageService.getProviderName())
            .projectId(projectId)
            .deliverableId(deliverableId)
            .uploadedBy(getUserByEmail(userEmail).getId())
            .version(1)
            .build();
            
        fileEntity = fileRepository.save(fileEntity);
        
        // Record file upload activity
        projectActivityService.recordActivity(projectId, 
            ActivityType.FILE_UPLOADED, fileEntity.getUploadedBy(), fileEntity.getId());
            
        return fileMapper.toResponse(fileEntity);
    }
    
    @Override
    public ResponseEntity<Resource> downloadFile(Long fileId, String userEmail) {
        File file = getFileById(fileId);
        validateFileAccess(file, userEmail);
        
        Resource resource = fileStorageService.downloadFile(file.getStoragePath());
        
        // Record download activity
        recordFileDownload(file, userEmail);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getOriginalFilename() + "\"")
            .body(resource);
    }
}
```

## 7. Integration Layer

### 7.1 File Storage Integration

```java
// Storage Provider Abstraction
public interface FileStorageService {
    String uploadFile(MultipartFile file, Long projectId);
    Resource downloadFile(String storagePath);
    void deleteFile(String storagePath);
    String generatePresignedUrl(String storagePath, Duration expiration);
    String getProviderName();
}

// Azure Blob Storage Implementation
@Service
@Profile("azure")
public class AzureBlobStorageService implements FileStorageService {
    
    private final BlobServiceClient blobServiceClient;
    
    @Override
    public String uploadFile(MultipartFile file, Long projectId) {
        try {
            String containerName = "project-files";
            String blobName = generateBlobName(projectId, file.getOriginalFilename());
            
            BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);
                
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            
            return blobClient.getBlobUrl();
            
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to Azure Blob Storage", e);
        }
    }
    
    private String generateBlobName(Long projectId, String originalFilename) {
        return String.format("projects/%d/%s/%s", 
            projectId, 
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            UUID.randomUUID() + "_" + originalFilename);
    }
}

// DigitalOcean Spaces Implementation
@Service
@Profile("digitalocean")
public class DigitalOceanSpacesService implements FileStorageService {
    
    private final AmazonS3 s3Client; // Uses S3-compatible API
    
    @Override
    public String uploadFile(MultipartFile file, Long projectId) {
        try {
            String bucketName = "takharrujy-files";
            String key = generateKey(projectId, file.getOriginalFilename());
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            s3Client.putObject(bucketName, key, file.getInputStream(), metadata);
            
            return s3Client.getUrl(bucketName, key).toString();
            
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to DigitalOcean Spaces", e);
        }
    }
}
```

### 7.2 Notification Service Integration

```java
// Multi-channel Notification Service
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PushNotificationService pushService;
    private final SmsService smsService;
    
    @Override
    @Async("notificationExecutor")
    public void sendTaskAssignedNotification(Task task, User assignedBy) {
        User assignedTo = userRepository.findById(task.getAssignedTo())
            .orElseThrow(() -> new UserNotFoundException("Assigned user not found"));
            
        Notification notification = Notification.builder()
            .userId(assignedTo.getId())
            .title("New Task Assigned")
            .message(String.format("You have been assigned task: %s", task.getTitle()))
            .type(NotificationType.TASK_ASSIGNED)
            .priority(NotificationPriority.MEDIUM)
            .relatedEntityType("TASK")
            .relatedEntityId(task.getId())
            .build();
            
        notification = notificationRepository.save(notification);
        
        // Send via multiple channels based on user preferences
        Map<String, Boolean> deliveryStatus = new HashMap<>();
        
        // Email notification
        try {
            emailService.sendTaskAssignedEmail(assignedTo, task, assignedBy);
            deliveryStatus.put("email", true);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            deliveryStatus.put("email", false);
        }
        
        // Push notification (if mobile app is installed)
        try {
            pushService.sendTaskAssignedPush(assignedTo, task);
            deliveryStatus.put("push", true);
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            deliveryStatus.put("push", false);
        }
        
        // Update notification with delivery status
        notification.setSentVia(deliveryStatus);
        notificationRepository.save(notification);
    }
}
```

### 7.3 Email Service Integration

```java
@Service
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final ThymeleafTemplateEngine templateEngine;
    
    @Override
    public void sendTaskAssignedEmail(User assignedTo, Task task, User assignedBy) {
        try {
            Context context = new Context();
            context.setVariable("recipientName", assignedTo.getFirstName());
            context.setVariable("taskTitle", task.getTitle());
            context.setVariable("taskDescription", task.getDescription());
            context.setVariable("assignedByName", assignedBy.getFirstName() + " " + assignedBy.getLastName());
            context.setVariable("dueDate", task.getDueDate());
            context.setVariable("projectTitle", getProjectTitle(task.getProjectId()));
            context.setVariable("dashboardUrl", "https://takharrujy.com/dashboard");
            
            // Use appropriate template based on user's language preference
            String templateName = "email/task-assigned-" + assignedTo.getPreferredLanguage();
            String htmlContent = templateEngine.process(templateName, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(assignedTo.getEmail());
            helper.setSubject("مهمة جديدة تم تعيينها لك - New Task Assigned");
            helper.setText(htmlContent, true);
            helper.setFrom("notifications@takharrujy.com");
            
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new EmailDeliveryException("Failed to send task assigned email", e);
        }
    }
}
```

## 8. Performance and Caching

### 8.1 Redis Caching Configuration

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            "projects", defaultConfig.entryTtl(Duration.ofMinutes(15)),
            "users", defaultConfig.entryTtl(Duration.ofHours(1)),
            "universities", defaultConfig.entryTtl(Duration.ofHours(24)),
            "project-stats", defaultConfig.entryTtl(Duration.ofMinutes(5))
        );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}

// Service-level Caching
@Service
public class ProjectServiceImpl implements ProjectService {
    
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProject(Long projectId, String userEmail) {
        // Implementation with cache-aside pattern
    }
    
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String userEmail) {
        // Implementation with cache invalidation
    }
    
    @Cacheable(value = "project-stats", key = "#projectId")
    public ProjectStatsResponse getProjectStats(Long projectId, String userEmail) {
        return ProjectStatsResponse.builder()
            .totalTasks(taskRepository.countByProjectId(projectId))
            .completedTasks(taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.COMPLETED))
            .totalFiles(fileRepository.countByProjectId(projectId))
            .teamMembersCount(projectMemberRepository.countByProjectId(projectId))
            .build();
    }
}
```

### 8.2 Database Query Optimization

```java
// Custom Repository with Optimized Queries
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    @Query("""
        SELECT p FROM Project p 
        LEFT JOIN FETCH p.supervisor 
        LEFT JOIN FETCH p.teamLeader 
        LEFT JOIN FETCH p.members m 
        LEFT JOIN FETCH m.user 
        WHERE p.id = :projectId
    """)
    Optional<Project> findByIdWithDetails(@Param("projectId") Long projectId);
    
    @Query("""
        SELECT p FROM Project p 
        JOIN p.members m 
        WHERE m.user.email = :userEmail 
        OR p.supervisor.email = :userEmail
        ORDER BY p.updatedAt DESC
    """)
    List<Project> findUserProjects(@Param("userEmail") String userEmail);
    
    @Query(value = """
        SELECT p.*, 
               (SELECT COUNT(*) FROM tasks t WHERE t.project_id = p.id) as total_tasks,
               (SELECT COUNT(*) FROM tasks t WHERE t.project_id = p.id AND t.status = 'COMPLETED') as completed_tasks
        FROM projects p 
        WHERE p.supervisor_id = :supervisorId 
        ORDER BY p.updated_at DESC
    """, nativeQuery = true)
    List<ProjectWithStats> findSupervisorProjectsWithStats(@Param("supervisorId") Long supervisorId);
}
```

## 9. Monitoring and Observability

### 9.1 Application Metrics

```java
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

// Service-level Metrics
@Service
public class ProjectServiceImpl implements ProjectService {
    
    private final Counter projectCreationCounter;
    private final Timer projectRetrievalTimer;
    
    public ProjectServiceImpl(MeterRegistry meterRegistry) {
        this.projectCreationCounter = Counter.builder("projects.created")
            .description("Number of projects created")
            .tag("service", "project")
            .register(meterRegistry);
            
        this.projectRetrievalTimer = Timer.builder("projects.retrieval.duration")
            .description("Time taken to retrieve project details")
            .register(meterRegistry);
    }
    
    @Override
    @Timed(name = "project.creation.time", description = "Time taken to create a project")
    public ProjectResponse createProject(ProjectCreateRequest request, String userEmail) {
        projectCreationCounter.increment();
        // Implementation
    }
    
    @Override
    public ProjectResponse getProject(Long projectId, String userEmail) {
        return projectRetrievalTimer.recordCallable(() -> {
            // Implementation
        });
    }
}
```

### 9.2 Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down()
            .withDetail("database", "PostgreSQL")
            .withDetail("status", "Connection validation failed")
            .build();
    }
}

@Component
public class FileStorageHealthIndicator implements HealthIndicator {
    
    private final FileStorageService fileStorageService;
    
    @Override
    public Health health() {
        try {
            // Test storage connectivity
            String testResult = fileStorageService.getProviderName();
            return Health.up()
                .withDetail("storage", testResult)
                .withDetail("status", "Available")
                .build();
        } catch (Exception e) {
            return Health.down(e)
                .withDetail("storage", fileStorageService.getProviderName())
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 10. Testing Strategy

### 10.1 Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private ProjectMapper projectMapper;
    
    @InjectMocks
    private ProjectServiceImpl projectService;
    
    @Test
    void createProject_ValidRequest_ReturnsProjectResponse() {
        // Given
        User student = createTestStudent();
        ProjectCreateRequest request = createTestProjectRequest();
        Project savedProject = createTestProject();
        ProjectResponse expectedResponse = createTestProjectResponse();
        
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(projectRepository.existsByTitleAndUniversityId(anyString(), anyLong())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toResponse(savedProject)).thenReturn(expectedResponse);
        
        // When
        ProjectResponse result = projectService.createProject(request, student.getEmail());
        
        // Then
        assertThat(result).isEqualTo(expectedResponse);
        verify(notificationService).sendProjectCreatedNotification(savedProject, student);
    }
    
    @Test
    void createProject_DuplicateTitle_ThrowsException() {
        // Given
        User student = createTestStudent();
        ProjectCreateRequest request = createTestProjectRequest();
        
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(projectRepository.existsByTitleAndUniversityId(request.getTitle(), student.getUniversityId()))
            .thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> projectService.createProject(request, student.getEmail()))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Project title already exists");
    }
}
```

### 10.2 Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-integration-test.properties")
@Testcontainers
class ProjectControllerIntegrationTest {
    
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
    private TestDataBuilder testDataBuilder;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    @Test
    void createProject_ValidRequestWithAuth_ReturnsCreatedProject() {
        // Given
        User student = testDataBuilder.createTestStudent();
        String authToken = generateAuthToken(student);
        ProjectCreateRequest request = testDataBuilder.createValidProjectRequest();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<ProjectCreateRequest> entity = new HttpEntity<>(request, headers);
        
        // When
        ResponseEntity<ProjectResponse> response = restTemplate.exchange(
            "/api/v1/projects",
            HttpMethod.POST,
            entity,
            ProjectResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getBody().getTeamLeader().getId()).isEqualTo(student.getId());
    }
}
```

### 10.3 Performance Testing

```java
@Test
@DirtiesContext
void performanceTest_ConcurrentProjectCreation() throws InterruptedException {
    int numberOfThreads = 50;
    int numberOfRequestsPerThread = 10;
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < numberOfThreads; i++) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                for (int j = 0; j < numberOfRequestsPerThread; j++) {
                    User student = testDataBuilder.createTestStudent();
                    String authToken = generateAuthToken(student);
                    ProjectCreateRequest request = testDataBuilder.createUniqueProjectRequest();
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(authToken);
                    HttpEntity<ProjectCreateRequest> entity = new HttpEntity<>(request, headers);
                    
                    ResponseEntity<ProjectResponse> response = restTemplate.exchange(
                        "/api/v1/projects",
                        HttpMethod.POST,
                        entity,
                        ProjectResponse.class
                    );
                    
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                }
            } finally {
                latch.countDown();
            }
        });
        futures.add(future);
    }
    
    // Wait for all threads to complete
    latch.await(60, TimeUnit.SECONDS);
    
    long endTime = System.currentTimeMillis();
    long totalRequests = numberOfThreads * numberOfRequestsPerThread;
    double requestsPerSecond = totalRequests / ((endTime - startTime) / 1000.0);
    
    System.out.printf("Performance Test Results:%n");
    System.out.printf("Total Requests: %d%n", totalRequests);
    System.out.printf("Total Time: %d ms%n", (endTime - startTime));
    System.out.printf("Requests per Second: %.2f%n", requestsPerSecond);
    
    // Assert performance requirements
    assertThat(requestsPerSecond).isGreaterThan(100); // Minimum 100 RPS
}
```

## 11. Deployment Configuration

### 11.1 Docker Configuration

```dockerfile
# Multi-stage Dockerfile
FROM eclipse-temurin:24-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:24-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -g 1001 -S takharrujy && \
    adduser -S takharrujy -u 1001 -G takharrujy

WORKDIR /app

# Copy application JAR
COPY --from=builder /app/target/takharrujy-*.jar app.jar

# Change ownership
RUN chown -R takharrujy:takharrujy /app
USER takharrujy

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Use virtual threads for better concurrency
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+UnlockExperimentalVMOptions", "-XX:+EnableJVMCIProduct", "-jar", "app.jar"]
```

### 11.2 Application Configuration

```yaml
# application.yml
spring:
  application:
    name: takharrujy-backend
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:development}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/takharrujy}
    username: ${DATABASE_USER:takharrujy}
    password: ${DATABASE_PASSWORD:password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC
    show-sql: false
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:https://auth.takharrujy.com}
          jwk-set-uri: ${JWT_JWK_SET_URI:https://auth.takharrujy.com/.well-known/jwks.json}

# Application-specific configuration
takharrujy:
  file-storage:
    provider: ${FILE_STORAGE_PROVIDER:azure}
    azure:
      connection-string: ${AZURE_STORAGE_CONNECTION_STRING}
      container-name: ${AZURE_CONTAINER_NAME:project-files}
    digitalocean:
      endpoint: ${DO_SPACES_ENDPOINT}
      access-key: ${DO_SPACES_ACCESS_KEY}
      secret-key: ${DO_SPACES_SECRET_KEY}
              bucket-name: ${DO_SPACES_BUCKET:takharujy-files}
  
  notification:
    email:
      provider: ${EMAIL_PROVIDER:sendgrid}
      sendgrid:
        api-key: ${SENDGRID_API_KEY}
      aws-ses:
        region: ${AWS_SES_REGION:us-east-1}
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
  
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,https://takharrujy.com}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    redis:
      enabled: true
    db:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.university.takharrujy: ${LOG_LEVEL:INFO}
    org.springframework.security: ${SECURITY_LOG_LEVEL:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 11.3 GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: takharrujy_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
          
      redis:
        image: redis:7-alpine
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 24
      uses: actions/setup-java@v4
      with:
        java-version: '24'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v4
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run tests
      run: ./mvnw clean test
      env:
        SPRING_PROFILES_ACTIVE: test
        DATABASE_URL: jdbc:postgresql://localhost:5432/takharrujy_test
        DATABASE_USER: postgres
        DATABASE_PASSWORD: postgres
        REDIS_HOST: localhost
        REDIS_PORT: 6379
  
  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Log in to Container Registry
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v5
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
  
  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Deploy to DigitalOcean App Platform
      uses: digitalocean/app_action@v1
      with:
        app_name: takharrujy-backend
        token: ${{ secrets.DIGITALOCEAN_ACCESS_TOKEN }}
        images: '[{
          "name": "takharrujy-backend",
          "image": {
            "registry_type": "GHCR",
            "repository": "${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}",
            "tag": "latest"
          }
        }]'
```

## 12. Security Considerations

### 12.1 Input Validation and Sanitization

```java
// Custom validation constraints
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeHtmlValidator.class)
public @interface SafeHtml {
    String message() default "HTML content contains potentially dangerous elements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {
    
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
        .and(Sanitizers.LINKS)
        .and(Sanitizers.BLOCKS);
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        
        String sanitized = POLICY.sanitize(value);
        return sanitized.equals(value);
    }
}

// Request DTOs with comprehensive validation
@Data
public class ProjectCreateRequest {
    
    @NotBlank(message = "Project title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\u0600-\\u06FF-]+$", 
             message = "Title contains invalid characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    @SafeHtml
    private String description;
    
    @NotNull(message = "Project type is required")
    private ProjectType projectType;
    
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\u0600-\\u06FF]+$", 
             message = "Category contains invalid characters")
    private String category;
    
    @Valid
    @Size(min = 1, max = 3, message = "Team must have 1-3 additional members")
    private List<@Email String> teamMemberEmails;
    
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
}
```

### 12.2 SQL Injection Prevention

```java
// Safe query construction with JPA Criteria API
@Repository
public class ProjectSearchRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Project> searchProjects(ProjectSearchCriteria criteria, String userEmail) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Project> query = cb.createQuery(Project.class);
        Root<Project> project = query.from(Project.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Add user access control
        Predicate accessPredicate = buildAccessPredicate(cb, project, userEmail);
        predicates.add(accessPredicate);
        
        // Add search filters safely
        if (StringUtils.hasText(criteria.getTitle())) {
            predicates.add(cb.like(
                cb.lower(project.get("title")), 
                "%" + criteria.getTitle().toLowerCase() + "%"
            ));
        }
        
        if (criteria.getProjectType() != null) {
            predicates.add(cb.equal(project.get("projectType"), criteria.getProjectType()));
        }
        
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(project.get("status"), criteria.getStatus()));
        }
        
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(project.get("updatedAt")));
        
        return entityManager.createQuery(query)
            .setMaxResults(50) // Prevent large result sets
            .getResultList();
    }
    
    private Predicate buildAccessPredicate(CriteriaBuilder cb, Root<Project> project, String userEmail) {
        // Users can only see projects they're members of or supervise
        Join<Project, ProjectMember> members = project.join("members", JoinType.LEFT);
        Join<Project, User> supervisor = project.join("supervisor", JoinType.LEFT);
        
        return cb.or(
            cb.equal(members.get("user").get("email"), userEmail),
            cb.equal(supervisor.get("email"), userEmail)
        );
    }
}
```

---

**Document Status:** Active Development  
**Next Review:** Week 2 of Sprint 1.5  
**Approval Required From:** Technical Lead, Product Owner, Security Reviewer  

**Implementation Priority:**
1. Database schema and migrations (Sprint 1, Week 1)
2. Core API endpoints with security (Sprint 1, Week 1-2)
3. File management and storage integration (Sprint 1, Week 2)
4. Real-time messaging infrastructure (Sprint 1.5)
5. Performance optimization and monitoring (Post-MVP)