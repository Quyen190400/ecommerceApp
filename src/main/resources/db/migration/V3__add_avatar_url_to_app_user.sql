-- Migration: Add avatar_url column to app_user table
ALTER TABLE app_user ADD COLUMN avatar_url VARCHAR(512); 