# 📋 Project Configuration - Takharrujy Platform

## 🚨 CRITICAL RULES
- **Java 24 with Virtual Threads**: Use Project Loom features (Executors.newVirtualThreadPerTaskExecutor())
- **Spring Boot 3.4.x with Spring 6.2.x**: Latest enterprise features and patterns
- **Modular Monolithic Architecture**: Domain-driven design with clear module boundaries
- **PostgreSQL 16.x**: Advanced features, row-level security, proper indexing
- **Arabic Language Support**: RTL UI, Arabic content validation, bilingual capabilities
- **Security First**: JWT authentication, RBAC, input validation, SQL injection prevention

### ✅ Memory System Successfully Integrated
**STATUS:** Memory system is now fully integrated and available for team collaboration.

**Benefits Enabled:**
- ✅ Shared knowledge base between developers
- ✅ Consistent coding patterns and architectural decisions  
- ✅ Collaborative development automation
- ✅ Team progress tracking and context preservation

**What's Available:**
- Complete memory system with 31,856+ lines of content
- 16 architectural decisions with full context
- 23 coding patterns across 7 categories
- Real-time task tracking and sprint progress
- Automated backup and optimization systems

## 🎯 PROJECT CONTEXT

### Project Overview
**Takharrujy (تخرجي)** - University Graduation Project Management Platform
- **Type**: Enterprise web application for academic project management
- **Domain**: Educational technology (EdTech) for higher education
- **Scale**: Multi-tenant SaaS platform supporting multiple universities
- **Timeline**: 3 weeks development (2 weeks Sprint 1 + 1 week Sprint 1.5)
- **Team**: 2 backend developers (small team coordination pattern)

### Technology Stack
- **Backend**: Java 24, Spring Boot 3.4.x, Spring Security 6.x, Spring Data JPA
- **Database**: PostgreSQL 16.x with advanced features (row-level security, JSON columns)
- **Caching**: Redis 7.x for session management and performance optimization
- **File Storage**: Azure Blob Storage / DigitalOcean Spaces (configurable)
- **Email**: Brevo SMTP / SendGrid / AWS SES (configurable providers)
- **SMS**: Azure Communication Services for notifications
- **Build Tool**: Maven with Spring Boot parent
- **Deployment**: Docker containers on DigitalOcean with Blue-Green deployment

### Architecture Decisions
- **Modular Monolithic**: Single deployable unit with clear domain boundaries
- **Package Structure**: `com.university.takharrujy.{presentation,application,domain,infrastructure}`
- **Multi-Tenancy**: Row-level security with university_id filtering
- **API Design**: RESTful with comprehensive OpenAPI documentation
- **Security**: JWT with role-based access control (Admin, Supervisor, Student)
- **Internationalization**: Arabic (primary) and English support with proper RTL handling

## 🔧 DEVELOPMENT PATTERNS

### Coding Standards
- **Java Style**: Google Java Style Guide with 4-space indentation
- **Naming**: Descriptive names, Arabic comments for business logic
- **Methods**: Keep under 50 lines, single responsibility principle
- **Classes**: Domain-driven design with clear boundaries
- **Packages**: Layered architecture with dependency inversion
- **Error Handling**: Custom exceptions with proper HTTP status codes

### Git Workflow Standards
- **Enhanced Feature Branch Workflow** with main/develop branches
- **Feature-per-Endpoint Development** for API-focused development
- **Conventional Commits** with Arabic language considerations
- **Branch Protection Rules** requiring code reviews and CI/CD checks
- **Automated Quality Gates** with testing, security, and performance validation

### File Organization
```
src/main/java/com/university/takharrujy/
├── presentation/          # Controllers, DTOs, validation
├── application/          # Services, use cases, orchestration
├── domain/              # Entities, value objects, repositories
└── infrastructure/      # External integrations, configurations
```

### Database Patterns
- **Entities**: JPA with proper relationships and constraints
- **Migrations**: Flyway for version-controlled schema changes
- **Indexing**: Strategic indexes for performance optimization
- **Security**: Row-level security for multi-tenancy
- **Enums**: Custom PostgreSQL enums for type safety

### Testing Strategies
- **Unit Tests**: JUnit 5 with Mockito for service layer
- **Integration Tests**: @SpringBootTest with TestContainers
- **API Tests**: RestAssured for endpoint testing
- **Performance Tests**: K6 for load testing
- **Security Tests**: OWASP ZAP integration
- **Postman Testing**: Dedicated collections for each endpoint with automated tests

