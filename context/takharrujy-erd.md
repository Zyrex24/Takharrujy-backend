# Takharrujy Platform - Entity Relationship Diagram (ERD)

**Version:** 1.0  
**Date:** December 2024  
**Project:** Takharrujy (تخرجي) - University Graduation Project Management Platform  
**Database:** PostgreSQL 16.x  
**Platform URL:** https://takharujy.tech  

## 1. Database Overview

The Takharrujy platform uses PostgreSQL as the primary database with a multi-tenant architecture supporting university-level data isolation. The schema is designed to handle academic workflows, project collaboration, and file management with proper audit trails and compliance requirements.

### 1.1 Key Design Principles

- **Multi-tenancy:** Row-level security with university-based data isolation
- **Audit Trail:** Complete tracking of all changes for academic compliance
- **Internationalization:** Full UTF-8 support for Arabic and English content
- **Performance:** Optimized indexes for common query patterns
- **Security:** Role-based access control with data encryption
- **Scalability:** Designed to handle 500+ concurrent users

## 2. Entity Relationship Diagram

```mermaid
erDiagram
    universities ||--o{ users : "belongs_to"
    universities ||--o{ projects : "hosts"
    universities ||--o{ departments : "contains"
    
    users ||--o{ projects : "supervises"
    users ||--o{ projects : "leads"
    users ||--o{ project_members : "participates"
    users ||--o{ tasks : "assigned_to"
    users ||--o{ tasks : "created_by"
    users ||--o{ files : "uploaded_by"
    users ||--o{ messages : "sends"
    users ||--o{ notifications : "receives"
    users ||--o{ deliverables : "submitted_by"
    users ||--o{ feedback : "provided_by"
    
    projects ||--o{ project_members : "has"
    projects ||--o{ tasks : "contains"
    projects ||--o{ files : "stores"
    projects ||--o{ messages : "discussion"
    projects ||--o{ deliverables : "produces"
    projects ||--o{ project_activities : "tracks"
    
    tasks ||--o{ task_dependencies : "depends_on"
    tasks ||--o{ task_activities : "logs"
    tasks ||--o{ task_comments : "has"
    
    files ||--o{ file_versions : "versioned"
    files ||--o{ file_shares : "shared"
    
    deliverables ||--o{ feedback : "receives"
    deliverables ||--o{ deliverable_files : "includes"
    
    messages ||--o{ message_attachments : "contains"
    messages ||--o{ message_reactions : "has"
    
    universities {
        BIGSERIAL id PK
        VARCHAR(255) name UK
        VARCHAR(100) domain UK
        VARCHAR(3) country_code
        VARCHAR(50) timezone
        JSONB settings
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    departments {
        BIGSERIAL id PK
        BIGINT university_id FK
        VARCHAR(255) name
        VARCHAR(100) code UK
        VARCHAR(500) description
        JSONB metadata
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    users {
        BIGSERIAL id PK
        VARCHAR(255) email UK
        VARCHAR(255) password_hash
        VARCHAR(100) first_name
        VARCHAR(100) last_name
        user_role role
        BIGINT university_id FK
        BIGINT department_id FK
        VARCHAR(50) student_id
        VARCHAR(20) phone_number
        VARCHAR(10) preferred_language
        VARCHAR(500) avatar_url
        BOOLEAN email_verified
        TIMESTAMP last_login_at
        JSONB preferences
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    projects {
        BIGSERIAL id PK
        VARCHAR(255) title UK
        TEXT description
        project_type project_type
        project_status status
        VARCHAR(100) category
        BIGINT university_id FK
        BIGINT department_id FK
        BIGINT supervisor_id FK
        BIGINT team_leader_id FK
        DATE start_date
        DATE due_date
        TIMESTAMP submission_date
        DECIMAL(5,2) progress_percentage
        JSONB metadata
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    project_members {
        BIGSERIAL id PK
        BIGINT project_id FK
        BIGINT user_id FK
        member_role role
        invitation_status status
        TIMESTAMP joined_at
        TIMESTAMP invited_at
        TIMESTAMP responded_at
        VARCHAR(500) invitation_message
        BOOLEAN active
    }
    
    tasks {
        BIGSERIAL id PK
        BIGINT project_id FK
        VARCHAR(255) title
        TEXT description
        BIGINT assigned_to FK
        BIGINT created_by FK
        task_status status
        task_priority priority
        TIMESTAMP due_date
        INTEGER estimated_hours
        INTEGER actual_hours
        BIGINT parent_task_id FK
        INTEGER order_index
        DECIMAL(5,2) progress_percentage
        JSONB metadata
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP completed_at
        BOOLEAN active
    }
    
    task_dependencies {
        BIGSERIAL id PK
        BIGINT task_id FK
        BIGINT depends_on_task_id FK
        dependency_type type
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    task_activities {
        BIGSERIAL id PK
        BIGINT task_id FK
        BIGINT user_id FK
        activity_type type
        TEXT description
        JSONB old_values
        JSONB new_values
        TIMESTAMP created_at
    }
    
    task_comments {
        BIGSERIAL id PK
        BIGINT task_id FK
        BIGINT user_id FK
        TEXT comment
        BIGINT reply_to_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    files {
        BIGSERIAL id PK
        VARCHAR(255) filename
        VARCHAR(255) original_filename
        VARCHAR(100) content_type
        BIGINT file_size
        VARCHAR(64) file_hash UK
        VARCHAR(500) storage_path
        VARCHAR(50) storage_provider
        BIGINT project_id FK
        BIGINT deliverable_id FK
        BIGINT uploaded_by FK
        INTEGER version
        BIGINT parent_file_id FK
        file_status status
        JSONB scan_results
        JSONB metadata
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    file_versions {
        BIGSERIAL id PK
        BIGINT file_id FK
        INTEGER version_number
        VARCHAR(500) storage_path
        BIGINT file_size
        VARCHAR(64) file_hash
        BIGINT uploaded_by FK
        TEXT version_notes
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    file_shares {
        BIGSERIAL id PK
        BIGINT file_id FK
        BIGINT shared_by FK
        BIGINT shared_with FK
        share_permission permission
        TIMESTAMP expires_at
        VARCHAR(255) access_token
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    deliverables {
        BIGSERIAL id PK
        BIGINT project_id FK
        VARCHAR(255) title
        deliverable_type type
        TEXT description
        TIMESTAMP due_date
        TIMESTAMP submitted_at
        BIGINT submitted_by FK
        deliverable_status status
        TEXT feedback
        DECIMAL(5,2) grade
        INTEGER max_grade
        JSONB rubric_scores
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    deliverable_files {
        BIGSERIAL id PK
        BIGINT deliverable_id FK
        BIGINT file_id FK
        INTEGER order_index
        TEXT description
        TIMESTAMP attached_at
        BOOLEAN active
    }
    
    feedback {
        BIGSERIAL id PK
        BIGINT deliverable_id FK
        BIGINT provided_by FK
        feedback_type type
        TEXT content
        DECIMAL(5,2) score
        INTEGER max_score
        feedback_status status
        BIGINT reply_to_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }
    
    messages {
        BIGSERIAL id PK
        BIGINT project_id FK
        BIGINT sender_id FK
        TEXT content
        message_type type
        BIGINT reply_to_id FK
        BIGINT thread_id FK
        BOOLEAN is_edited
        TIMESTAMP edited_at
        JSONB metadata
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    message_attachments {
        BIGSERIAL id PK
        BIGINT message_id FK
        BIGINT file_id FK
        VARCHAR(255) display_name
        TIMESTAMP attached_at
        BOOLEAN active
    }
    
    message_reactions {
        BIGSERIAL id PK
        BIGINT message_id FK
        BIGINT user_id FK
        VARCHAR(50) reaction_type
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    notifications {
        BIGSERIAL id PK
        BIGINT user_id FK
        VARCHAR(255) title
        TEXT message
        notification_type type
        notification_priority priority
        VARCHAR(50) related_entity_type
        BIGINT related_entity_id
        TIMESTAMP read_at
        JSONB sent_via
        JSONB metadata
        TIMESTAMP created_at
        BOOLEAN active
    }
    
    project_activities {
        BIGSERIAL id PK
        BIGINT project_id FK
        BIGINT user_id FK
        activity_type type
        TEXT description
        VARCHAR(50) entity_type
        BIGINT entity_id
        JSONB metadata
        TIMESTAMP created_at
    }
    
    user_sessions {
        BIGSERIAL id PK
        BIGINT user_id FK
        VARCHAR(255) session_token UK
        VARCHAR(45) ip_address
        VARCHAR(500) user_agent
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP last_accessed_at
        BOOLEAN active
    }
    
    audit_logs {
        BIGSERIAL id PK
        BIGINT user_id FK
        VARCHAR(50) table_name
        BIGINT record_id
        audit_action action
        JSONB old_values
        JSONB new_values
        VARCHAR(45) ip_address
        VARCHAR(500) user_agent
        TIMESTAMP created_at
    }
```

