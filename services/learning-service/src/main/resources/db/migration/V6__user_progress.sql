-- V6: user_progress table — per-user learning activity stats owned by
-- learning-service. Replaces the streak_count / last_study_date / points /
-- daily_goal_minutes columns that user-service used to carry.
--
-- user_id is a cross-service reference (no FK to a local users table).

CREATE TABLE IF NOT EXISTS user_progress (
    user_id            UUID         PRIMARY KEY,
    streak_count       INT          NOT NULL DEFAULT 0,
    last_study_date    TIMESTAMP,
    points             INT          NOT NULL DEFAULT 0,
    daily_goal_minutes INT          NOT NULL DEFAULT 15,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(64),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by         VARCHAR(64)
);
