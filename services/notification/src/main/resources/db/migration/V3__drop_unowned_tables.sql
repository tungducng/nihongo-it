-- V3: Real db-per-service cleanup.
-- notification-service owns only the `notifications` table (recipients are
-- identified by user_id UUID without a local users table — the canonical
-- users table lives in user-service). Drop everything else that V1 created.

-- Drop FK from notifications -> users first.
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;

-- FK-dependent tables first.
DROP TABLE IF EXISTS saved_vocabulary CASCADE;
DROP TABLE IF EXISTS review_logs CASCADE;
DROP TABLE IF EXISTS flashcards CASCADE;
DROP TABLE IF EXISTS conversation_lines CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS feedback CASCADE;
DROP TABLE IF EXISTS vocabulary CASCADE;
DROP TABLE IF EXISTS topics CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS user_notification_preferences CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
