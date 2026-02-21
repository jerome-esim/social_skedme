CREATE TABLE social_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    platform        VARCHAR(50)  NOT NULL,    -- 'instagram', 'tiktok', 'linkedin'
    late_account_id VARCHAR(255) NOT NULL,    -- ID returned by getlate.dev
    account_name    VARCHAR(255),
    connected_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_social_accounts_user_id ON social_accounts(user_id);
