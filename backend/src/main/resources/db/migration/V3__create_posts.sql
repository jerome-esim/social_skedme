CREATE TABLE posts (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID REFERENCES users(id) ON DELETE CASCADE,
    title          VARCHAR(255),
    caption        TEXT,
    hashtags       TEXT,
    video_url      TEXT NOT NULL,
    video_filename VARCHAR(255),
    platforms      TEXT,                        -- JSON array: ["instagram","tiktok"]
    scheduled_at   TIMESTAMP NOT NULL,          -- stored in UTC
    timezone       VARCHAR(100) DEFAULT 'Europe/Paris',
    status         VARCHAR(50) DEFAULT 'draft', -- draft|pending|scheduled|published|failed
    late_post_id   VARCHAR(255),                -- ID returned by getlate.dev
    error_message  TEXT,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_posts_user_id    ON posts(user_id);
CREATE INDEX idx_posts_status     ON posts(status);
CREATE INDEX idx_posts_scheduled  ON posts(scheduled_at);
