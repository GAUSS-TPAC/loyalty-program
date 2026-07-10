--liquibase formatted sql

--changeset yowyob:009-create-tenants-table
CREATE TABLE IF NOT EXISTS tenants (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    status      VARCHAR(50) NOT NULL,
    plan        VARCHAR(50) NOT NULL,
    config      JSONB DEFAULT '{}',
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants (status);
