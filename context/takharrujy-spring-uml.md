# Takharrujy Platform - Spring Application UML Architecture

**Version:** 1.0  
**Date:** December 2024  
**Project:** Takharrujy (تخرجي) - University Graduation Project Management Platform  
**Framework:** Spring Boot 3.4.x with Java 24  
**Architecture:** Modular Monolithic with Domain-Driven Design  
**Platform URL:** https://takharujy.tech  

## 1. Application Architecture Overview

The Takharrujy Spring Boot application follows a modular monolithic architecture with clear domain boundaries, implementing Domain-Driven Design (DDD) principles. The application is structured to support 500+ concurrent users with high performance and scalability.

### 1.1 Architectural Principles

- **Domain-Driven Design:** Clear domain boundaries with aggregate roots
- **Clean Architecture:** Dependency inversion and separation of concerns
- **CQRS Pattern:** Command-Query Responsibility Segregation for complex operations
- **Event-Driven:** Asynchronous processing for notifications and workflows
- **Security-First:** Role-based access control with JWT authentication
- **Multi-tenancy:** University-level data isolation with row-level security

## 2. High-Level System Architecture

```mermaid
graph TB
    subgraph "External Clients"
        WEB[Web Client<br/>React/Next.js<br/>🌐]
        MOBILE[Mobile App<br/>Flutter<br/>📱]
        API_CLIENT[API Client<br/>Third-party<br/>🔌]
    end
    
    subgraph "Load Balancer & Gateway"
        LB[DigitalOcean<br/>Load Balancer<br/>⚖️]
        GATEWAY[API Gateway<br/>Rate Limiting<br/>🚪]
    end
    
    subgraph "Spring Boot Application - Layered Architecture"
        subgraph "Presentation Layer"
            CONTROLLER[REST Controllers<br/>WebSocket Controllers<br/>Exception Handlers<br/>🎮]
        end
        
        subgraph "Application Service Layer"
            SERVICE[Business Services<br/>Authentication Service<br/>Validation Service<br/>🔧]
        end
        
        subgraph "Domain Layer"
            DOMAIN[Domain Models<br/>Domain Services<br/>Domain Events<br/>🏢]
        end
        
        subgraph "Infrastructure Layer"
            REPOSITORY[JPA Repositories<br/>External Integrations<br/>Caching Services<br/>🗄️]
        end
    end
    
    subgraph "External Services"
        AZURE[Azure Blob Storage<br/>File Storage & CDN<br/>☁️]
        BREVO[Brevo SMTP<br/>Email Service<br/>📧]
        REDIS[Redis Cache<br/>Session Store<br/>⚡]
        VIRUS[Virus Scanning<br/>Security Service<br/>🛡️]
    end
    
    subgraph "Data Layer"
        POSTGRES[(PostgreSQL 16<br/>Primary Database<br/>ACID Transactions<br/>🗃️)]
    end
    
    WEB --> LB
    MOBILE --> LB
    API_CLIENT --> LB
    
    LB --> GATEWAY
    GATEWAY --> CONTROLLER
    
    CONTROLLER --> SERVICE
    SERVICE --> DOMAIN
    SERVICE --> REPOSITORY
    
    SERVICE --> AZURE
    SERVICE --> BREVO
    SERVICE --> VIRUS
    REPOSITORY --> REDIS
    
    REPOSITORY --> POSTGRES
    
    %% Style the layers
    classDef presentationLayer fill:#e1f5fe
    classDef serviceLayer fill:#f3e5f5
    classDef domainLayer fill:#fff3e0
    classDef infraLayer fill:#e8f5e8
    
    class CONTROLLER presentationLayer
    class SERVICE serviceLayer
    class DOMAIN domainLayer
    class REPOSITORY infraLayer
```

## 3. Architecture-to-Implementation Mapping

### 3.1 Layer-to-Package Mapping

```mermaid
graph LR
    subgraph "Architectural Layers"
        PRES_LAYER[Presentation Layer]
        APP_LAYER[Application Service Layer]
        DOMAIN_LAYER[Domain Layer]
        INFRA_LAYER[Infrastructure Layer]
        CONFIG_LAYER[Configuration Layer]
    end
    
    subgraph "Java Package Structure"
        PRES_PKG[com.university.takharrujy.presentation<br/>• controller<br/>• dto<br/>• exception<br/>• websocket]
        APP_PKG[com.university.takharrujy.application<br/>• service<br/>• event<br/>• mapper]
        DOMAIN_PKG[com.university.takharrujy.domain<br/>• model<br/>• service<br/>• event<br/>• repository]
        INFRA_PKG[com.university.takharrujy.infrastructure<br/>• repository<br/>• external<br/>• cache<br/>• messaging]
        CONFIG_PKG[com.university.takharrujy.configuration<br/>• database<br/>• security<br/>• cache<br/>• integration]
    end
    
    PRES_LAYER --> PRES_PKG
    APP_LAYER --> APP_PKG
    DOMAIN_LAYER --> DOMAIN_PKG
    INFRA_LAYER --> INFRA_PKG
    CONFIG_LAYER --> CONFIG_PKG
```

### 3.2 Request Flow Through Layers

