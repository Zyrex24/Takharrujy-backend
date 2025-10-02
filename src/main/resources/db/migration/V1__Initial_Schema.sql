-- Initial schema for Takharrujy Platform
-- Creating PostgreSQL database schema with full Arabic language support and row-level security

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search optimization

-- Create custom enums
CREATE TYPE user_role AS ENUM ('STUDENT', 'SUPERVISOR', 'ADMIN');
CREATE TYPE project_status AS ENUM ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED', 'REJECTED');
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'REVIEW', 'COMPLETED', 'BLOCKED', 'CANCELLED');
CREATE TYPE member_role AS ENUM ('LEADER', 'MEMBER', 'COLLABORATOR');
CREATE TYPE member_status AS ENUM ('PENDING', 'ACTIVE', 'INACTIVE', 'REJECTED', 'REMOVED');

-- Universities table (top-level tenant)
CREATE TABLE universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255) NOT NULL,
    domain VARCHAR(100) NOT NULL UNIQUE,
    contact_email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    address_ar VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    country_code VARCHAR(10),
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    name_ar VARCHAR(255) NOT NULL,
    code VARCHAR(20) UNIQUE,
    description TEXT,
    description_ar TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Users table (students, supervisors, admins)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name_ar VARCHAR(100),
    last_name_ar VARCHAR(100),
    role user_role NOT NULL,
    student_id VARCHAR(20) UNIQUE,
    phone VARCHAR(20),
    date_of_birth DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    profile_picture_url VARCHAR(255),
    bio TEXT,
    bio_ar TEXT,
    preferred_language VARCHAR(10) DEFAULT 'ar',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    team_leader_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    supervisor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    title_ar VARCHAR(255),
    description TEXT NOT NULL,
    description_ar TEXT,
    status project_status NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    due_date DATE,
    completion_date DATE,
    project_type VARCHAR(100),
    objectives TEXT,
    objectives_ar TEXT,
    technologies VARCHAR(1000),
    is_public BOOLEAN DEFAULT false,
    progress_percentage INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    final_grade DECIMAL(5,2),
    supervisor_feedback TEXT,
    supervisor_feedback_ar TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Project members table (many-to-many relationship between projects and users)
CREATE TABLE project_members (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role member_role NOT NULL DEFAULT 'MEMBER',
    status member_status NOT NULL DEFAULT 'PENDING',
    invited_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    joined_at TIMESTAMP WITH TIME ZONE,
    invitation_sent_at TIMESTAMP WITH TIME ZONE,
    invitation_accepted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    UNIQUE(project_id, user_id)
);

-- Tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    parent_task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    assigned_to_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    title_ar VARCHAR(255),
    description TEXT,
    description_ar TEXT,
    status task_status NOT NULL DEFAULT 'TODO',
    start_date DATE,
    due_date DATE,
    completion_date DATE,
    priority INTEGER DEFAULT 1 CHECK (priority >= 1 AND priority <= 4), -- 1=Low, 2=Medium, 3=High, 4=Critical
    estimated_hours INTEGER,
    actual_hours INTEGER,
    progress_percentage INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    notes TEXT,
    notes_ar TEXT,
    is_milestone BOOLEAN DEFAULT false,
    task_order INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Task dependencies table (many-to-many self-referencing)
CREATE TABLE task_dependencies (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    depends_on_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(task_id, depends_on_id),
    CHECK (task_id != depends_on_id) -- Prevent self-dependency
);

-- Files table
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_file_id BIGINT REFERENCES files(id) ON DELETE SET NULL,
    original_filename VARCHAR(255) NOT NULL,
    unique_filename VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100),
    file_size BIGINT NOT NULL,
    storage_url VARCHAR(1000) NOT NULL,
    description VARCHAR(500),
    description_ar VARCHAR(500),
    is_public BOOLEAN DEFAULT false,
    download_count BIGINT DEFAULT 0,
    file_category VARCHAR(50),
    virus_scan_status VARCHAR(50) DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT true,
    file_hash VARCHAR(32),
    file_version INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_universities_domain ON universities(domain);
CREATE INDEX idx_universities_is_active ON universities(is_active);

CREATE INDEX idx_departments_university_id ON departments(university_id);
CREATE INDEX idx_departments_code ON departments(code);
CREATE INDEX idx_departments_is_active ON departments(is_active);

CREATE INDEX idx_users_university_id ON users(university_id);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_student_id ON users(student_id) WHERE student_id IS NOT NULL;
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE INDEX idx_projects_university_id ON projects(university_id);
CREATE INDEX idx_projects_team_leader_id ON projects(team_leader_id);
CREATE INDEX idx_projects_supervisor_id ON projects(supervisor_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_university_status ON projects(university_id, status);
CREATE INDEX idx_projects_due_date ON projects(due_date);

CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);
CREATE INDEX idx_project_members_status ON project_members(status);

