-- V7: Real db-per-service cleanup.
-- After Phase 1 split, each service owns its own database. user-service's V1
-- created the entire monolith schema; drop the tables that actually belong to
-- learning-service so user_service DB stays focused on user/auth domain.
-- Idempotent: uses IF EXISTS / CASCADE.

-- Drop FK-dependent tables first.
DROP TABLE IF EXISTS saved_vocabulary CASCADE;
DROP TABLE IF EXISTS review_logs CASCADE;
DROP TABLE IF EXISTS flashcards CASCADE;
DROP TABLE IF EXISTS conversation_lines CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS feedback CASCADE;
DROP TABLE IF EXISTS vocabulary CASCADE;
DROP TABLE IF EXISTS topics CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