```mermaid
sequenceDiagram
    participant Client as 🌐 Client
    participant Controller as 🎮 Controller
    participant Service as 🔧 Service
    participant Domain as 🏢 Domain
    participant Repository as 🗄️ Repository
    participant Database as 🗃️ Database
    
    Note over Client,Database: Example: Create Project Request
    
    Client->>Controller: POST /api/v1/projects<br/>ProjectCreateRequest
    
    Note over Controller: Presentation Layer
    Controller->>Controller: Validate Request DTO
    Controller->>Controller: Extract User from JWT
    
    Controller->>Service: createProject(request, userEmail)
    
    Note over Service: Application Service Layer
    Service->>Service: Validate Business Rules
    Service->>Domain: new Project(title, description, type)
    
    Note over Domain: Domain Layer
    Domain->>Domain: Enforce Domain Rules
    Domain->>Domain: Calculate Initial State
    Domain-->>Service: Project Entity
    
    Service->>Repository: save(project)
    
    Note over Repository: Infrastructure Layer
    Repository->>Database: INSERT INTO projects...
    Database-->>Repository: Generated ID
    Repository-->>Service: Saved Project
    
    Service->>Service: Publish ProjectCreatedEvent
    Service->>Service: Map to Response DTO
    Service-->>Controller: ProjectResponse
    
    Controller-->>Client: 201 Created<br/>ProjectResponse
```

## 4. Detailed Application Layer Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        REST[REST Controllers]
        WS[WebSocket Controllers]
        EXCEPTION[Global Exception Handler]
        SECURITY[Security Configuration]
    end
    
    subgraph "Application Service Layer"
        USER_SERVICE[User Service]
        PROJECT_SERVICE[Project Service]
        TASK_SERVICE[Task Service]
        FILE_SERVICE[File Service]
        NOTIFICATION_SERVICE[Notification Service]
        MESSAGE_SERVICE[Message Service]
        AUTH_SERVICE[Authentication Service]
    end
    
    subgraph "Domain Layer"
        USER_DOMAIN[User Domain]
        PROJECT_DOMAIN[Project Domain]
        TASK_DOMAIN[Task Domain]
        FILE_DOMAIN[File Domain]
        NOTIFICATION_DOMAIN[Notification Domain]
    end
    
    subgraph "Infrastructure Layer"
        JPA_REPOS[JPA Repositories]
        FILE_STORAGE[File Storage Service]
        EMAIL_SERVICE[Email Service]
        CACHE_SERVICE[Cache Service]
        AUDIT_SERVICE[Audit Service]
    end
    
    subgraph "Configuration Layer"
        DB_CONFIG[Database Config]
        SECURITY_CONFIG[Security Config]
        CACHE_CONFIG[Cache Config]
        INTEGRATION_CONFIG[Integration Config]
    end
    
    REST --> USER_SERVICE
    REST --> PROJECT_SERVICE
    REST --> TASK_SERVICE
    REST --> FILE_SERVICE
    WS --> MESSAGE_SERVICE
    
    USER_SERVICE --> USER_DOMAIN
    PROJECT_SERVICE --> PROJECT_DOMAIN
    TASK_SERVICE --> TASK_DOMAIN
    FILE_SERVICE --> FILE_DOMAIN
    NOTIFICATION_SERVICE --> NOTIFICATION_DOMAIN
    
    USER_SERVICE --> JPA_REPOS
    PROJECT_SERVICE --> JPA_REPOS
    TASK_SERVICE --> JPA_REPOS
    FILE_SERVICE --> FILE_STORAGE
    NOTIFICATION_SERVICE --> EMAIL_SERVICE
    
    JPA_REPOS --> DB_CONFIG
    FILE_STORAGE --> INTEGRATION_CONFIG
    EMAIL_SERVICE --> INTEGRATION_CONFIG
    CACHE_SERVICE --> CACHE_CONFIG
```

## 4. Domain Model Class Diagrams

### 4.1 User Management Domain

```mermaid
classDiagram
    class User {
        -Long id
        -String email
        -String passwordHash
        -String firstName
        -String lastName
        -UserRole role
        -Long universityId
        -Long departmentId
        -String studentId
        -String phoneNumber
        -String preferredLanguage
        -String avatarUrl
        -boolean emailVerified
        -LocalDateTime lastLoginAt
        -Map~String,Object~ preferences
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean active
        
        +validateEmail() boolean
        +isStudent() boolean
        +isSupervisor() boolean
        +isAdmin() boolean
        +getFullName() String
        +updateLastLogin() void
    }
    
    class UserRole {
        <<enumeration>>
        STUDENT
        SUPERVISOR
        ADMIN
    }
    
    class University {
        -Long id
        -String name
        -String domain
        -String countryCode
        -String timezone
        -Map~String,Object~ settings
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean active
        
        +validateEmailDomain(String email) boolean
        +getSettings() Map
        +isActive() boolean
    }
    
    class Department {
        -Long id
        -Long universityId
        -String name
        -String code
        -String description
        -Map~String,Object~ metadata
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean active
        
        +getFullName() String
        +belongsToUniversity(Long universityId) boolean
    }
    
    User ||--|| UserRole : has
    User }|--|| University : belongs_to
    User }|--|| Department : belongs_to
    University ||--o{ Department : contains
```

### 4.2 Project Management Domain

```mermaid
classDiagram
    class Project {
        -Long id
        -String title
        -String description
        -ProjectType projectType
        -ProjectStatus status
        -String category
        -Long universityId
        -Long departmentId
        -Long supervisorId
        -Long teamLeaderId
        -LocalDate startDate
        -LocalDate dueDate
        -LocalDateTime submissionDate
        -BigDecimal progressPercentage
        -Map~String,Object~ metadata
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean active
        
        +calculateProgress() BigDecimal
        +isOverdue() boolean
        +canBeEditedBy(User user) boolean
        +addTeamMember(User user) void
        +removeTeamMember(User user) void
        +updateStatus(ProjectStatus status) void
    }
    
    class ProjectType {
        <<enumeration>>
        THESIS
        CAPSTONE
        RESEARCH
        DEVELOPMENT
    }
    
    class ProjectStatus {
        <<enumeration>>
        DRAFT
        ACTIVE
        SUBMITTED
        UNDER_REVIEW
        APPROVED
        REJECTED
        COMPLETED
        ARCHIVED
    }
    
    class ProjectMember {
        -Long id
        -Long projectId
        -Long userId
        -MemberRole role
        -InvitationStatus status
        -LocalDateTime joinedAt
        -LocalDateTime invitedAt
        -LocalDateTime respondedAt
        -String invitationMessage
        -boolean active
        
        +isTeamLeader() boolean
        +acceptInvitation() void
        +rejectInvitation() void
        +isActive() boolean
    }
    
    class MemberRole {
        <<enumeration>>
        LEADER
        MEMBER
    }
    
    class InvitationStatus {
        <<enumeration>>
        PENDING
        ACCEPTED
        REJECTED
        EXPIRED
    }
    
    Project ||--|| ProjectType : has
    Project ||--|| ProjectStatus : has
    Project ||--o{ ProjectMember : contains
    ProjectMember ||--|| MemberRole : has
    ProjectMember ||--|| InvitationStatus : has
    Project }|--|| User : supervised_by
    Project }|--|| User : led_by
    ProjectMember }|--|| User : represents
