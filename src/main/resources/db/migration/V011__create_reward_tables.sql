-- ============================================================
-- V011 — Rewards catalogue and grants
-- ============================================================

CREATE TABLE IF NOT EXISTS rewards (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    type                VARCHAR(100) NOT NULL,
    value_json          JSONB NOT NULL,
    cost_in_points      BIGINT NOT NULL DEFAULT 0,
    stock_total         INT,
    stock_remaining     INT,
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    grant_expiry_days   INT NOT NULL DEFAULT 0,
    image_url           VARCHAR(500),
    metadata            JSONB NOT NULL DEFAULT '{}',
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    version             INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_stock CHECK (
        (stock_total IS NULL AND stock_remaining IS NULL)
        OR (stock_total IS NOT NULL AND stock_remaining IS NOT NULL
            AND stock_remaining >= 0 AND stock_remaining <= stock_total)
    ),
    CONSTRAINT chk_cost CHECK (cost_in_points >= 0)
);

CREATE INDEX IF NOT EXISTS idx_rewards_tenant_status ON rewards(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rewards_tenant_active ON rewards(tenant_id) WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS reward_grants (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL,
    member_id               UUID NOT NULL,
    reward_id               UUID NOT NULL REFERENCES rewards(id),
    reward_name             VARCHAR(255) NOT NULL,
    reward_type             VARCHAR(100) NOT NULL,
    reward_value_json       JSONB NOT NULL,
    source                  VARCHAR(100) NOT NULL,
    source_rule_id          UUID,
    source_event_id         VARCHAR(255),
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remaining_applications  INT NOT NULL DEFAULT 1,
    granted_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at              TIMESTAMPTZ,
    used_at                 TIMESTAMPTZ,
    used_in_context         JSONB,
    idempotency_key         VARCHAR(255) UNIQUE,
    version                 INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_remaining CHECK (remaining_applications >= 0)
);

CREATE INDEX IF NOT EXISTS idx_grants_member_tenant ON reward_grants(member_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_grants_member_status ON reward_grants(member_id, tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_grants_expires ON reward_grants(expires_at)
    WHERE status = 'ACTIVE' AND expires_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grants_idempotency ON reward_grants(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grants_reward ON reward_grants(reward_id);
