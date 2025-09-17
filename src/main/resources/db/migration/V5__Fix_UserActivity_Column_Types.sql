-- Migration: Fix column types in user_activities table
-- Version: V5  
-- Description: Convert ip_address from INET to VARCHAR and user_agent from TEXT to VARCHAR(500)

-- Convert ip_address from INET to VARCHAR(45)
ALTER TABLE user_activities ALTER COLUMN ip_address TYPE VARCHAR(45) USING ip_address::text;

-- Convert user_agent from VARCHAR(500) to TEXT (as specified in entity columnDefinition)
-- Actually, let's check what we have first and make it consistent with the entity
ALTER TABLE user_activities ALTER COLUMN user_agent TYPE TEXT;

-- Add comments for clarity
COMMENT ON COLUMN user_activities.ip_address IS 'Client IP address as string (IPv4 or IPv6)';
COMMENT ON COLUMN user_activities.user_agent IS 'Client user agent string';