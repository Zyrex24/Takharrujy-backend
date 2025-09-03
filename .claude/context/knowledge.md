# Takharrujy Platform Knowledge Base

## Domain Expertise

### University Management System
**Core Concepts:**
- **Graduation Project Lifecycle:** Proposal → Development → Review → Defense → Completion
- **Supervisor-Student Relationships:** 1 supervisor can manage up to 12 projects, each project has 1 primary supervisor
- **Academic Evaluation Processes:** Deliverable reviews, progress assessments, final defense scoring
- **Multi-tenant University System:** Each university operates as an isolated tenant with separate data
- **FERPA Compliance:** Educational record privacy and security requirements

**Business Entities:**
- **Universities:** Top-level tenant entities with domains and departments
- **Users:** Students, Supervisors, and Admins with role-based permissions
- **Projects:** Graduation projects with teams, timelines, and deliverables
- **Tasks:** Project work items with assignments, dependencies, and status tracking
- **Deliverables:** Formal submissions requiring supervisor review and approval
- **Files:** Documents, presentations, and media with versioning and sharing
- **Notifications:** System alerts, reminders, and communication messages
- **Messages:** Real-time project communication and collaboration

**Business Rules:**
- **Multi-tenancy:** All data scoped by university_id with row-level security
- **Team Size Limits:** Maximum 4 students per project team
- **Supervisor Workload:** Maximum 12 active projects per supervisor
- **University Email Validation:** Users must register with valid university domain email
- **File Size Limits:** 100MB maximum per file, 1GB total per project
- **Project Duration:** Typically 2 semesters (8-10 months) from proposal to defense

### Arabic Language Requirements
**Primary Language Support:**
- **RTL (Right-to-Left) Layout:** All UI components support Arabic text direction
- **Arabic Character Validation:** Proper handling of Arabic Unicode ranges (U+0600-U+06FF)
- **Bilingual Interface:** Arabic primary, English secondary with language switching
- **Arabic Date/Time:** Support for both Gregorian and Hijri calendar systems
- **Text Search:** Arabic-aware search with diacritics normalization

**Implementation Requirements:**
- **UTF-8 Encoding:** Throughout database, API, and frontend systems
- **Arabic Text Fields:** Proper validation for names, descriptions, and content
- **Database Collation:** Arabic collation for proper text sorting and comparison
- **i18n Message Bundles:** Localized messages for Arabic and English
- **Font Support:** Arabic typography with proper character rendering

### Academic Workflow Patterns
**Project Proposal Phase:**
1. Student creates project proposal with title, description, and objectives
2. System suggests supervisors based on expertise and workload
3. Supervisor reviews and approves/rejects proposal with feedback
4. Approved projects move to development phase

**Development Phase:**
1. Project team is formed with invited members
2. Tasks are created and assigned to team members
3. Regular progress updates and milestone deliverables
4. Supervisor monitors progress and provides guidance

**Review and Defense Phase:**
1. Final deliverables submitted for supervisor review
2. Supervisor provides feedback and approval
3. Defense presentation scheduled and conducted
4. Final grades and project completion recorded

## Technical Knowledge

### Architecture Decisions
**Modular Monolithic Design:**
- **Rationale:** Single deployable unit with clear domain boundaries for small team efficiency
- **Package Structure:** `com.university.takharrujy.{presentation,application,domain,infrastructure}`
- **Domain Boundaries:** Clear separation between User, Project, Task, File, Notification, and Message domains
- **Integration:** Shared DTOs and services with minimal coupling between modules

**Database Design with Row-Level Security:**
- **Multi-tenancy:** University-scoped data access with PostgreSQL RLS policies
- **Security:** All queries automatically filtered by university_id
- **Performance:** Strategic indexing on university_id and frequently queried fields
- **Compliance:** FERPA-compliant data isolation and access controls

**Caching Strategy with Redis:**
- **Session Storage:** JWT tokens and user session data
- **Metadata Caching:** Project, task, and user metadata for quick access
- **Query Result Caching:** Frequently accessed data with TTL-based expiration
- **Real-time Data:** WebSocket connection management and message queuing

### Technology Stack Expertise
**Java 24 with Project Loom:**
- **Virtual Threads:** High-concurrency handling with `Executors.newVirtualThreadPerTaskExecutor()`
- **Pattern Matching:** Enhanced switch expressions and instanceof patterns
- **Records:** Immutable data classes for DTOs and value objects
- **Text Blocks:** Multi-line string literals for SQL queries and JSON templates

**Spring Boot 3.4.x with Spring 6.2.x:**
- **Native Compilation:** GraalVM native image support for faster startup
- **Observability:** Built-in metrics and tracing with Micrometer
- **Security:** Spring Security 6.x with JWT and OAuth2 support
- **Data Access:** Spring Data JPA with custom repositories and specifications

**PostgreSQL 16.x Advanced Features:**
- **Row-Level Security:** Automatic data filtering based on user context
- **JSON Columns:** Flexible metadata storage with JSON queries
- **Full-Text Search:** Arabic-aware search with custom configurations
- **Advanced Indexing:** GIN, GiST, and partial indexes for performance

