-- ============================================================
-- V012 — Referral (parrainage) tables
-- ============================================================

CREATE TABLE IF NOT EXISTS referral_programs (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL UNIQUE,
    name                        VARCHAR(255) NOT NULL,
    active                      BOOLEAN NOT NULL DEFAULT false,
    max_referrals_per_referrer  INT NOT NULL DEFAULT 0,
    referral_window_days        INT NOT NULL DEFAULT 30,
    referrer_reward_id          UUID,
    referee_reward_id           UUID,
    min_conversion_amount       INT NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS referral_links (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    referrer_id     UUID NOT NULL,
    code            VARCHAR(20) NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    usage_count     INT NOT NULL DEFAULT 0,
    conversion_count INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT true,
    version         BIGINT NOT NULL DEFAULT 0,
    UNIQUE(tenant_id, referrer_id)
);

CREATE INDEX IF NOT EXISTS idx_referral_links_code ON referral_links(code);
CREATE INDEX IF NOT EXISTS idx_referral_links_referrer ON referral_links(tenant_id, referrer_id);

CREATE TABLE IF NOT EXISTS referral_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    referral_link_id    UUID NOT NULL REFERENCES referral_links(id),
    referrer_id         UUID NOT NULL,
    referee_id          UUID NOT NULL,
    enrolled_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    converted_at        TIMESTAMPTZ,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    fraud_reason        TEXT,
    conversion_amount   NUMERIC(15, 2),
    idempotency_key     VARCHAR(255) UNIQUE,
    version             BIGINT NOT NULL DEFAULT 0,
    UNIQUE(tenant_id, referee_id)
);

CREATE INDEX IF NOT EXISTS idx_referral_events_referrer ON referral_events(tenant_id, referrer_id);
CREATE INDEX IF NOT EXISTS idx_referral_events_referee ON referral_events(tenant_id, referee_id);
CREATE INDEX IF NOT EXISTS idx_referral_events_status ON referral_events(tenant_id, status);
