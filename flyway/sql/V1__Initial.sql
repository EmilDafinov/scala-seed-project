CREATE TABLE shortened_urls (
    id BIGSERIAL,
    url_hash VARCHAR(12),
    full_url TEXT,
    UNIQUE(url_hash)
);