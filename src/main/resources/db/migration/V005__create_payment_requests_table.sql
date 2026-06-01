CREATE TABLE IF NOT EXISTS payment_requests (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_transaction_id UUID          NOT NULL REFERENCES wallet_transactions(id),
    provider              VARCHAR(20)   NOT NULL,
    external_ref          VARCHAR(255),
    status                VARCHAR(20)   NOT NULL DEFAULT 'INITIATED',
    real_amount           NUMERIC(20,2) NOT NULL,
    real_currency         VARCHAR(10)   NOT NULL,
    virtual_amount        NUMERIC(20,2) NOT NULL,
    exchange_rate         NUMERIC(10,6) NOT NULL DEFAULT 1.0,
    raw_response          TEXT,
    initiated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    confirmed_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_payment_req_tx_id      ON payment_requests(wallet_transaction_id);
CREATE INDEX IF NOT EXISTS idx_payment_req_ext_ref    ON payment_requests(external_ref);
CREATE INDEX IF NOT EXISTS idx_payment_req_status     ON payment_requests(status);
