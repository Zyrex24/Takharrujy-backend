-- Migration: Add missing audit columns to user_preferences table
-- Version: V6
-- Description: Add created_by, updated_by, version columns to user_preferences table for BaseEntity compatibility

-- Add missing audit columns to user_preferences table
ALTER TABLE user_preferences 
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN updated_by VARCHAR(255),
ADD COLUMN version BIGINT DEFAULT 0;

-- Add comments for new columns
COMMENT ON COLUMN user_preferences.created_by IS 'User who created this preferences record';
COMMENT ON COLUMN user_preferences.updated_by IS 'User who last updated this preferences record';
COMMENT ON COLUMN user_preferences.version IS 'Optimistic locking version number';