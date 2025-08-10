-- Migration: Add phone column to app_user table
ALTER TABLE app_user ADD COLUMN phone VARCHAR(20); 