```

### 4.3 Task Management Domain

```mermaid
classDiagram
    class Task {
        -Long id
        -Long projectId
        -String title
        -String description
        -Long assignedTo
        -Long createdBy
        -TaskStatus status
        -TaskPriority priority
        -LocalDateTime dueDate
        -Integer estimatedHours
        -Integer actualHours
        -Long parentTaskId
        -Integer orderIndex
        -BigDecimal progressPercentage
        -Map~String,Object~ metadata
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -LocalDateTime completedAt
        -boolean active
        
        +isOverdue() boolean
        +canBeCompletedBy(User user) boolean
        +updateStatus(TaskStatus status, User user) void
        +addComment(String comment, User user) void
        +calculateProgress() BigDecimal
        +getSubtasks() List~Task~
    }
    
    class TaskStatus {
        <<enumeration>>
        TODO
        IN_PROGRESS
        IN_REVIEW
        COMPLETED
        BLOCKED
        CANCELLED
    }
    
    class TaskPriority {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
        URGENT
    }
    
    class TaskDependency {
        -Long id
        -Long taskId
        -Long dependsOnTaskId
        -DependencyType type
        -LocalDateTime createdAt
        -boolean active
        
        +isBlocking() boolean
        +canBeRemoved() boolean
    }
    
    class DependencyType {
        <<enumeration>>
        FINISH_TO_START
        START_TO_START
        FINISH_TO_FINISH
        START_TO_FINISH
    }
    
    class TaskComment {
        -Long id
        -Long taskId
        -Long userId
        -String comment
        -Long replyToId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean active
        
        +isReply() boolean
        +canBeEditedBy(User user) boolean
    }
    
    Task ||--|| TaskStatus : has
    Task ||--|| TaskPriority : has
    Task ||--o{ TaskDependency : has_dependencies
    Task ||--o{ TaskComment : has_comments
    TaskDependency ||--|| DependencyType : has
    Task }|--|| User : assigned_to
    Task }|--|| User : created_by
    Task }|--|| Project : belongs_to
    TaskComment }|--|| User : written_by
```

### 4.4 File Management Domain

```mermaid
classDiagram
    class File {
        -Long id
        -String filename
        -String originalFilename
        -String contentType
        -Long fileSize
        -String fileHash
        -String storagePath
        -String storageProvider
        -Long projectId
        -Long deliverableId
        -Long uploadedBy
        -Integer version
        -Long parentFileId
        -FileStatus status
        -Map~String,Object~ scanResults
        -Map~String,Object~ metadata
        -LocalDateTime createdAt
        -boolean active
        
        +isImage() boolean
        +isDocument() boolean
        +canBeAccessedBy(User user) boolean
        +createVersion(byte[] content) FileVersion
        +generateDownloadUrl() String
        +isVirusFree() boolean
    }
    
    class FileStatus {
        <<enumeration>>
        UPLOADING
        PROCESSING
        AVAILABLE
        QUARANTINED
        DELETED
    }
    
    class FileVersion {
        -Long id
        -Long fileId
        -Integer versionNumber
        -String storagePath
        -Long fileSize
        -String fileHash
        -Long uploadedBy
        -String versionNotes
        -LocalDateTime createdAt
        -boolean active
        
        +isLatest() boolean
        +getDifferences(FileVersion other) Map
    }
    
    class FileShare {
        -Long id
        -Long fileId
        -Long sharedBy
        -Long sharedWith
        -SharePermission permission
        -LocalDateTime expiresAt
        -String accessToken
        -LocalDateTime createdAt
        -boolean active
        
        +isExpired() boolean
        +generateAccessToken() String
        +hasPermission(SharePermission required) boolean
    }
    
    class SharePermission {
        <<enumeration>>
        VIEW
        DOWNLOAD
        EDIT
    }
    
    File ||--|| FileStatus : has
    File ||--o{ FileVersion : has_versions
    File ||--o{ FileShare : shared_as
    FileShare ||--|| SharePermission : has
    File }|--|| User : uploaded_by
    File }|--|| Project : belongs_to
    FileVersion }|--|| User : uploaded_by
    FileShare }|--|| User : shared_by
    FileShare }|--|| User : shared_with