## 3. Database Schema Details

### 3.1 Core Entities

#### 3.1.1 Universities Table
```sql
CREATE TABLE universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    domain VARCHAR(100) UNIQUE NOT NULL,
    country_code VARCHAR(3),
    timezone VARCHAR(50) DEFAULT 'UTC',
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_universities_domain ON universities(domain);
CREATE INDEX idx_universities_country ON universities(country_code);
```

#### 3.1.2 Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL,
    university_id BIGINT REFERENCES universities(id),
    department_id BIGINT REFERENCES departments(id),
    student_id VARCHAR(50),
    phone_number VARCHAR(20),
    preferred_language VARCHAR(10) DEFAULT 'en',
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_university ON users(university_id);
CREATE INDEX idx_users_student_id ON users(student_id);
```

#### 3.1.3 Projects Table
```sql
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    project_type project_type NOT NULL,
    status project_status DEFAULT 'DRAFT',
    category VARCHAR(100),
    university_id BIGINT REFERENCES universities(id),
    department_id BIGINT REFERENCES departments(id),
    supervisor_id BIGINT REFERENCES users(id),
    team_leader_id BIGINT REFERENCES users(id),
    start_date DATE,
    due_date DATE,
    submission_date TIMESTAMP,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    active BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT unique_project_title_per_university 
        UNIQUE(title, university_id)
);

