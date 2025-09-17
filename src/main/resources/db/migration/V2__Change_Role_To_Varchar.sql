-- Change role column from enum to varchar to resolve Hibernate type mapping issues

-- First, alter the column type
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(50);

-- Add a check constraint to ensure only valid values are allowed
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('STUDENT', 'SUPERVISOR', 'ADMIN'));