```

## 5. Service Layer Architecture

### 5.1 Core Services Class Diagram

```mermaid
classDiagram
    class UserService {
        -UserRepository userRepository
        -UniversityService universityService
        -EmailService emailService
        -PasswordEncoder passwordEncoder
        -JwtTokenProvider tokenProvider
        
        +registerUser(UserRegistrationRequest) UserResponse
        +authenticateUser(LoginRequest) AuthenticationResponse
        +getUserProfile(String email) UserResponse
        +updateUserProfile(String email, UserUpdateRequest) UserResponse
        +resetPassword(String email) void
        +verifyEmail(String token) boolean
        +changePassword(String email, ChangePasswordRequest) void
    }
    
    class ProjectService {
        -ProjectRepository projectRepository
        -ProjectMemberRepository memberRepository
        -UserService userService
        -NotificationService notificationService
        -ProjectMapper projectMapper
        
        +createProject(ProjectCreateRequest, String userEmail) ProjectResponse
        +getProject(Long projectId, String userEmail) ProjectResponse
        +updateProject(Long projectId, ProjectUpdateRequest) ProjectResponse
        +deleteProject(Long projectId, String userEmail) void
        +addTeamMember(Long projectId, String memberEmail) void
        +removeTeamMember(Long projectId, Long memberId) void
        +assignSupervisor(Long projectId, Long supervisorId) void
        +getProjectStats(Long projectId) ProjectStatsResponse
    }
    
    class TaskService {
        -TaskRepository taskRepository
        -ProjectService projectService
        -NotificationService notificationService
        -TaskMapper taskMapper
        
        +createTask(Long projectId, TaskCreateRequest) TaskResponse
        +updateTask(Long taskId, TaskUpdateRequest) TaskResponse
        +updateTaskStatus(Long taskId, TaskStatus status) TaskResponse
        +assignTask(Long taskId, Long userId) TaskResponse
        +deleteTask(Long taskId, String userEmail) void
        +getProjectTasks(Long projectId) List~TaskResponse~
        +getUserTasks(String userEmail) List~TaskResponse~
        +addTaskComment(Long taskId, String comment) TaskCommentResponse
    }
    
    class FileService {
        -FileRepository fileRepository
        -FileStorageService storageService
        -VirusScanService virusScanService
        -ProjectService projectService
        -FileMapper fileMapper
        
        +uploadFile(Long projectId, MultipartFile file) FileResponse
        +downloadFile(Long fileId, String userEmail) ResponseEntity~Resource~
        +deleteFile(Long fileId, String userEmail) void
        +createFileVersion(Long fileId, MultipartFile file) FileVersionResponse
        +shareFile(Long fileId, FileShareRequest) FileShareResponse
        +getProjectFiles(Long projectId) List~FileResponse~
    }
    
    class NotificationService {
        -NotificationRepository notificationRepository
        -EmailService emailService
        -WebSocketService webSocketService
        -NotificationMapper notificationMapper
        
        +sendTaskAssignedNotification(Task task, User assignedBy) void
        +sendProjectUpdateNotification(Project project, String message) void
        +sendDeadlineReminder(Task task) void
        +sendFeedbackNotification(Deliverable deliverable) void
        +getUserNotifications(String userEmail) List~NotificationResponse~
        +markNotificationRead(Long notificationId) void
        +markAllNotificationsRead(String userEmail) void
    }
    
    UserService --> UserRepository
    ProjectService --> ProjectRepository
    TaskService --> TaskRepository
    FileService --> FileRepository
    NotificationService --> NotificationRepository
    
    ProjectService --> UserService
    TaskService --> ProjectService
    TaskService --> NotificationService
    FileService --> ProjectService
    NotificationService --> EmailService
```

### 5.2 Integration Services

```mermaid
classDiagram
    class FileStorageService {
        <<interface>>
        +uploadFile(MultipartFile file, Long projectId) String
        +downloadFile(String storagePath) Resource
        +deleteFile(String storagePath) void
        +generatePresignedUrl(String storagePath, Duration expiration) String
        +getProviderName() String
    }
    
    class AzureBlobStorageService {
        -BlobServiceClient blobServiceClient
        -String containerName
        
        +uploadFile(MultipartFile file, Long projectId) String
        +downloadFile(String storagePath) Resource
        +deleteFile(String storagePath) void
        +generatePresignedUrl(String storagePath, Duration expiration) String
        +getProviderName() String
    }
    
    class EmailService {
        <<interface>>
        +sendTaskAssignedEmail(User assignedTo, Task task, User assignedBy) void
        +sendProjectInvitationEmail(User invitee, Project project) void
        +sendPasswordResetEmail(User user, String resetToken) void
        +sendEmailVerificationEmail(User user, String verificationToken) void
    }
    
    class BrevoEmailService {
        -JavaMailSender mailSender
        -TemplateEngine templateEngine
        -String fromAddress
        
        +sendTaskAssignedEmail(User assignedTo, Task task, User assignedBy) void
        +sendProjectInvitationEmail(User invitee, Project project) void
        +sendPasswordResetEmail(User user, String resetToken) void
        +sendEmailVerificationEmail(User user, String verificationToken) void
    }
    
    class VirusScanService {
        <<interface>>
        +scanFile(MultipartFile file) ScanResult
        +isClean(ScanResult result) boolean
    }
    
    class ClamAVScanService {
        -String clamAVHost
        -Integer clamAVPort
        
        +scanFile(MultipartFile file) ScanResult
        +isClean(ScanResult result) boolean
    }
    
    class WebSocketService {
        -SimpMessagingTemplate messagingTemplate
        
        +sendMessageToProject(Long projectId, Object message) void
        +sendNotificationToUser(String userEmail, Object notification) void
        +broadcastSystemMessage(Object message) void
    }
    
    FileStorageService <|-- AzureBlobStorageService
    EmailService <|-- BrevoEmailService
    VirusScanService <|-- ClamAVScanService