### Postman Organization Requirements
- **Folder Structure**: Create `postman/{endpoint-name}/` for each API endpoint
- **Environment Files**: Separate environments for dev, staging, prod in each folder
- **Collection Structure**: Organized by controller with comprehensive test scenarios
- **Test Scripts**: Pre-request scripts and automated validation tests
- **Documentation**: Request/response examples with Arabic language support

## 🐝 SWARM ORCHESTRATION

### Agent Coordination Patterns
- **Small Team Pattern**: 2-5 agents for efficient coordination
- **Backend Focus**: Specialized Java/Spring agents for enterprise development
- **Quality Emphasis**: Testing and security agents for reliability
- **Documentation**: API documentation and Arabic language support

### Task Distribution Strategies
- **Developer 1 (Senior)**: Authentication, security, project management, file handling
- **Developer 2 (Mid-Level)**: Task management, notifications, messaging, admin features
- **Parallel Development**: Independent modules with clear interfaces
- **Integration Points**: Shared DTOs, common services, unified error handling

### Parallel Execution Rules
- **Concurrent Operations**: Always batch related operations in single messages
- **TodoWrite**: Batch 5-10+ todos in one call with priorities and dependencies
- **Task Spawning**: Deploy multiple agents simultaneously with full context
- **File Operations**: Read/Write/Edit multiple files in parallel
- **Database Operations**: Batch related queries and migrations

## 🧠 MEMORY MANAGEMENT

### Integrated Memory System
- **Memory Integration**: Import comprehensive project knowledge and context
- **Knowledge Base**: Domain expertise, technical decisions, and patterns
- **Decision Tracking**: Historical record of architectural and technical choices
- **Task Memory**: Persistent sprint progress and development continuity
- **Session Context**: Conversation history and development progress tracking

### Memory Imports
@.claude/context/knowledge.md
@.claude/context/patterns.md  
@.claude/context/decisions.md
@.claude/context/tasks.md
@context/github-best-practices.md
@context/gitignore-impact-analysis.md

### Context Storage Patterns
- **Project Memory**: Store architectural decisions and patterns in structured JSON format
- **Sprint Context**: Track current sprint progress and blockers with real-time updates
- **Technical Debt**: Document known issues and improvement opportunities
- **Integration Points**: Remember external service configurations and API contracts
- **Security Decisions**: Track authentication and authorization implementation choices

### Decision Tracking
- **Structured Decisions**: JSON-based decision log with rationale and impact assessment
- **Database Schema**: Store entity relationships, constraints, and migration history
- **API Contracts**: Remember endpoint specifications, DTOs, and validation rules
- **Business Rules**: Track validation rules, workflows, and compliance requirements
- **Performance Optimizations**: Document caching strategies and query optimizations

### Knowledge Persistence
- **Domain Expertise**: University management, Arabic language, academic workflows
- **Technical Patterns**: Spring Boot, PostgreSQL, Redis implementation patterns
- **Error Solutions**: Comprehensive troubleshooting guide with common issues and fixes
- **Testing Strategies**: Unit, integration, and Postman testing patterns
- **Security Implementation**: JWT, RBAC, validation, and compliance patterns

### Memory Commands
- **View Memory**: Use `/memory` to see loaded memory files and current context
- **Add Memory**: Use `/memory add` to store new information in the knowledge base
- **Search Memory**: Use `/memory search` to find relevant patterns and decisions
- **Update Context**: Memory automatically updates with each development session

## 🚀 DEPLOYMENT & CI/CD

### Build Processes
- **Maven Goals**: `clean compile test package` with Spring Boot plugin
- **Docker Images**: Multi-stage builds with JDK 24 base images
- **Environment Configs**: Profile-based configuration (dev, staging, prod)
- **Database Migrations**: Flyway automatic migration on startup
- **Health Checks**: Spring Actuator endpoints for monitoring

### Testing Pipelines
- **Unit Tests**: Run with every commit, require 80%+ coverage
- **Integration Tests**: TestContainers with PostgreSQL and Redis
- **API Tests**: RestAssured with comprehensive endpoint coverage
- **Security Tests**: OWASP ZAP automated scanning
- **Performance Tests**: K6 load testing for critical endpoints

### Deployment Strategies
- **Blue-Green Deployment**: Zero-downtime deployments on DigitalOcean
- **Database Migrations**: Forward-compatible schema changes
- **Configuration Management**: Environment variables for secrets
- **Monitoring**: Application metrics and health monitoring
- **Rollback Strategy**: Automated rollback on health check failures

## 📊 MONITORING & ANALYTICS

