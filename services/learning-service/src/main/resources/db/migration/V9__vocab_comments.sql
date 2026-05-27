-- V9: Vocabulary comment threads — Facebook-style nested comments scoped per
-- vocab entry. See docs/plans/2026-05-27-vocab-comments.md §3 for the model.

CREATE TABLE vocab_comments (
    comment_id        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    vocab_id          UUID         NOT NULL REFERENCES vocabulary(vocab_id) ON DELETE CASCADE,
    parent_comment_id UUID         REFERENCES vocab_comments(comment_id) ON DELETE CASCADE,
    user_id           UUID         NOT NULL,
    user_full_name    VARCHAR(100) NOT NULL,   -- denormalised at insert; no FK to user-service
    content           TEXT         NOT NULL,
    like_count        INTEGER      NOT NULL DEFAULT 0,
    reply_count       INTEGER      NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    created_by        VARCHAR(64),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by        VARCHAR(64),
    CONSTRAINT chk_vocab_comment_content_len CHECK (char_length(content) BETWEEN 1 AND 2000),
    CONSTRAINT chk_vocab_comment_self_parent CHECK (parent_comment_id IS NULL OR parent_comment_id <> comment_id)
);

-- Top-level listing by newest, scoped per vocab (most common path)
CREATE INDEX idx_vocab_comments_toplevel_newest
    ON vocab_comments (vocab_id, created_at DESC)
    WHERE parent_comment_id IS NULL AND deleted_at IS NULL;

-- Top-level listing by popularity
CREATE INDEX idx_vocab_comments_toplevel_top
    ON vocab_comments (vocab_id, like_count DESC, created_at DESC)
    WHERE parent_comment_id IS NULL AND deleted_at IS NULL;

-- Replies lookup
CREATE INDEX idx_vocab_comments_parent
    ON vocab_comments (parent_comment_id, created_at ASC)
    WHERE parent_comment_id IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE vocab_comment_likes (
    comment_id UUID      NOT NULL REFERENCES vocab_comments(comment_id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (comment_id, user_id)
);

-- "Has user X liked comment Y" lookup + reverse lookup
CREATE INDEX idx_vocab_comment_likes_user ON vocab_comment_likes (user_id, comment_id);