CREATE INDEX idx_projects_supervisor ON projects(supervisor_id);
CREATE INDEX idx_projects_status_university ON projects(status, university_id);
CREATE INDEX idx_projects_due_date ON projects(due_date);
CREATE INDEX idx_projects_team_leader ON projects(team_leader_id);
```

### 3.2 Custom Types and Enums

```sql
-- User roles
CREATE TYPE user_role AS ENUM ('STUDENT', 'SUPERVISOR', 'ADMIN');

-- Project types and statuses
CREATE TYPE project_type AS ENUM ('THESIS', 'CAPSTONE', 'RESEARCH', 'DEVELOPMENT');
CREATE TYPE project_status AS ENUM ('DRAFT', 'ACTIVE', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'COMPLETED', 'ARCHIVED');

-- Member roles and invitation statuses
CREATE TYPE member_role AS ENUM ('LEADER', 'MEMBER');
CREATE TYPE invitation_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED');

-- Task management
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED', 'BLOCKED', 'CANCELLED');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE dependency_type AS ENUM ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH');

-- File management
CREATE TYPE file_status AS ENUM ('UPLOADING', 'PROCESSING', 'AVAILABLE', 'QUARANTINED', 'DELETED');
CREATE TYPE share_permission AS ENUM ('VIEW', 'DOWNLOAD', 'EDIT');

