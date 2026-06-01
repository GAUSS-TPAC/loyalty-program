-- Catalogue de récompenses du tenant
CREATE TABLE IF NOT EXISTS rewards (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(50)  NOT NULL,
    cost_points  BIGINT       NOT NULL,
    stock        INT,
    status       VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    valid_from   TIMESTAMPTZ,
    valid_until  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rewards_tenant_status ON rewards(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rewards_tenant_type   ON rewards(tenant_id, type);

-- Attributions de récompenses à des membres (reward grants)
CREATE TABLE IF NOT EXISTS reward_grants (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    member_id   VARCHAR(255) NOT NULL,
    reward_id   UUID         NOT NULL REFERENCES rewards(id),
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    granted_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ,
    used_at     TIMESTAMPTZ,
    use_context JSONB
);

CREATE INDEX IF NOT EXISTS idx_reward_grants_tenant_member ON reward_grants(tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_reward_grants_status        ON reward_grants(tenant_id, status);
