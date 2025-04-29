CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL,
    content JSONB NOT NULL
);