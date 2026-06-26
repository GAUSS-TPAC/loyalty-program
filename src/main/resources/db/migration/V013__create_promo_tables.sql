-- ============================================================
-- V013 — Codes Promo tables
-- ============================================================

CREATE TABLE IF NOT EXISTS promo_campaigns (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    discount_type       VARCHAR(50) NOT NULL,
    discount_value      NUMERIC(15, 4) NOT NULL,
    min_order_amount    NUMERIC(15, 2) NOT NULL DEFAULT 0,
    max_uses            INT NOT NULL DEFAULT 0,
    per_member_limit    INT NOT NULL DEFAULT 1,
    start_date          TIMESTAMPTZ NOT NULL,
    end_date            TIMESTAMPTZ,
    active              BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    UNIQUE(tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_promo_campaigns_tenant_code ON promo_campaigns(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_promo_campaigns_active ON promo_campaigns(tenant_id, active);

CREATE TABLE IF NOT EXISTS promo_usages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    campaign_id         UUID NOT NULL REFERENCES promo_campaigns(id),
    member_id           UUID NOT NULL,
    order_id            VARCHAR(255) NOT NULL,
    discount_applied    NUMERIC(15, 2) NOT NULL,
    used_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, campaign_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_promo_usages_campaign ON promo_usages(tenant_id, campaign_id);
CREATE INDEX IF NOT EXISTS idx_promo_usages_member ON promo_usages(tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_promo_usages_member_campaign ON promo_usages(tenant_id, campaign_id, member_id);
