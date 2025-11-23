CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL UNIQUE,
    quantity INTEGER NOT NULL,
    version BIGINT
);

INSERT INTO inventory (product_id, quantity, version) VALUES ('PROD-001', 100, 0);
INSERT INTO inventory (product_id, quantity, version) VALUES ('PROD-002', 50, 0);
INSERT INTO inventory (product_id, quantity, version) VALUES ('PROD-003', 200, 0);
