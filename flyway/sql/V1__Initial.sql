CREATE EXTENSION "uuid-ossp";
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL DEFAULT uuid_generate_v1(),
    event_group VARCHAR(100) NOT NULL,
    content JSONB NOT NULL,
    delivered BOOLEAN DEFAULT FALSE
);