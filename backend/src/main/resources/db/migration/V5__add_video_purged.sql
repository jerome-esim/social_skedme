ALTER TABLE posts
    ADD COLUMN video_purged    BOOLEAN   DEFAULT FALSE,
    ADD COLUMN video_purged_at TIMESTAMP;

CREATE INDEX idx_posts_video_cleanup
    ON posts(status, updated_at)
    WHERE video_purged = FALSE;
