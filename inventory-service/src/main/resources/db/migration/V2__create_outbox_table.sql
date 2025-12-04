CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255),
    aggregate_id VARCHAR(255),
    type VARCHAR(255),
    payload_class VARCHAR(255),
    payload VARCHAR(4096),
    topic VARCHAR(255),
    created_at TIMESTAMP
);
