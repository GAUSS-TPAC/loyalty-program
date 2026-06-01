CREATE TABLE IF NOT EXISTS rules (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    priority      INT          NOT NULL DEFAULT 100,
    status        VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    trigger_def   JSONB        NOT NULL,
    conditions_def JSONB       NOT NULL DEFAULT '[]',
    effects_def   JSONB        NOT NULL DEFAULT '[]',
    valid_from    TIMESTAMPTZ,
    valid_until   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_rules_tenant_status   ON rules(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rules_tenant_priority  ON rules(tenant_id, priority DESC);
