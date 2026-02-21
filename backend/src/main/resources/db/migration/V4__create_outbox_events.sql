CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,         -- 'POST'
    aggregate_id   UUID        NOT NULL,           -- post.id
    event_type     VARCHAR(100) NOT NULL,          -- 'POST_SCHEDULE_REQUESTED'
    payload        TEXT        NOT NULL,           -- JSON: everything needed to call Late
    status         VARCHAR(50) DEFAULT 'pending',  -- pending|processing|sent|failed
    attempts       INT         DEFAULT 0,
    last_error     TEXT,
    created_at     TIMESTAMP   DEFAULT NOW(),
    processed_at   TIMESTAMP
);

CREATE INDEX idx_outbox_status ON outbox_events(status)
    WHERE status IN ('pending', 'failed');
