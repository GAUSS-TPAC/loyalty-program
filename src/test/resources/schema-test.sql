CREATE TABLE IF NOT EXISTS tenants (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    plan        VARCHAR(50) NOT NULL,
    config      VARCHAR(4000) DEFAULT '{}',
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS wallet_policies (
    id          UUID PRIMARY KEY,
    tenant_id   UUID NOT NULL,
    name        VARCHAR(255),
    config      VARCHAR(4000) DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS wallets (
    id                  UUID PRIMARY KEY,
    member_id           UUID NOT NULL,
    tenant_id           UUID NOT NULL,
    available_balance   DECIMAL(19, 4) NOT NULL DEFAULT 0,
    reserved_balance    DECIMAL(19, 4) NOT NULL DEFAULT 0,
    expiring_balance    DECIMAL(19, 4) NOT NULL DEFAULT 0,
    status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    wallet_policy_id    UUID NOT NULL,
    kyc_validated       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id               UUID PRIMARY KEY,
    wallet_id        UUID NOT NULL,
    tenant_id        UUID NOT NULL,
    type             VARCHAR(50) NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL,
    balance_before   DECIMAL(19, 4) NOT NULL,
    balance_after    DECIMAL(19, 4) NOT NULL,
    status           VARCHAR(50) NOT NULL,
    source           VARCHAR(50) NOT NULL,
    idempotency_key  VARCHAR(255),
    reference_id     UUID,
    reversal_of      UUID,
    metadata         VARCHAR(4000) DEFAULT '{}',
    created_at       TIMESTAMP
);
