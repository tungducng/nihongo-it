-- V5: Decouple cross-service FKs.
--
-- After Phase 1 (DB-per-service), the canonical users / roles / notifications
-- live in user-service / notification-service respectively. learning-service
-- columns that reference user_id are now cross-service references — they keep
-- the column but lose the DB-level FK constraint, so writes don't require a
-- local users row.
--
-- The local users / roles tables are kept temporarily as a stale shadow until
-- learning-service.UserService is replaced with a REST client to user-service.
-- See docs/plans/2026-05-19-yas-adoption-plan.md (P6 follow-up).

ALTER TABLE flashcards         DROP CONSTRAINT IF EXISTS flashcards_user_id_fkey;
ALTER TABLE feedback           DROP CONSTRAINT IF EXISTS feedback_user_id_fkey;
ALTER TABLE saved_vocabulary   DROP CONSTRAINT IF EXISTS saved_vocabulary_user_id_fkey;
ALTER TABLE notifications      DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;

-- notifications table is owned by notification-service. learning-service does
-- not send notifications itself; the entity here is orphan code (no repository
-- references it). Drop the table to avoid confusion.
DROP TABLE IF EXISTS notifications CASCADE;
