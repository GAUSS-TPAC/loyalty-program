-- Programme de parrainage (un par tenant)
CREATE TABLE IF NOT EXISTS referral_programs (
    id                       UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                UUID         NOT NULL UNIQUE,
    active                   BOOLEAN      NOT NULL DEFAULT true,
    referrer_reward_type     VARCHAR(50)  NOT NULL DEFAULT 'CREDIT_POINTS',
    referrer_reward_value    NUMERIC(10,2) NOT NULL DEFAULT 500,
    referee_reward_type      VARCHAR(50)  NOT NULL DEFAULT 'CREDIT_POINTS',
    referee_reward_value     NUMERIC(10,2) NOT NULL DEFAULT 200,
    conversion_event_type    VARCHAR(100) NOT NULL DEFAULT 'purchase.completed',
    min_conversion_amount    NUMERIC(10,2) NOT NULL DEFAULT 0,
    conversion_deadline_days INT          NOT NULL DEFAULT 30
);

-- Liens de parrainage (un par membre par tenant)
CREATE TABLE IF NOT EXISTS referral_links (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    referrer_id VARCHAR(255) NOT NULL,
    code        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, referrer_id),
    UNIQUE (tenant_id, code)
);

-- Événements de parrainage (un par couple parrain/filleul)
CREATE TABLE IF NOT EXISTS referral_events (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    referrer_id   VARCHAR(255) NOT NULL,
    referee_id    VARCHAR(255) NOT NULL,
    referral_code VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    enrolled_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    converted_at  TIMESTAMPTZ,
    rewarded_at   TIMESTAMPTZ,
    expires_at    TIMESTAMPTZ  NOT NULL,
    UNIQUE (tenant_id, referrer_id, referee_id)
);

CREATE INDEX IF NOT EXISTS idx_referral_links_tenant_code    ON referral_links(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_referral_events_tenant_referee ON referral_events(tenant_id, referee_id, status);
CREATE INDEX IF NOT EXISTS idx_referral_events_tenant_referrer ON referral_events(tenant_id, referrer_id);