CREATE INDEX idx_tasks_university_id ON tasks(university_id);
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_parent_task_id ON tasks(parent_task_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_priority ON tasks(priority);

CREATE INDEX idx_task_dependencies_task_id ON task_dependencies(task_id);
CREATE INDEX idx_task_dependencies_depends_on_id ON task_dependencies(depends_on_id);

CREATE INDEX idx_files_university_id ON files(university_id);
CREATE INDEX idx_files_project_id ON files(project_id);
CREATE INDEX idx_files_task_id ON files(task_id);
CREATE INDEX idx_files_uploaded_by_id ON files(uploaded_by_id);
CREATE INDEX idx_files_unique_filename ON files(unique_filename);
CREATE INDEX idx_files_is_active ON files(is_active);
CREATE INDEX idx_files_virus_scan_status ON files(virus_scan_status);

-- Text search indexes for Arabic content
CREATE INDEX idx_universities_name_gin ON universities USING gin(to_tsvector('arabic', coalesce(name_ar, name)));
CREATE INDEX idx_departments_name_gin ON departments USING gin(to_tsvector('arabic', coalesce(name_ar, name)));
CREATE INDEX idx_users_name_gin ON users USING gin(to_tsvector('arabic', coalesce(first_name_ar || ' ' || last_name_ar, first_name || ' ' || last_name)));
CREATE INDEX idx_projects_title_gin ON projects USING gin(to_tsvector('arabic', coalesce(title_ar, title)));
CREATE INDEX idx_projects_description_gin ON projects USING gin(to_tsvector('arabic', coalesce(description_ar, description)));
CREATE INDEX idx_tasks_title_gin ON tasks USING gin(to_tsvector('arabic', coalesce(title_ar, title)));

-- Enable Row Level Security (RLS) for multi-tenancy
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE departments ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE task_dependencies ENABLE ROW LEVEL SECURITY;
ALTER TABLE files ENABLE ROW LEVEL SECURITY;

-- Create RLS policies (these will be applied when user context is set)
-- Users can only access data from their own university
CREATE POLICY users_tenant_policy ON users
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

CREATE POLICY departments_tenant_policy ON departments
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

CREATE POLICY projects_tenant_policy ON projects
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

CREATE POLICY project_members_tenant_policy ON project_members
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

CREATE POLICY tasks_tenant_policy ON tasks
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

CREATE POLICY files_tenant_policy ON files
    FOR ALL
    TO PUBLIC
    USING (university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id));

-- Task dependencies inherit from task policy
CREATE POLICY task_dependencies_tenant_policy ON task_dependencies
    FOR ALL
    TO PUBLIC
    USING (EXISTS (
        SELECT 1 FROM tasks t 
        WHERE t.id = task_dependencies.task_id 
        AND t.university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, t.university_id)
    ));

-- Add triggers for updating 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to all tables
CREATE TRIGGER update_universities_updated_at BEFORE UPDATE ON universities FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_project_members_updated_at BEFORE UPDATE ON project_members FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_files_updated_at BEFORE UPDATE ON files FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample universities for development
INSERT INTO universities (name, name_ar, domain, contact_email, country_code, timezone) VALUES
('Cairo University', 'جامعة القاهرة', 'cu.edu.eg', 'info@cu.edu.eg', 'EG', 'Africa/Cairo'),
('King Saud University', 'جامعة الملك سعود', 'ksu.edu.sa', 'info@ksu.edu.sa', 'SA', 'Asia/Riyadh'),
('American University of Beirut', 'الجامعة الأمريكية في بيروت', 'aub.edu.lb', 'info@aub.edu.lb', 'LB', 'Asia/Beirut');

-- Insert sample departments
INSERT INTO departments (university_id, name, name_ar, code, description, description_ar) VALUES
-- Cairo University departments
(1, 'Computer Science', 'علوم الحاسوب', 'CS', 'Department of Computer Science and Software Engineering', 'قسم علوم الحاسوب وهندسة البرمجيات'),
(1, 'Information Systems', 'نظم المعلومات', 'IS', 'Department of Information Systems and Data Management', 'قسم نظم المعلومات وإدارة البيانات'),
(1, 'Engineering', 'الهندسة', 'ENG', 'Faculty of Engineering', 'كلية الهندسة'),

-- King Saud University departments
(2, 'Computer Science', 'علوم الحاسوب', 'CS-KSU', 'Department of Computer Science', 'قسم علوم الحاسوب'),
(2, 'Software Engineering', 'هندسة البرمجيات', 'SE', 'Department of Software Engineering', 'قسم هندسة البرمجيات'),

-- American University of Beirut departments  
(3, 'Computer Science', 'علوم الحاسوب', 'CS-AUB', 'Department of Computer Science', 'قسم علوم الحاسوب'),
(3, 'Electrical Engineering', 'الهندسة الكهربائية', 'EE', 'Department of Electrical and Computer Engineering', 'قسم الهندسة الكهربائية والحاسوب');