-- Deliverables and feedback
CREATE TYPE deliverable_type AS ENUM ('PROPOSAL', 'PROGRESS_REPORT', 'FINAL_REPORT', 'PRESENTATION', 'CODE', 'DOCUMENTATION', 'OTHER');
CREATE TYPE deliverable_status AS ENUM ('PENDING', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REVISION_REQUIRED', 'REJECTED');
CREATE TYPE feedback_type AS ENUM ('GENERAL', 'TECHNICAL', 'ACADEMIC', 'FORMATTING');
CREATE TYPE feedback_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');

-- Communication
CREATE TYPE message_type AS ENUM ('TEXT', 'FILE_SHARE', 'SYSTEM_NOTIFICATION', 'ANNOUNCEMENT');

-- Notifications
CREATE TYPE notification_type AS ENUM ('TASK_ASSIGNED', 'TASK_COMPLETED', 'DEADLINE_REMINDER', 'SUBMISSION_RECEIVED', 'FEEDBACK_AVAILABLE', 'PROJECT_UPDATE', 'INVITATION_RECEIVED', 'MESSAGE_RECEIVED');
CREATE TYPE notification_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Activity tracking
CREATE TYPE activity_type AS ENUM ('PROJECT_CREATED', 'PROJECT_UPDATED', 'TASK_CREATED', 'TASK_UPDATED', 'TASK_COMPLETED', 'FILE_UPLOADED', 'FILE_DOWNLOADED', 'MESSAGE_SENT', 'DELIVERABLE_SUBMITTED', 'FEEDBACK_PROVIDED');

-- Audit logging
CREATE TYPE audit_action AS ENUM ('INSERT', 'UPDATE', 'DELETE', 'SELECT');
```

### 3.3 Performance Optimization Indexes

```sql
-- Core business logic indexes
CREATE INDEX idx_project_members_project_user ON project_members(project_id, user_id);
CREATE INDEX idx_project_members_user_status ON project_members(user_id, status);

CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_tasks_due_date ON tasks(due_date) WHERE due_date IS NOT NULL;
CREATE INDEX idx_tasks_parent ON tasks(parent_task_id) WHERE parent_task_id IS NOT NULL;

CREATE INDEX idx_files_project_deliverable ON files(project_id, deliverable_id);
CREATE INDEX idx_files_hash ON files(file_hash);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_type_priority ON notifications(type, priority);

CREATE INDEX idx_messages_project_created ON messages(project_id, created_at DESC);
CREATE INDEX idx_messages_thread ON messages(thread_id) WHERE thread_id IS NOT NULL;

-- Full-text search indexes
CREATE INDEX idx_projects_search ON projects USING gin(to_tsvector('english', title || ' ' || COALESCE(description, '')));
CREATE INDEX idx_tasks_search ON tasks USING gin(to_tsvector('english', title || ' ' || COALESCE(description, '')));
CREATE INDEX idx_users_search ON users USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || email));

-- Composite indexes for common queries
CREATE INDEX idx_projects_supervisor_status ON projects(supervisor_id, status);
CREATE INDEX idx_projects_university_status ON projects(university_id, status);
CREATE INDEX idx_tasks_assignee_status ON tasks(assigned_to, status);
CREATE INDEX idx_files_project_active ON files(project_id, active);
```

### 3.4 Row-Level Security (Multi-tenancy)

```sql
-- Enable RLS on core tables
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE files ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- University-level isolation policy
CREATE POLICY university_isolation_projects ON projects
    FOR ALL TO authenticated_users
    USING (university_id = current_setting('app.current_university_id')::BIGINT);

-- Project member access policy
CREATE POLICY project_member_access ON project_members
    FOR ALL TO authenticated_users
    USING (
        project_id IN (
            SELECT id FROM projects 
            WHERE university_id = current_setting('app.current_university_id')::BIGINT
        )
    );

-- Task access based on project membership
CREATE POLICY task_project_access ON tasks
    FOR ALL TO authenticated_users
    USING (
        project_id IN (
            SELECT pm.project_id FROM project_members pm
            WHERE pm.user_id = current_setting('app.current_user_id')::BIGINT
            AND pm.status = 'ACCEPTED'
        ) OR
        project_id IN (
            SELECT p.id FROM projects p
            WHERE p.supervisor_id = current_setting('app.current_user_id')::BIGINT
        )
    );