```

## 6. Controller Layer Architecture

### 6.1 REST Controllers

```mermaid
classDiagram
    class UserController {
        -UserService userService
        
        +register(UserRegistrationRequest) ResponseEntity~UserResponse~
        +login(LoginRequest) ResponseEntity~AuthenticationResponse~
        +getProfile() ResponseEntity~UserResponse~
        +updateProfile(UserUpdateRequest) ResponseEntity~UserResponse~
        +changePassword(ChangePasswordRequest) ResponseEntity~Void~
        +resetPassword(PasswordResetRequest) ResponseEntity~Void~
        +verifyEmail(String token) ResponseEntity~Void~
    }
    
    class ProjectController {
        -ProjectService projectService
        
        +createProject(ProjectCreateRequest) ResponseEntity~ProjectResponse~
        +getProject(Long projectId) ResponseEntity~ProjectResponse~
        +updateProject(Long projectId, ProjectUpdateRequest) ResponseEntity~ProjectResponse~
        +deleteProject(Long projectId) ResponseEntity~Void~
        +getUserProjects() ResponseEntity~List~ProjectResponse~~
        +addTeamMember(Long projectId, TeamMemberRequest) ResponseEntity~Void~
        +removeTeamMember(Long projectId, Long memberId) ResponseEntity~Void~
        +getProjectStats(Long projectId) ResponseEntity~ProjectStatsResponse~
    }
    
    class TaskController {
        -TaskService taskService
        
        +createTask(Long projectId, TaskCreateRequest) ResponseEntity~TaskResponse~
        +getTask(Long taskId) ResponseEntity~TaskResponse~
        +updateTask(Long taskId, TaskUpdateRequest) ResponseEntity~TaskResponse~
        +updateTaskStatus(Long taskId, TaskStatusUpdateRequest) ResponseEntity~TaskResponse~
        +deleteTask(Long taskId) ResponseEntity~Void~
        +getProjectTasks(Long projectId) ResponseEntity~List~TaskResponse~~
        +getUserTasks() ResponseEntity~List~TaskResponse~~
        +addTaskComment(Long taskId, TaskCommentRequest) ResponseEntity~TaskCommentResponse~
    }
    
    class FileController {
        -FileService fileService
        
        +uploadFile(Long projectId, MultipartFile file) ResponseEntity~FileResponse~
        +downloadFile(Long fileId) ResponseEntity~Resource~
        +deleteFile(Long fileId) ResponseEntity~Void~
        +getProjectFiles(Long projectId) ResponseEntity~List~FileResponse~~
        +createFileVersion(Long fileId, MultipartFile file) ResponseEntity~FileVersionResponse~
        +shareFile(Long fileId, FileShareRequest) ResponseEntity~FileShareResponse~
        +getFileVersions(Long fileId) ResponseEntity~List~FileVersionResponse~~
    }
    
    class NotificationController {
        -NotificationService notificationService
        
        +getUserNotifications() ResponseEntity~List~NotificationResponse~~
        +markNotificationRead(Long notificationId) ResponseEntity~Void~
        +markAllNotificationsRead() ResponseEntity~Void~
        +getNotificationPreferences() ResponseEntity~NotificationPreferencesResponse~
        +updateNotificationPreferences(NotificationPreferencesRequest) ResponseEntity~Void~
    }
    
    UserController --> UserService
    ProjectController --> ProjectService
    TaskController --> TaskService
    FileController --> FileService
    NotificationController --> NotificationService
```

### 6.2 WebSocket Controllers

```mermaid
classDiagram
    class MessageController {
        -MessageService messageService
        -WebSocketService webSocketService
        
        +sendMessage(Long projectId, MessageRequest) void
        +editMessage(Long messageId, MessageEditRequest) void
        +deleteMessage(Long messageId) void
        +reactToMessage(Long messageId, ReactionRequest) void
        +getProjectMessages(Long projectId) List~MessageResponse~
        +searchMessages(Long projectId, String query) List~MessageResponse~
    }
    
    class NotificationWebSocketController {
        -NotificationService notificationService
        -WebSocketService webSocketService
        
        +subscribeToNotifications() void
        +unsubscribeFromNotifications() void
        +markNotificationRead(Long notificationId) void
    }
    
    MessageController --> MessageService
    MessageController --> WebSocketService
    NotificationWebSocketController --> NotificationService
    NotificationWebSocketController --> WebSocketService
