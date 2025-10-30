-- Migration: Add temporary_password column to users table
-- Feature: Force Password Change on First Login
-- Date: 2025-10-30
-- Description: Adds a boolean column to track if user has a temporary password
--              that must be changed on first login

-- Add column temporary_password with default false
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS temporary_password BOOLEAN DEFAULT false;

-- Create partial index for performance (only index rows where temporary_password = true)
CREATE INDEX IF NOT EXISTS idx_users_temporary_password 
ON users(temporary_password) 
WHERE temporary_password = true;

-- Optional: Mark existing users created with temporary passwords
-- Uncomment if you want to mark users created after the temp password feature was implemented
-- UPDATE users 
-- SET temporary_password = true 
-- WHERE created_at >= '2025-10-29' 
-- AND role = 'master'
-- AND password_hash IS NOT NULL;

-- Verify the changes
SELECT 
    column_name, 
    data_type, 
    column_default, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name = 'temporary_password';

-- Show index information
SELECT 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename = 'users' 
AND indexname = 'idx_users_temporary_password';

-- Count users with temporary passwords
SELECT 
    COUNT(*) as users_with_temp_password 
FROM users 
WHERE temporary_password = true;

COMMIT;
