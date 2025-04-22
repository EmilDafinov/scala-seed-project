CREATE TABLE events (
    id BIGINT PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL,
    content JSONB NOT NULL
);