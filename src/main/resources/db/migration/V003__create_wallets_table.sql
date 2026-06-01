CREATE TABLE IF NOT EXISTS wallets (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL,
    member_id   VARCHAR(255) NOT NULL,
    currency    VARCHAR(10)  NOT NULL DEFAULT 'PTS',
    balance     NUMERIC(20,2) NOT NULL DEFAULT 0.00,
    status      VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    policy      JSONB        NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    UNIQUE (tenant_id, member_id)
);

CREATE INDEX IF NOT EXISTS idx_wallets_tenant_member ON wallets(tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_wallets_status         ON wallets(status);
