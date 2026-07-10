--liquibase formatted sql

--changeset yowyob:017-create-webhook-tables
CREATE TABLE IF NOT EXISTS webhook_endpoints (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL,
    url          VARCHAR(2048) NOT NULL,
    secret       VARCHAR(128) NOT NULL,
    description  VARCHAR(255),
    event_types  VARCHAR(500) NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version      BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_webhook_endpoints_tenant_id ON webhook_endpoints (tenant_id);

CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL,
    endpoint_id       UUID NOT NULL REFERENCES webhook_endpoints (id),
    event_type        VARCHAR(100) NOT NULL,
    payload           TEXT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    http_status_code  INT,
    response_snippet  VARCHAR(1000),
    attempt_count     INT NOT NULL DEFAULT 0,
    next_attempt_at   TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delivered_at      TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_webhook_deliveries_tenant_id ON webhook_deliveries (tenant_id);
CREATE INDEX IF NOT EXISTS idx_webhook_deliveries_status_next_attempt ON webhook_deliveries (status, next_attempt_at);
