-- Migration: Add missing audit columns to user_activities table
-- Version: V4
-- Description: Add created_by, updated_by, version columns to user_activities table for BaseEntity compatibility

-- Add missing audit columns to user_activities table
ALTER TABLE user_activities 
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255),
ADD COLUMN version BIGINT DEFAULT 0;

-- Also add updated_at column for consistency (user_activities only had created_at)
ALTER TABLE user_activities 
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Create trigger to update updated_at timestamp for user_activities
CREATE TRIGGER update_user_activities_updated_at 
    BEFORE UPDATE ON user_activities 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments for new columns
COMMENT ON COLUMN user_activities.created_by IS 'User who created this activity record';
COMMENT ON COLUMN user_activities.updated_by IS 'User who last updated this activity record';
COMMENT ON COLUMN user_activities.version IS 'Optimistic locking version number';
COMMENT ON COLUMN user_activities.updated_at IS 'Timestamp when this activity record was last updated';