```

### 3.5 Audit Trail and Logging

```sql
-- Audit trigger function
CREATE OR REPLACE FUNCTION audit_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO audit_logs (user_id, table_name, record_id, action, old_values, ip_address, user_agent)
        VALUES (
            current_setting('app.current_user_id', true)::BIGINT,
            TG_TABLE_NAME,
            OLD.id,
            'DELETE',
            row_to_json(OLD),
            current_setting('app.client_ip', true),
            current_setting('app.user_agent', true)
        );
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_logs (user_id, table_name, record_id, action, old_values, new_values, ip_address, user_agent)
        VALUES (
            current_setting('app.current_user_id', true)::BIGINT,
            TG_TABLE_NAME,
            NEW.id,
            'UPDATE',
            row_to_json(OLD),
            row_to_json(NEW),
            current_setting('app.client_ip', true),
            current_setting('app.user_agent', true)
        );
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO audit_logs (user_id, table_name, record_id, action, new_values, ip_address, user_agent)
        VALUES (
            current_setting('app.current_user_id', true)::BIGINT,
            TG_TABLE_NAME,
            NEW.id,
            'INSERT',
            row_to_json(NEW),
            current_setting('app.client_ip', true),
            current_setting('app.user_agent', true)
        );
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Apply audit triggers to critical tables
CREATE TRIGGER audit_users AFTER INSERT OR UPDATE OR DELETE ON users FOR EACH ROW EXECUTE FUNCTION audit_trigger();
CREATE TRIGGER audit_projects AFTER INSERT OR UPDATE OR DELETE ON projects FOR EACH ROW EXECUTE FUNCTION audit_trigger();
CREATE TRIGGER audit_files AFTER INSERT OR UPDATE OR DELETE ON files FOR EACH ROW EXECUTE FUNCTION audit_trigger();
CREATE TRIGGER audit_deliverables AFTER INSERT OR UPDATE OR DELETE ON deliverables FOR EACH ROW EXECUTE FUNCTION audit_trigger();
```

## 4. Data Relationships and Constraints

### 4.1 Primary Relationships

1. **University → Users:** One-to-many relationship where each user belongs to exactly one university
2. **University → Projects:** One-to-many relationship with university-level project isolation
3. **User → Projects:** Multiple relationships (supervisor, team leader, team member)
4. **Project → Tasks:** One-to-many hierarchical relationship with parent-child task support
5. **Project → Files:** One-to-many with version control and access permissions
6. **Project → Messages:** One-to-many for project-based communication
7. **User → Notifications:** One-to-many for all notification types

### 4.2 Business Rules and Constraints

```sql
-- Business rule constraints
ALTER TABLE projects ADD CONSTRAINT check_project_dates 
    CHECK (due_date IS NULL OR start_date IS NULL OR due_date >= start_date);

ALTER TABLE tasks ADD CONSTRAINT check_task_hours 
    CHECK (estimated_hours IS NULL OR estimated_hours > 0);

ALTER TABLE tasks ADD CONSTRAINT check_actual_hours 
    CHECK (actual_hours IS NULL OR actual_hours >= 0);

ALTER TABLE files ADD CONSTRAINT check_file_size 
    CHECK (file_size > 0 AND file_size <= 104857600); -- 100MB limit

ALTER TABLE deliverables ADD CONSTRAINT check_grade_range 
    CHECK (grade IS NULL OR (grade >= 0 AND grade <= max_grade));

ALTER TABLE feedback ADD CONSTRAINT check_score_range 
    CHECK (score IS NULL OR (score >= 0 AND score <= max_score));

-- Prevent self-referential relationships
ALTER TABLE tasks ADD CONSTRAINT check_no_self_parent 
    CHECK (parent_task_id != id);

ALTER TABLE messages ADD CONSTRAINT check_no_self_reply 
    CHECK (reply_to_id != id);
