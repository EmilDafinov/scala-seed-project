CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_group VARCHAR(100) NOT NULL,
    content JSONB NOT NULL,
    delivered BOOLEAN DEFAULT FALSE
);