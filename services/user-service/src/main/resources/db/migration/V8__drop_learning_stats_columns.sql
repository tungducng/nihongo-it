-- V8: Drop the learning-activity stat columns from users.
--
-- streak_count, last_study_date, points, daily_goal_minutes are now owned by
-- learning-service via its `user_progress` table (P6.A). Identity, profile
-- and notification-preference columns remain here.

ALTER TABLE users DROP COLUMN IF EXISTS streak_count;
ALTER TABLE users DROP COLUMN IF EXISTS last_study_date;
ALTER TABLE users DROP COLUMN IF EXISTS points;
ALTER TABLE users DROP COLUMN IF EXISTS daily_goal_minutes;