```

## 7. Configuration and Security Architecture

### 7.1 Security Configuration

```mermaid
classDiagram
    class SecurityConfiguration {
        -JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
        -JwtRequestFilter jwtRequestFilter
        
        +filterChain(HttpSecurity http) SecurityFilterChain
        +authenticationManager(AuthenticationConfiguration config) AuthenticationManager
        +passwordEncoder() PasswordEncoder
        +corsConfigurationSource() CorsConfigurationSource
    }
    
    class JwtTokenProvider {
        -String jwtSecret
        -Integer jwtExpirationInMs
        -RedisTemplate redisTemplate
        
        +generateToken(UserPrincipal userPrincipal) String
        +getUserIdFromToken(String token) Long
        +getUserEmailFromToken(String token) String
        +validateToken(String token) boolean
        +invalidateToken(String token) void
        +getExpirationDateFromToken(String token) Date
    }
    
    class JwtRequestFilter {
        -UserDetailsService userDetailsService
        -JwtTokenProvider tokenProvider
        
        +doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) void
        +getJwtFromRequest(HttpServletRequest request) String
    }
    
    class UserPrincipal {
        -Long id
        -String email
        -String password
        -Collection~GrantedAuthority~ authorities
        -Long universityId
        -UserRole role
        
        +create(User user) UserPrincipal
        +getAuthorities() Collection~GrantedAuthority~
        +isAccountNonExpired() boolean
        +isAccountNonLocked() boolean
        +isCredentialsNonExpired() boolean
        +isEnabled() boolean
    }
    
    SecurityConfiguration --> JwtRequestFilter
    JwtRequestFilter --> JwtTokenProvider
    JwtRequestFilter --> UserPrincipal
    JwtTokenProvider --> UserPrincipal
```

### 7.2 Application Configuration

```mermaid
classDiagram
    class DatabaseConfiguration {
        -DataSource dataSource
        -EntityManagerFactory entityManagerFactory
        
        +dataSource() DataSource
        +entityManagerFactory() LocalContainerEntityManagerFactoryBean
        +transactionManager() PlatformTransactionManager
        +hikariConfig() HikariConfig
    }
    
    class CacheConfiguration {
        -RedisConnectionFactory connectionFactory
        
        +cacheManager() RedisCacheManager
        +redisTemplate() RedisTemplate
        +stringRedisTemplate() StringRedisTemplate
        +cacheConfigurations() Map~String,RedisCacheConfiguration~
    }
    
    class WebSocketConfiguration {
        +configureMessageBroker(MessageBrokerRegistry registry) void
        +registerStompEndpoints(StompEndpointRegistry registry) void
        +configureClientInboundChannel(ChannelRegistration registration) void
        +configureClientOutboundChannel(ChannelRegistration registration) void
    }
    
    class AsyncConfiguration {
        +taskExecutor() Executor
        +applicationEventMulticaster() ApplicationEventMulticaster
        +asyncUncaughtExceptionHandler() AsyncUncaughtExceptionHandler
    }
    
    class IntegrationConfiguration {
        -AzureStorageProperties azureProperties
        -BrevoEmailProperties brevoProperties
        
        +blobServiceClient() BlobServiceClient
        +javaMailSender() JavaMailSender
        +thymeleafTemplateEngine() TemplateEngine
    }
```

## 8. Exception Handling and Validation

### 8.1 Exception Handling Architecture

```mermaid
classDiagram
    class GlobalExceptionHandler {
        +handleValidationException(MethodArgumentNotValidException ex) ResponseEntity~ErrorResponse~
        +handleResourceNotFoundException(ResourceNotFoundException ex) ResponseEntity~ErrorResponse~
        +handleAccessDeniedException(AccessDeniedException ex) ResponseEntity~ErrorResponse~
        +handleDuplicateResourceException(DuplicateResourceException ex) ResponseEntity~ErrorResponse~
        +handleFileStorageException(FileStorageException ex) ResponseEntity~ErrorResponse~
        +handleEmailDeliveryException(EmailDeliveryException ex) ResponseEntity~ErrorResponse~
        +handleGenericException(Exception ex) ResponseEntity~ErrorResponse~
    }
    
    class ErrorResponse {
        -String error
        -String message
        -Integer status
        -String path
        -String timestamp
        -Map~String,String~ validationErrors
        
        +builder() ErrorResponseBuilder
        +addValidationError(String field, String message) void
    }
    
    class ResourceNotFoundException {
        -String resourceName
        -String fieldName
        -Object fieldValue
        
        +ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue)
        +getMessage() String
    }
    
    class DuplicateResourceException {
        -String resourceName
        -String fieldName
        -Object fieldValue
        
        +DuplicateResourceException(String resourceName, String fieldName, Object fieldValue)
        +getMessage() String
    }
    
    GlobalExceptionHandler --> ErrorResponse
    GlobalExceptionHandler --> ResourceNotFoundException
    GlobalExceptionHandler --> DuplicateResourceException
```

### 8.2 Validation Architecture

```mermaid
classDiagram
    class UserRegistrationRequest {
        -@Email @NotBlank String email
        -@Size(min=8,max=64) String password
        -@NotBlank @Size(max=100) String firstName
        -@NotBlank @Size(max=100) String lastName
        -@NotNull UserRole role
        -@NotBlank String universityDomain
        -String studentId
        -String department
        -String phoneNumber
        -String preferredLanguage
        
        +validate() boolean
    }
    
    class ProjectCreateRequest {
        -@NotBlank @Size(max=255) String title
        -@NotBlank @Size(max=2000) String description
        -@NotNull ProjectType projectType
        -String category
        -Long preferredSupervisorId
        -LocalDate startDate
        -LocalDate dueDate
        -@Valid List~TeamMemberInvitation~ teamMembers
        
        +validate() boolean
    }
    
    class TaskCreateRequest {
        -@NotBlank @Size(max=255) String title
        -@Size(max=2000) String description
        -Long assignedTo
        -@NotNull TaskPriority priority
        -LocalDateTime dueDate
        -Integer estimatedHours
        -Long parentTaskId
        
        +validate() boolean
    }
    
    class UniversityEmailValidator {
        -UniversityService universityService
        
        +isValid(String email, ConstraintValidatorContext context) boolean
        +initialize(UniversityEmail constraintAnnotation) void
    }
    
    class SafeHtmlValidator {
        -PolicyFactory policy
        
        +isValid(String value, ConstraintValidatorContext context) boolean
        +initialize(SafeHtml constraintAnnotation) void
    }
    
    UniversityEmailValidator --> UniversityService
    UserRegistrationRequest --> UniversityEmailValidator
    ProjectCreateRequest --> SafeHtmlValidator
