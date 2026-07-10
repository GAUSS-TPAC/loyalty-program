--liquibase formatted sql

--changeset yowyob:015-create-subscription-tables
-- ============================================================
-- V015 — Abonnements SaaS
-- ============================================================

CREATE TABLE IF NOT EXISTS subscription_plans (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                    VARCHAR(50) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    price_monthly           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    price_yearly            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    currency                VARCHAR(10) NOT NULL DEFAULT 'XAF',
    max_rules               INT NOT NULL DEFAULT 5,
    max_members             INT NOT NULL DEFAULT 0,
    max_events_per_month    INT NOT NULL DEFAULT 0,
    referral_enabled        BOOLEAN NOT NULL DEFAULT false,
    campaigns_enabled       BOOLEAN NOT NULL DEFAULT false,
    promo_codes_enabled     BOOLEAN NOT NULL DEFAULT false,
    analytics_enabled       BOOLEAN NOT NULL DEFAULT false,
    active                  BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tenant_subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL UNIQUE,
    plan_id                 UUID NOT NULL REFERENCES subscription_plans(id),
    status                  VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    billing_cycle           VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    current_period_start    TIMESTAMPTZ NOT NULL,
    current_period_end      TIMESTAMPTZ NOT NULL,
    trial_end_date          TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON tenant_subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_period_end ON tenant_subscriptions(current_period_end) WHERE status IN ('ACTIVE','TRIAL','PAST_DUE');

CREATE TABLE IF NOT EXISTS invoice_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    subscription_id     UUID NOT NULL REFERENCES tenant_subscriptions(id),
    plan_id             UUID NOT NULL REFERENCES subscription_plans(id),
    amount              NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'XAF',
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    period_start        TIMESTAMPTZ NOT NULL,
    period_end          TIMESTAMPTZ NOT NULL,
    due_date            TIMESTAMPTZ NOT NULL,
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_invoices_tenant ON invoice_records(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoice_records(status, due_date);

-- Plans par défaut
INSERT INTO subscription_plans (id, code, name, description, price_monthly, price_yearly, max_rules, max_members, max_events_per_month, referral_enabled, campaigns_enabled, promo_codes_enabled, analytics_enabled)
VALUES
  (gen_random_uuid(), 'FREE',       'Gratuit',     'Plan de démarrage',    0,      0,       5,   1000,  10000,  false, false, false, false),
  (gen_random_uuid(), 'PRO',        'Pro',          'Pour les PME',         9900,   99000,   50,  50000, 500000, true,  true,  true,  true),
  (gen_random_uuid(), 'ENTERPRISE', 'Enterprise',  'Sans limites',         49900,  499000,  -1,  0,     0,      true,  true,  true,  true)
ON CONFLICT (code) DO NOTHING;
