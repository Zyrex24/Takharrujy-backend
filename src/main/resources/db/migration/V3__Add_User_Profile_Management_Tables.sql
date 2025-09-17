-- Migration: Add User Profile Management Tables
-- Version: V3
-- Description: Creates user_preferences and user_activities tables for user profile management system

-- Create user_activity_type enum
CREATE TYPE user_activity_type AS ENUM (
    'LOGIN',
    'LOGOUT', 
    'PROFILE_UPDATE',
    'PASSWORD_CHANGE',
    'AVATAR_UPLOAD',
    'PREFERENCES_UPDATE',
    'PROJECT_CREATE',
    'PROJECT_JOIN',
    'TASK_CREATE',
    'TASK_UPDATE',
    'FILE_UPLOAD',
    'MESSAGE_SEND'
);

-- Create user_preferences table
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    university_id BIGINT NOT NULL REFERENCES universities(id),
    
    -- Notification preferences
    email_notifications BOOLEAN NOT NULL DEFAULT true,
    push_notifications BOOLEAN NOT NULL DEFAULT true,
    sms_notifications BOOLEAN NOT NULL DEFAULT false,
    
    -- Specific notification types
    project_updates BOOLEAN NOT NULL DEFAULT true,
    task_assignments BOOLEAN NOT NULL DEFAULT true,
    task_due_reminders BOOLEAN NOT NULL DEFAULT true,
    project_invitations BOOLEAN NOT NULL DEFAULT true,
    new_messages BOOLEAN NOT NULL DEFAULT true,
    message_mentions BOOLEAN NOT NULL DEFAULT true,
    submission_notifications BOOLEAN NOT NULL DEFAULT true,
    progress_reports BOOLEAN NOT NULL DEFAULT false,
    
    -- UI preferences
    theme VARCHAR(20) NOT NULL DEFAULT 'auto' CHECK (theme IN ('light', 'dark', 'auto')),
    language VARCHAR(5) NOT NULL DEFAULT 'ar' CHECK (language IN ('ar', 'en')),
    timezone VARCHAR(50) NOT NULL DEFAULT 'Africa/Cairo',
    
    -- Privacy preferences
    profile_visibility VARCHAR(20) NOT NULL DEFAULT 'university' CHECK (profile_visibility IN ('public', 'university', 'private')),
    show_email BOOLEAN NOT NULL DEFAULT false,
    show_phone BOOLEAN NOT NULL DEFAULT false,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(user_id)
);

-- Create user_activities table
CREATE TABLE user_activities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    university_id BIGINT NOT NULL REFERENCES universities(id),
    
    -- Activity details
    activity_type user_activity_type NOT NULL,
    description VARCHAR(500) NOT NULL,
    description_ar VARCHAR(500),
    
    -- Resource information (optional)
    resource_type VARCHAR(50), -- 'project', 'task', 'file', etc.
    resource_id BIGINT,
    
    -- Request context
    ip_address INET,
    user_agent VARCHAR(500),
    
    -- Additional data (JSON for flexible metadata)
    additional_data JSONB,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for user_preferences
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX idx_user_preferences_university_id ON user_preferences(university_id);

-- Create indexes for user_activities
CREATE INDEX idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX idx_user_activities_university_id ON user_activities(university_id);
CREATE INDEX idx_user_activities_type ON user_activities(activity_type);
CREATE INDEX idx_user_activities_created_at ON user_activities(created_at);
CREATE INDEX idx_user_activities_resource ON user_activities(resource_type, resource_id);

-- Create trigger to update updated_at timestamp for user_preferences
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_preferences_updated_at 
    BEFORE UPDATE ON user_preferences 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Enable Row Level Security on new tables
ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_activities ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for user_preferences
CREATE POLICY user_preferences_tenant_policy ON user_preferences
    USING (university_id = current_setting('app.current_university_id')::bigint);

-- Create RLS policies for user_activities  
CREATE POLICY user_activities_tenant_policy ON user_activities
    USING (university_id = current_setting('app.current_university_id')::bigint);

-- Add comments for documentation
COMMENT ON TABLE user_preferences IS 'User notification and UI preferences with multi-tenancy support';
COMMENT ON TABLE user_activities IS 'User activity log with comprehensive audit trail and multi-tenancy support';

COMMENT ON COLUMN user_preferences.theme IS 'UI theme preference: light, dark, or auto';
COMMENT ON COLUMN user_preferences.language IS 'Preferred language: ar (Arabic) or en (English)';
COMMENT ON COLUMN user_preferences.profile_visibility IS 'Profile visibility: public, university-only, or private';

COMMENT ON COLUMN user_activities.activity_type IS 'Type of user activity from user_activity_type enum';
COMMENT ON COLUMN user_activities.resource_type IS 'Type of resource involved in the activity (project, task, file, etc.)';
COMMENT ON COLUMN user_activities.additional_data IS 'JSON metadata for flexible activity context storage';