CREATE TABLE IF NOT EXISTS promo_campaigns (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID          NOT NULL,
    name                VARCHAR(255)  NOT NULL,
    code                VARCHAR(100),
    discount_type       VARCHAR(50)   NOT NULL,
    discount_value      NUMERIC(15,4) NOT NULL,
    status              VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    max_uses_total      INT,
    max_uses_per_member INT,
    min_order_amount    NUMERIC(15,2),
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_promo_tenant_code   ON promo_campaigns(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_promo_tenant_status ON promo_campaigns(tenant_id, status);

CREATE TABLE IF NOT EXISTS promo_usages (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID          NOT NULL,
    campaign_id      UUID          NOT NULL REFERENCES promo_campaigns(id),
    member_id        VARCHAR(255)  NOT NULL,
    order_reference  VARCHAR(255),
    discount_applied NUMERIC(15,4) NOT NULL,
    used_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_promo_usages_campaign ON promo_usages(campaign_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_promo_usages_member   ON promo_usages(campaign_id, member_id);