```

## 9. Event-Driven Architecture

### 9.1 Application Events

```mermaid
classDiagram
    class ApplicationEventPublisher {
        <<interface>>
        +publishEvent(Object event) void
    }
    
    class ProjectCreatedEvent {
        -Project project
        -User createdBy
        -LocalDateTime timestamp
        
        +getProject() Project
        +getCreatedBy() User
        +getTimestamp() LocalDateTime
    }
    
    class TaskAssignedEvent {
        -Task task
        -User assignedTo
        -User assignedBy
        -LocalDateTime timestamp
        
        +getTask() Task
        +getAssignedTo() User
        +getAssignedBy() User
    }
    
    class FileUploadedEvent {
        -File file
        -User uploadedBy
        -LocalDateTime timestamp
        
        +getFile() File
        +getUploadedBy() User
    }
    
    class ProjectEventListener {
        -NotificationService notificationService
        -ActivityService activityService
        
        +handleProjectCreated(ProjectCreatedEvent event) void
        +handleProjectUpdated(ProjectUpdatedEvent event) void
        +handleProjectCompleted(ProjectCompletedEvent event) void
    }
    
    class TaskEventListener {
        -NotificationService notificationService
        -ActivityService activityService
        
        +handleTaskAssigned(TaskAssignedEvent event) void
        +handleTaskCompleted(TaskCompletedEvent event) void
        +handleTaskOverdue(TaskOverdueEvent event) void
    }
    
    ApplicationEventPublisher --> ProjectCreatedEvent
    ApplicationEventPublisher --> TaskAssignedEvent
    ApplicationEventPublisher --> FileUploadedEvent
    
    ProjectEventListener --> NotificationService
    TaskEventListener --> NotificationService
```

## 10. Performance and Monitoring

### 10.1 Caching Architecture

```mermaid
classDiagram
    class CacheService {
        -RedisTemplate redisTemplate
        -CacheManager cacheManager
        
        +get(String key) Object
        +put(String key, Object value, Duration ttl) void
        +evict(String key) void
        +evictAll(String pattern) void
        +exists(String key) boolean
    }
    
    class ProjectCacheService {
        -CacheService cacheService
        -String CACHE_PREFIX
        
        +cacheProject(Project project) void
        +getProjectFromCache(Long projectId) Project
        +evictProject(Long projectId) void
        +cacheProjectStats(Long projectId, ProjectStats stats) void
    }
    
    class UserCacheService {
        -CacheService cacheService
        -String CACHE_PREFIX
        
        +cacheUser(User user) void
        +getUserFromCache(String email) User
        +evictUser(String email) void
        +cacheUserSessions(String email, List~UserSession~ sessions) void
    }
    
    ProjectCacheService --> CacheService
    UserCacheService --> CacheService
```

### 10.2 Metrics and Monitoring

```mermaid
classDiagram
    class MetricsConfiguration {
        -MeterRegistry meterRegistry
        
        +meterRegistry() MeterRegistry
        +timedAspect() TimedAspect
        +customMetrics() void
    }
    
    class HealthIndicator {
        <<interface>>
        +health() Health
    }
    
    class DatabaseHealthIndicator {
        -DataSource dataSource
        
        +health() Health
        +checkDatabaseConnection() boolean
    }
    
    class FileStorageHealthIndicator {
        -FileStorageService fileStorageService
        
        +health() Health
        +checkStorageAvailability() boolean
    }
    
    class EmailServiceHealthIndicator {
        -EmailService emailService
        
        +health() Health
        +checkEmailServiceConnectivity() boolean
    }
    
    HealthIndicator <|-- DatabaseHealthIndicator
    HealthIndicator <|-- FileStorageHealthIndicator
    HealthIndicator <|-- EmailServiceHealthIndicator
    
    MetricsConfiguration --> MeterRegistry
```

## 11. Testing Architecture

### 11.1 Test Structure

```mermaid
classDiagram
    class BaseIntegrationTest {
        <<abstract>>
        -TestContainers containers
        -TestRestTemplate restTemplate
        -TestDataBuilder testDataBuilder
        
        +setUp() void
        +tearDown() void
        +createTestUser() User
        +createTestProject() Project
        +authenticateUser(User user) String
    }
    
    class UserServiceTest {
        -UserService userService
        -UserRepository userRepository
        -EmailService emailService
        
        +testUserRegistration() void
        +testUserAuthentication() void
        +testPasswordReset() void
        +testEmailVerification() void
    }
    
    class ProjectControllerIntegrationTest {
        -TestRestTemplate restTemplate
        -ProjectService projectService
        
        +testCreateProject() void
        +testGetProject() void
        +testUpdateProject() void
        +testDeleteProject() void
        +testAddTeamMember() void
    }
    
    class SecurityTest {
        -MockMvc mockMvc
        -JwtTokenProvider tokenProvider
        
        +testUnauthorizedAccess() void
        +testRoleBasedAccess() void
        +testTokenValidation() void
        +testCorsConfiguration() void
    }
    
    BaseIntegrationTest <|-- ProjectControllerIntegrationTest
    UserServiceTest --> UserService
    ProjectControllerIntegrationTest --> ProjectService
    SecurityTest --> JwtTokenProvider