### Performance Tracking
- **Application Metrics**: Spring Actuator with Micrometer
- **Database Performance**: Query execution time and optimization
- **Cache Hit Rates**: Redis performance monitoring
- **API Response Times**: Endpoint performance tracking
- **Virtual Thread Utilization**: Project Loom performance metrics

### Error Monitoring
- **Exception Tracking**: Structured logging with correlation IDs
- **Security Events**: Authentication failures and suspicious activity
- **Database Errors**: Connection issues and query failures
- **External Service Failures**: File storage and email service issues
- **User Experience**: Frontend error reporting and analytics

### User Analytics
- **Usage Patterns**: Feature adoption and user behavior
- **Performance Impact**: Real user monitoring and Core Web Vitals
- **Security Metrics**: Failed login attempts and access patterns
- **Content Analytics**: Arabic vs English usage patterns
- **Mobile Usage**: Flutter app usage statistics

## 🔒 SECURITY & COMPLIANCE

### Security Practices
- **Authentication**: JWT with secure token generation and validation
- **Authorization**: Role-based access control with method-level security
- **Input Validation**: Bean Validation with custom Arabic text validators
- **SQL Injection Prevention**: Parameterized queries and JPA criteria
- **XSS Protection**: Content Security Policy and output encoding
- **CSRF Protection**: Spring Security CSRF tokens for state-changing operations

### Compliance Requirements
- **FERPA Compliance**: Educational record privacy and security
- **Data Protection**: Secure handling of student and academic data
- **Audit Logging**: Complete audit trail for compliance reporting
- **Access Controls**: Proper segregation of duties and permissions
- **Data Retention**: Configurable retention policies for academic records

### Access Controls
- **Multi-Tenancy**: University-level data isolation with row-level security
- **Role Hierarchy**: Admin > Supervisor > Student with proper inheritance
- **Resource-Based Authorization**: Project-level and task-level permissions
- **API Security**: Rate limiting and API key management
- **Session Management**: Secure session handling with Redis storage

## 🎯 SPECIAL CONSIDERATIONS

### Arabic Language Support
- **RTL Layout**: Proper right-to-left text handling in UI components
- **Text Validation**: Arabic character validation and normalization
- **Date/Time**: Hijri calendar support alongside Gregorian
- **Collation**: Proper Arabic text sorting and searching
- **Fonts**: Arabic font rendering and typography considerations

### University Domain Expertise
- **Academic Workflows**: Understanding of graduation project processes
- **Role Hierarchies**: Academic supervisor-student relationships
- **Project Lifecycle**: Proposal, development, review, and defense phases
- **Collaboration Patterns**: Team-based project development
- **Assessment Criteria**: Academic evaluation and grading systems

### Performance Optimization
- **Virtual Threads**: Leverage Project Loom for high concurrency
- **Database Optimization**: Strategic indexing and query optimization
- **Caching Strategy**: Redis for session data and frequently accessed content
- **File Handling**: Efficient large file uploads with chunking
- **API Optimization**: Response compression and pagination

### Integration Patterns
- **External Services**: Resilient integration with file storage and email services
- **Webhook Support**: Real-time notifications and event-driven architecture
- **API Versioning**: Backward-compatible API evolution
- **Mobile API**: Optimized endpoints for Flutter mobile application
- **Third-Party Auth**: OAuth2 integration for university systems

---

## 🎮 QUICK REFERENCE

### Essential Commands
```bash
# Development
./mvnw spring-boot:run -Dspring.profiles.active=dev
./mvnw test
./mvnw clean package -DskipTests

# Database
./mvnw flyway:migrate
./mvnw flyway:info
./mvnw flyway:repair

# Docker
docker-compose up -d postgres redis
docker build -t takharrujy-backend .
docker run -p 8080:8080 takharrujy-backend
```

### Key URLs
- **API Base**: https://api.takharujy.tech/v1
- **Documentation**: https://api.takharujy.tech/swagger-ui.html
- **Health Check**: https://api.takharujy.tech/actuator/health
- **Metrics**: https://api.takharujy.tech/actuator/metrics

### Configuration Priorities
1. **Security**: JWT, RBAC, input validation, SQL injection prevention
2. **Performance**: Virtual threads, caching, database optimization
3. **Scalability**: Multi-tenancy, horizontal scaling, load balancing
4. **Maintainability**: Clean architecture, comprehensive testing, documentation
5. **Compliance**: FERPA, data protection, audit logging, access controls

Remember: This is a **small team (2-5)** project focused on **Java/Spring** enterprise development with **agile/scrum** methodology, using a **monolithic** architecture for **API development**. Prioritize code quality, security, and Arabic language support throughout development.