-- Create a sample admin user for each university (password: 'admin123')
INSERT INTO users (university_id, department_id, email, password_hash, first_name, last_name, first_name_ar, last_name_ar, role, is_active, is_email_verified, preferred_language) VALUES
(1, 1, 'admin@cu.edu.eg', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeHR.KHCpRx2g.MX2', 'Admin', 'User', 'مدير', 'النظام', 'ADMIN', true, true, 'ar'),
(2, 4, 'admin@ksu.edu.sa', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeHR.KHCpRx2g.MX2', 'Admin', 'User', 'مدير', 'النظام', 'ADMIN', true, true, 'ar'),
(3, 6, 'admin@aub.edu.lb', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeHR.KHCpRx2g.MX2', 'Admin', 'User', 'مدير', 'النظام', 'ADMIN', true, true, 'ar');


-- ============================================
-- Comments Table
-- ============================================

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,

    -- Audit fields (BaseEntity style)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Indexes
CREATE INDEX idx_comments_university_id ON comments(university_id);
CREATE INDEX idx_comments_task_id ON comments(task_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Trigger for updated_at
CREATE TRIGGER update_comments_updated_at
BEFORE UPDATE ON comments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Row Level Security (RLS) for multi-tenancy
-- ============================================

ALTER TABLE comments ENABLE ROW LEVEL SECURITY;

-- Policy: users can only see comments of their own university
CREATE POLICY comments_tenant_policy ON comments
    FOR ALL
    TO PUBLIC
    USING (
        university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id)
    );

-- ================================
-- Deliverables Table
-- ================================
CREATE TABLE deliverables (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    due_date TIMESTAMP,
    submitted_at TIMESTAMP,
    submission_notes VARCHAR(2000),
    submission_file_url VARCHAR(500),
    supervisor_feedback VARCHAR(2000),
    supervisor_feedback_ar VARCHAR(2000),

    -- Relationships
    project_id BIGINT NOT NULL,

    -- Audit fields
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_deliverables_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_deliverables_university FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE
);

-- ============================================
-- Indexes
-- ============================================
CREATE INDEX idx_deliverables_university_id ON deliverables(university_id);
CREATE INDEX idx_deliverables_project_id ON deliverables(project_id);
CREATE INDEX idx_deliverables_status ON deliverables(status);
CREATE INDEX idx_deliverables_due_date ON deliverables(due_date);
CREATE INDEX idx_deliverables_created_at ON deliverables(created_at);

-- ============================================
-- Trigger for updated_at
-- ============================================
CREATE TRIGGER update_deliverables_updated_at
BEFORE UPDATE ON deliverables
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Row Level Security (RLS) for multi-tenancy
-- ============================================
ALTER TABLE deliverables ENABLE ROW LEVEL SECURITY;

-- Policy: users can only see deliverables of their own university
CREATE POLICY deliverables_tenant_policy ON deliverables
    FOR ALL
    TO PUBLIC
    USING (
        university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id)
    );


-- ================================
-- Notifications Table
-- ================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,

    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50),

    -- Relationships
    user_id BIGINT NOT NULL,

    -- Audit fields (from BaseEntity)
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_university FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE
);

-- ============================================
-- Indexes
-- ============================================
CREATE INDEX idx_notifications_university_id ON notifications(university_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ============================================
-- Trigger for updated_at
-- ============================================
CREATE TRIGGER update_notifications_updated_at
BEFORE UPDATE ON notifications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Row Level Security (RLS) for multi-tenancy
-- ============================================
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- Policy: users can only see notifications of their own university
CREATE POLICY notifications_tenant_policy ON notifications
    FOR ALL
    TO PUBLIC
    USING (
        university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id)
    );

-- ================================
-- Project Files Table
-- ================================
CREATE TABLE project_files (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL,

    project_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit fields
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_project_files_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_files_user FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_files_university FOREIGN KEY (university_id) REFERENCES universities(id) ON DELETE CASCADE
);

-- ============================================
-- Indexes
-- ============================================
CREATE INDEX idx_project_files_project ON project_files(project_id);
CREATE INDEX idx_project_files_uploaded_by ON project_files(uploaded_by_user_id);
CREATE INDEX idx_project_files_uploaded_at ON project_files(uploaded_at);
CREATE INDEX idx_project_files_university_id ON project_files(university_id);

-- ============================================
-- Trigger for updated_at
-- ============================================
CREATE TRIGGER update_project_files_updated_at
BEFORE UPDATE ON project_files
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- Row Level Security (RLS) for multi-tenancy
-- ============================================
ALTER TABLE project_files ENABLE ROW LEVEL SECURITY;

-- Policy: users can only see project files of their own university
CREATE POLICY project_files_tenant_policy ON project_files
    FOR ALL
    TO PUBLIC
    USING (
        university_id = COALESCE(current_setting('app.current_university_id', true)::bigint, university_id)
    );


COMMIT;