```

## 12. Deployment and DevOps Architecture

### 12.1 Container Architecture

```mermaid
classDiagram
    class DockerConfiguration {
        +dockerfile() String
        +buildImage() void
        +configureHealthCheck() void
        +optimizeImageSize() void
    }
    
    class KubernetesConfiguration {
        +deploymentYaml() String
        +serviceYaml() String
        +configMapYaml() String
        +secretYaml() String
        +ingressYaml() String
    }
    
    class GitHubActionsWorkflow {
        +buildAndTest() void
        +securityScan() void
        +deployToStaging() void
        +deployToProduction() void
        +rollback() void
    }
    
    DockerConfiguration --> KubernetesConfiguration
    KubernetesConfiguration --> GitHubActionsWorkflow
```

## 13. Complete Documentation Suite Integration

### 13.1 Three-Tier Documentation Approach

This UML document is part of a comprehensive three-tier documentation suite:

```mermaid
graph TB
    subgraph "📋 Documentation Hierarchy"
        LAYERED[1️⃣ Layered Architecture<br/>takharrujy-layered-architecture.md<br/>• System structure and layers<br/>• Component organization<br/>• Request flow patterns<br/>• Cross-cutting concerns]
        
        UML[2️⃣ UML Class Diagrams<br/>takharrujy-spring-uml.md<br/>• Detailed class relationships<br/>• Method signatures<br/>• Domain models<br/>• Service implementations]
        
        ERD[3️⃣ Database ERD<br/>takharrujy-erd.md<br/>• Entity relationships<br/>• Database schema<br/>• Performance indexes<br/>• Multi-tenancy design]
    end
    
    subgraph "🎯 Usage Guidelines"
        ARCH_DESIGN[Architecture Design<br/>Use: Layered Architecture<br/>For: System structure decisions]
        
        IMPLEMENTATION[Implementation<br/>Use: UML Class Diagrams<br/>For: Coding and development]
        
        DATA_DESIGN[Data Design<br/>Use: Database ERD<br/>For: Database implementation]
    end
    
    LAYERED --> ARCH_DESIGN
    UML --> IMPLEMENTATION
    ERD --> DATA_DESIGN
    
    ARCH_DESIGN -.-> UML
    UML -.-> ERD
    ERD -.-> LAYERED
```

### 13.2 Implementation Roadmap

**Phase 1: Foundation (Sprint 1)**
1. **Start with ERD:** Create database schema and entities
2. **Implement Domain Layer:** Core business entities and rules
3. **Build Infrastructure:** Repositories and external integrations
4. **Create Services:** Application service layer implementation

**Phase 2: API Layer (Sprint 1.5)**
1. **Implement Controllers:** REST and WebSocket endpoints
2. **Add Security:** JWT authentication and authorization
3. **Configure Integration:** External services (Azure, Brevo)
4. **Setup Monitoring:** Health checks and metrics

**Phase 3: Enhancement (Post-MVP)**
1. **Add Advanced Features:** Real-time messaging, file versioning
2. **Implement AI Integration:** Academic assistant capabilities
3. **Scale Architecture:** Microservices migration patterns
4. **Optimize Performance:** Caching and query optimization

### 13.3 Development Team Guidelines

**For Backend Developers:**
- **Primary Reference:** UML Class Diagrams (this document)
- **Secondary Reference:** Layered Architecture for structure
- **Database Reference:** ERD for data relationships

**For System Architects:**
- **Primary Reference:** Layered Architecture document
- **Secondary Reference:** UML for implementation details
- **Integration Reference:** ERD for data flow design

**For Database Designers:**
- **Primary Reference:** Database ERD document
- **Secondary Reference:** UML for business logic understanding
- **Performance Reference:** Layered Architecture for query patterns

### 13.4 Quality Assurance Integration

```mermaid
graph LR
    subgraph "Testing Strategy"
        UNIT[Unit Tests<br/>Test individual classes<br/>from UML diagrams]
        
        INTEGRATION[Integration Tests<br/>Test layer interactions<br/>from Layered Architecture]
        
        DATA[Data Tests<br/>Test database operations<br/>from ERD specifications]
    end
    
    subgraph "Documentation Validation"
        UML_DOC[UML Class Diagrams] --> UNIT
        LAYERED_DOC[Layered Architecture] --> INTEGRATION  
        ERD_DOC[Database ERD] --> DATA
    end
    
    UNIT --> CODE_QUALITY[Code Quality Metrics]
    INTEGRATION --> SYSTEM_QUALITY[System Quality Metrics]
    DATA --> DATA_QUALITY[Data Quality Metrics]
```

### 13.5 Maintenance and Evolution

**Document Synchronization:**
- All three documents must be updated together when architecture changes
- Version numbers should be synchronized across all documents
- Change logs should reference impacts across all three perspectives

**Evolution Path:**
1. **Monolith → Modular Monolith** (Current state)
2. **Modular Monolith → Microservices** (Future state)
3. **Microservices → Event-Driven Architecture** (Long-term vision)

Each evolution step requires updates to all three documentation layers.

---

**UML Architecture Document Status:** Active Development  
**Complements:** Layered Architecture & Database ERD  
**Next Review:** End of Sprint 1  
**Framework Version:** Spring Boot 3.4.x  
**Java Version:** 24 with Virtual Threads  
**Architecture Version:** 1.0  
**Last Updated:** December 2024

This comprehensive UML architecture provides the complete structural foundation for the Takharrujy Spring Boot application, working in harmony with the Layered Architecture and Database ERD documents to support all MVP requirements while establishing scalable patterns for future AI integration, microservices evolution, and multi-university deployment.