```

### 4.3 Data Integrity Functions

```sql
-- Function to update project progress based on task completion
CREATE OR REPLACE FUNCTION update_project_progress()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE projects 
    SET progress_percentage = (
        SELECT COALESCE(
            ROUND(
                (COUNT(*) FILTER (WHERE status = 'COMPLETED')::DECIMAL / COUNT(*)) * 100, 
                2
            ), 
            0
        )
        FROM tasks 
        WHERE project_id = COALESCE(NEW.project_id, OLD.project_id)
        AND active = TRUE
    )
    WHERE id = COALESCE(NEW.project_id, OLD.project_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update project progress
CREATE TRIGGER update_project_progress_trigger
    AFTER INSERT OR UPDATE OR DELETE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_project_progress();
```

## 5. Migration Strategy

### 5.1 Database Migrations with Flyway

```sql
-- V001__Create_base_schema.sql
-- Initial schema creation with core tables

-- V002__Add_indexes.sql  
-- Performance optimization indexes

-- V003__Add_rls_policies.sql
-- Row-level security implementation

-- V004__Add_audit_system.sql
-- Audit logging and triggers

-- V005__Add_business_constraints.sql
-- Business rule constraints and validation
```

### 5.2 Seed Data

```sql
-- Default universities for development
INSERT INTO universities (name, domain, country_code, timezone) VALUES
('Cairo University', 'cu.edu.eg', 'EGY', 'Africa/Cairo'),
('American University of Cairo', 'aucegypt.edu', 'EGY', 'Africa/Cairo'),
('Jordan University of Science and Technology', 'just.edu.jo', 'JOR', 'Asia/Amman');

-- Default departments
INSERT INTO departments (university_id, name, code) VALUES
(1, 'Computer Science', 'CS'),
(1, 'Engineering', 'ENG'),
(2, 'Computer Science', 'CS'),
(3, 'Information Technology', 'IT');
```

## 6. Performance Considerations

### 6.1 Query Optimization

- **Partitioning:** Consider partitioning audit_logs by date for long-term performance
- **Connection Pooling:** HikariCP with optimized pool settings
- **Query Caching:** Redis integration for frequently accessed data
- **Read Replicas:** For reporting and analytics queries

### 6.2 Monitoring and Maintenance

```sql
-- Query performance monitoring
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Regular maintenance procedures
CREATE OR REPLACE FUNCTION maintenance_cleanup()
RETURNS void AS $$
BEGIN
    -- Clean up old audit logs (older than 2 years)
    DELETE FROM audit_logs WHERE created_at < NOW() - INTERVAL '2 years';
    
    -- Clean up expired sessions
    DELETE FROM user_sessions WHERE expires_at < NOW();
    
    -- Clean up old file versions (keep last 10 versions)
    DELETE FROM file_versions fv1
    WHERE fv1.id NOT IN (
        SELECT fv2.id FROM file_versions fv2
        WHERE fv2.file_id = fv1.file_id
        ORDER BY fv2.version_number DESC
        LIMIT 10
    );
    
    -- Update statistics
    ANALYZE;
END;
$$ LANGUAGE plpgsql;
```

## 7. Security and Compliance

### 7.1 Data Protection

- **Encryption at Rest:** Database-level encryption for sensitive fields
- **Encryption in Transit:** SSL/TLS for all database connections
- **Password Security:** bcrypt hashing with salt rounds
- **PII Protection:** Anonymization procedures for deleted users

### 7.2 FERPA Compliance

- **Data Retention:** Configurable retention policies per university
- **Access Logging:** Complete audit trail for all data access
- **Data Export:** Student data portability features
- **Right to Deletion:** Secure data removal procedures

---

**ERD Document Status:** Active Development  
**Next Review:** End of Sprint 1  
**Database Version:** PostgreSQL 16.x  
**Schema Version:** 1.0  
**Last Updated:** December 2024

This ERD provides the complete database foundation for the Takharrujy platform, supporting all MVP features while establishing scalable patterns for future enhancements including AI integration and multi-university deployment.
