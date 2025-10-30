CREATE TABLE shortened_urls (
    id BIGSERIAL,
    short_URL VARCHAR(10),
    url_hash VARCHAR(12),
    full_url TEXT,
    UNIQUE(short_URL)
);

CREATE INDEX url_hash_index ON shortened_urls(url_hash);