**Redis 7.x for Caching:**
- **Data Structures:** Strings, hashes, lists, sets, and sorted sets
- **Pub/Sub:** Real-time messaging and event notifications
- **Lua Scripts:** Atomic operations and complex data manipulations
- **Clustering:** High availability and horizontal scaling support

### External Service Integration
**File Storage Services:**
- **Azure Blob Storage:** Primary storage with CDN integration
- **DigitalOcean Spaces:** Alternative storage with S3-compatible API
- **Virus Scanning:** Integrated malware detection for all uploads
- **File Processing:** Image thumbnails, document previews, and format conversion

**Email Services:**
- **Brevo SMTP:** Primary email service with transactional templates
- **SendGrid:** Alternative email provider with advanced analytics
- **AWS SES:** Backup email service with high deliverability
- **Template Engine:** Localized email templates for Arabic and English

**SMS and Communication:**
- **Azure Communication Services:** SMS notifications for critical alerts
- **WhatsApp Business API:** Optional messaging integration (future)
- **Push Notifications:** Mobile app notifications via Firebase
- **WebSocket:** Real-time messaging and live updates

### Security Implementation
**Authentication and Authorization:**
- **JWT Tokens:** Stateless authentication with refresh token rotation
- **Role-Based Access Control:** Student, Supervisor, Admin roles with hierarchical permissions
- **Multi-factor Authentication:** Optional 2FA via SMS or authenticator apps
- **Session Management:** Redis-based session storage with automatic expiration

**Data Protection:**
- **Encryption at Rest:** Database and file storage encryption
- **Encryption in Transit:** TLS 1.3 for all API communications
- **Input Validation:** Comprehensive validation with custom Arabic text validators
- **SQL Injection Prevention:** Parameterized queries and JPA criteria API

**Compliance and Auditing:**
- **FERPA Compliance:** Educational record privacy and access controls
- **Audit Logging:** Complete audit trail for all data access and modifications
- **Data Retention:** Configurable retention policies for academic records
- **Privacy Controls:** User data export and deletion capabilities

## Performance Optimization

### Database Performance
**Query Optimization:**
- **Strategic Indexing:** Composite indexes on frequently queried columns
- **Query Analysis:** Regular EXPLAIN ANALYZE for performance monitoring
- **Connection Pooling:** HikariCP with optimized pool sizing
- **Read Replicas:** Read-only replicas for reporting and analytics

**Caching Strategies:**
- **Query Result Caching:** Redis caching for expensive queries
- **Application-Level Caching:** In-memory caching for static data
- **CDN Integration:** Static asset caching and global distribution
- **Cache Invalidation:** Event-driven cache updates and TTL management

### Application Performance
**Virtual Thread Optimization:**
- **Blocking I/O:** Virtual threads for database and external service calls
- **Concurrent Processing:** Parallel task execution for bulk operations
- **Resource Management:** Efficient memory usage with virtual thread pools
- **Performance Monitoring:** Thread pool metrics and performance tracking

**API Performance:**
- **Response Compression:** GZIP compression for large responses
- **Pagination:** Efficient pagination with cursor-based navigation
- **Async Processing:** Non-blocking operations for long-running tasks
- **Rate Limiting:** API throttling to prevent abuse and ensure stability

## Troubleshooting and Common Issues

### Database Issues
**Connection Problems:**
- **Symptoms:** Connection timeouts, pool exhaustion
- **Solutions:** Increase pool size, optimize long-running queries, add connection monitoring
- **Prevention:** Regular connection pool health checks and monitoring

**Performance Issues:**
- **Symptoms:** Slow query responses, high CPU usage
- **Solutions:** Add missing indexes, optimize query plans, implement caching
- **Prevention:** Regular performance audits and query analysis

### Authentication Issues
**Token Problems:**
- **Symptoms:** Authentication failures, token expiration errors
- **Solutions:** Implement token refresh, check token validation logic
- **Prevention:** Proper token lifecycle management and monitoring

**Permission Errors:**
- **Symptoms:** Access denied, insufficient privileges
- **Solutions:** Review role assignments, check permission configurations
- **Prevention:** Comprehensive authorization testing and role validation

### File Upload Issues
**Size Limit Errors:**
- **Symptoms:** File upload failures, 413 Payload Too Large
- **Solutions:** Increase server limits, implement chunked uploads
- **Prevention:** Client-side file size validation and progress indicators

**Virus Detection:**
- **Symptoms:** File upload rejected, security warnings
- **Solutions:** Review file content, implement file type restrictions
- **Prevention:** User education and clear file upload guidelines

### Arabic Language Issues
**Text Rendering:**
- **Symptoms:** Garbled Arabic text, incorrect character display
- **Solutions:** Check UTF-8 encoding, validate font support
- **Prevention:** Comprehensive Arabic text testing across all components

**RTL Layout:**
- **Symptoms:** Incorrect text direction, misaligned UI elements
- **Solutions:** Apply RTL CSS classes, test with Arabic content
- **Prevention:** RTL-aware UI component design and testing

---

**Knowledge Base Status:** ✅ Comprehensive  
**Last Updated:** December 2024  
**Coverage:** Domain expertise, technical knowledge, troubleshooting  
**Maintenance:** Updated with each major project milestone  

This knowledge base serves as the authoritative source of domain and technical expertise for the Takharrujy platform, ensuring consistent understanding and implementation across the development team.
