--liquibase formatted sql

--changeset yowyob:008-create-loyalty-tables
-- ============================================================
-- V008 — Loyalty Core tables (rules, points, counters, tiers)
-- ============================================================

CREATE TABLE IF NOT EXISTS rules (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            INT NOT NULL DEFAULT 0,
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    trigger_definition  JSONB NOT NULL,
    conditions          JSONB NOT NULL DEFAULT '[]',
    effects             JSONB NOT NULL DEFAULT '[]',
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    version             INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rules_tenant_status ON rules(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rules_tenant_priority ON rules(tenant_id, priority DESC) WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS points_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    member_id           UUID NOT NULL,
    available_points    BIGINT NOT NULL DEFAULT 0,
    lifetime_earned     BIGINT NOT NULL DEFAULT 0,
    lifetime_spent      BIGINT NOT NULL DEFAULT 0,
    version             BIGINT NOT NULL DEFAULT 0,
    last_activity_at    TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_points_member_tenant UNIQUE (member_id, tenant_id),
    CONSTRAINT points_available_nn CHECK (available_points >= 0)
);

CREATE INDEX IF NOT EXISTS idx_points_member_tenant ON points_accounts(member_id, tenant_id);

CREATE TABLE IF NOT EXISTS points_transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    points_account_id       UUID NOT NULL REFERENCES points_accounts(id),
    tenant_id               UUID NOT NULL,
    type                    VARCHAR(50) NOT NULL,
    amount                  BIGINT NOT NULL,
    balance_after           BIGINT NOT NULL,
    source                  VARCHAR(100) NOT NULL,
    rule_id                 UUID,
    event_idempotency_key   VARCHAR(255),
    metadata                JSONB NOT NULL DEFAULT '{}',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_points_txn_account ON points_transactions(points_account_id, created_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uq_points_txn_event_key
    ON points_transactions(tenant_id, event_idempotency_key)
    WHERE event_idempotency_key IS NOT NULL;

CREATE TABLE IF NOT EXISTS loyalty_counters (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    member_id       UUID NOT NULL,
    counter_key     VARCHAR(255) NOT NULL,
    value           BIGINT NOT NULL DEFAULT 0,
    window_type     VARCHAR(50),
    window_start    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_counter_member_key UNIQUE (tenant_id, member_id, counter_key)
);

CREATE INDEX IF NOT EXISTS idx_counter_member_tenant ON loyalty_counters(tenant_id, member_id);

CREATE TABLE IF NOT EXISTS member_tiers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    member_id       UUID NOT NULL,
    tier_level      VARCHAR(50) NOT NULL DEFAULT 'BRONZE',
    multiplier      DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    reached_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_until     TIMESTAMPTZ,
    CONSTRAINT uq_tier_member_tenant UNIQUE (member_id, tenant_id)
);

CREATE TABLE IF NOT EXISTS tier_policies (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL UNIQUE,
    criterion                   VARCHAR(100) NOT NULL DEFAULT 'LIFETIME_POINTS',
    thresholds                  JSONB NOT NULL DEFAULT '[]',
    maintain_period             VARCHAR(50) NOT NULL DEFAULT 'QUARTERLY',
    maintain_threshold_points   BIGINT NOT NULL DEFAULT 0,
    downgrade_grace_days        INT NOT NULL DEFAULT 30,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
