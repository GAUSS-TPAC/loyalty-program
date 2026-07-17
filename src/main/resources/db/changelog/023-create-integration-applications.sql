--liquibase formatted sql

--changeset yowyob:023-create-integration-applications
CREATE TABLE IF NOT EXISTS integration_applications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(1000),
    website_url         VARCHAR(2048),
    logo_url            VARCHAR(2048),
    public_key          VARCHAR(64) NOT NULL UNIQUE,
    api_key_id          UUID NOT NULL REFERENCES api_keys (id),
    webhook_endpoint_id UUID REFERENCES webhook_endpoints (id) ON DELETE SET NULL,
    mode                VARCHAR(10) NOT NULL DEFAULT 'LIVE',
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_integration_applications_tenant_id ON integration_applications (tenant_id);
CREATE INDEX IF NOT EXISTS idx_integration_applications_webhook_endpoint_id ON integration_applications (webhook_endpoint_id);
