CREATE TABLE IF NOT EXISTS wallet_transactions (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id              UUID          NOT NULL REFERENCES wallets(id),
    tenant_id              UUID          NOT NULL,
    type                   VARCHAR(20)   NOT NULL,
    amount                 NUMERIC(20,2) NOT NULL,
    currency               VARCHAR(10)   NOT NULL,
    status                 VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED',
    source                 VARCHAR(50)   NOT NULL,
    idempotency_key        VARCHAR(255),
    balance_before         NUMERIC(20,2) NOT NULL,
    balance_after          NUMERIC(20,2) NOT NULL,
    related_transaction_id UUID,
    metadata               JSONB,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_wallet_tx_idempotency
    ON wallet_transactions(idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_wallet_tx_wallet_id  ON wallet_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_created_at ON wallet_transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_type       ON wallet_transactions(type);
