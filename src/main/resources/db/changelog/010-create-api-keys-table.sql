--liquibase formatted sql

--changeset yowyob:010-create-api-keys-table
CREATE TABLE IF NOT EXISTS api_keys (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL,
    name        VARCHAR(255) NOT NULL,
    key_hash    VARCHAR(64) NOT NULL UNIQUE,
    key_prefix  VARCHAR(8)  NOT NULL,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_api_keys_tenant_id ON api_keys (tenant_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_key_hash  ON api_keys (key_hash);
