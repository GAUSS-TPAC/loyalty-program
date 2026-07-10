--liquibase formatted sql

--changeset yowyob:014-create-campaign-tables
-- ============================================================
-- V014 — Campagnes temporisées
-- ============================================================

CREATE TABLE IF NOT EXISTS campaigns (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    campaign_type       VARCHAR(50) NOT NULL,
    target_event_type   VARCHAR(100),
    bonus_multiplier    NUMERIC(8, 4) NOT NULL DEFAULT 1.0,
    bonus_points        BIGINT NOT NULL DEFAULT 0,
    start_date          TIMESTAMPTZ NOT NULL,
    end_date            TIMESTAMPTZ,
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_campaigns_tenant_status ON campaigns(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_campaigns_dates ON campaigns(start_date, end_date) WHERE status IN ('DRAFT', 'ACTIVE');
