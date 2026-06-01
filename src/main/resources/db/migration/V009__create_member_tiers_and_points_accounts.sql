-- Paliers membres
CREATE TABLE IF NOT EXISTS member_tiers (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL,
    member_id        VARCHAR(255) NOT NULL,
    level            VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    lifetime_points  BIGINT      NOT NULL DEFAULT 0,
    reached_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, member_id)
);

CREATE INDEX IF NOT EXISTS idx_member_tiers_tenant_member ON member_tiers(tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_member_tiers_level         ON member_tiers(tenant_id, level);

-- Comptes de points
CREATE TABLE IF NOT EXISTS points_accounts (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL,
    member_id        VARCHAR(255) NOT NULL,
    available_points BIGINT      NOT NULL DEFAULT 0,
    lifetime_earned  BIGINT      NOT NULL DEFAULT 0,
    lifetime_spent   BIGINT      NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, member_id)
);

CREATE INDEX IF NOT EXISTS idx_points_accounts_tenant_member ON points_accounts(tenant_id, member_id);

-- Transactions de points (immuables)
CREATE TABLE IF NOT EXISTS points_transactions (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL,
    member_id        VARCHAR(255) NOT NULL,
    type             VARCHAR(20) NOT NULL,
    amount           BIGINT      NOT NULL,
    balance_before   BIGINT      NOT NULL,
    balance_after    BIGINT      NOT NULL,
    description      VARCHAR(500),
    source_reference VARCHAR(255),
    occurred_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_points_tx_tenant_member ON points_transactions(tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_points_tx_occurred_at   ON points_transactions(occurred_at DESC);
