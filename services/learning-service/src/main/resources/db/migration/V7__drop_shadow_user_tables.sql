-- V7: Drop shadow tables that mirror state owned by other services.
--
-- After P6.A/B/C, learning-service no longer owns:
--   - users / roles                   (canonical: user-service)
--   - refresh_tokens                  (canonical: user-service)
--   - user_notification_preferences   (canonical: notification-service)
--
-- Cross-service references (e.g. flashcards.user_id) keep the UUID column but
-- the DB-level FK was already dropped in V5. We can now drop the orphan
-- shadow tables themselves — no Kotlin code references them anymore.

DROP TABLE IF EXISTS user_notification_preferences CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
