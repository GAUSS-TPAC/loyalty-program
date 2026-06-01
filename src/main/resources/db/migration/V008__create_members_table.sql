CREATE TABLE IF NOT EXISTS members (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL,
    external_id  VARCHAR(255) NOT NULL,
    email        VARCHAR(255),
    phone        VARCHAR(50),
    display_name VARCHAR(255),
    status       VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    UNIQUE (tenant_id, external_id)
);

CREATE INDEX IF NOT EXISTS idx_members_tenant_external  ON members(tenant_id, external_id);
CREATE INDEX IF NOT EXISTS idx_members_tenant_status    ON members(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_members_email            ON members(email);
