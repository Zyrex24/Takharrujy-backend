-- Change project status column from enum to varchar for better compatibility
-- This migration fixes the PostgreSQL enum type casting issue

-- First, alter the column to varchar with explicit casting
ALTER TABLE projects ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- Add a check constraint to maintain data integrity (optional but recommended)
ALTER TABLE projects ADD CONSTRAINT check_project_status 
    CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED', 'REJECTED'));
