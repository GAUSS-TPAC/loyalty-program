-- ============================================================
-- V003 — Table payment_requests
-- Suivi des interactions asynchrones avec les providers externes
-- (MTN Mobile Money, Orange Money, Stripe).
-- ============================================================

-- Types ENUM PostgreSQL
DO $$ BEGIN
    CREATE TYPE payment_provider AS ENUM (
        'MTN',
        'ORANGE',
        'STRIPE',
        'INTERNAL'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_direction AS ENUM (
        'INBOUND',
        'OUTBOUND'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_request_status AS ENUM (
        'INITIATED',
        'PROCESSING',
        'CONFIRMED',
        'FAILED',
        'TIMEOUT',
        'CANCELLED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── Table principale ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payment_requests (

    -- Identité
    id                          UUID                        NOT NULL DEFAULT gen_random_uuid(),
    wallet_id                   UUID                        NOT NULL,
    tenant_id                   UUID                        NOT NULL,
    wallet_transaction_id       UUID                        NOT NULL,   -- Transaction liée (statut PENDING)

    -- Provider & direction
    provider                    payment_provider            NOT NULL,
    direction                   payment_direction           NOT NULL,

    -- Montant
    amount                      NUMERIC(19, 4)              NOT NULL,
    currency                    VARCHAR(3)                  NOT NULL,

    -- Identifiants provider
    external_reference          VARCHAR(512),                           -- Retourné par le provider après initiation
    mobile_money_phone_number   VARCHAR(20),                            -- Pour MTN/Orange (E.164 : +237XXXXXXXXX)

    -- État & retry
    status                      payment_request_status      NOT NULL DEFAULT 'INITIATED',
    retry_count                 SMALLINT                    NOT NULL DEFAULT 0,
    max_retries                 SMALLINT                    NOT NULL DEFAULT 3,

    -- Horodatages
    created_at                  TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    next_retry_at               TIMESTAMPTZ,                            -- Planification du prochain retry
    expires_at                  TIMESTAMPTZ,                            -- Timeout si pas de webhook avant cette date
    resolved_at                 TIMESTAMPTZ,                            -- Date de résolution finale

    -- Réponse provider
    provider_error_message      TEXT,                                   -- Message d'erreur du provider (si FAILED)
    webhook_payload             TEXT,                                   -- Payload brut du webhook reçu (audit)

    -- Contraintes
    CONSTRAINT pr_pkey                          PRIMARY KEY (id),
    CONSTRAINT pr_amount_positive               CHECK (amount > 0),
    CONSTRAINT pr_currency_length               CHECK (char_length(currency) = 3),
    CONSTRAINT pr_retry_count_valid             CHECK (retry_count >= 0 AND retry_count <= max_retries),
    CONSTRAINT pr_mobile_money_required         CHECK (
        (provider IN ('MTN', 'ORANGE') AND mobile_money_phone_number IS NOT NULL)
        OR provider NOT IN ('MTN', 'ORANGE')
    ),
    CONSTRAINT pr_resolved_at_terminal          CHECK (
        (status IN ('CONFIRMED', 'FAILED', 'CANCELLED') AND resolved_at IS NOT NULL)
        OR status NOT IN ('CONFIRMED', 'FAILED', 'CANCELLED')
    ),

    -- Clés étrangères
    CONSTRAINT pr_fk_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets(id)
        ON DELETE RESTRICT,

    CONSTRAINT pr_fk_wallet_transaction
        FOREIGN KEY (wallet_transaction_id) REFERENCES wallet_transactions(id)
        ON DELETE RESTRICT
);

-- ── Index pour le rapprochement webhook ─────────────────────
-- Utilisé par findByExternalReference() lors de la réception d'un webhook
CREATE INDEX IF NOT EXISTS idx_pr_external_reference
    ON payment_requests (external_reference, provider)
    WHERE external_reference IS NOT NULL;

-- ── Index pour findActiveByWalletId() ───────────────────────
-- Utilisé avant clôture d'un wallet (vérification pas de requête en cours)
CREATE INDEX IF NOT EXISTS idx_pr_wallet_active
    ON payment_requests (wallet_id, tenant_id, status)
    WHERE status IN ('INITIATED', 'PROCESSING');

-- ── Index pour le job de retry planifié ─────────────────────
CREATE INDEX IF NOT EXISTS idx_pr_retry_pending
    ON payment_requests (next_retry_at, retry_count)
    WHERE status = 'TIMEOUT';

-- ── Commentaires ─────────────────────────────────────────────
COMMENT ON TABLE  payment_requests                          IS 'Suivi des demandes de paiement vers les providers externes (MTN, Orange, Stripe). Cycle de vie asynchrone via webhooks.';
COMMENT ON COLUMN payment_requests.external_reference       IS 'Référence unique retournée par le provider. Clé de rapprochement pour les webhooks entrants.';
COMMENT ON COLUMN payment_requests.mobile_money_phone_number IS 'Numéro Mobile Money en format E.164 (ex. +237XXXXXXXXX). Obligatoire pour MTN et Orange.';
COMMENT ON COLUMN payment_requests.retry_count              IS 'Nombre de tentatives effectuées. Ne dépasse jamais max_retries.';
COMMENT ON COLUMN payment_requests.webhook_payload          IS 'Payload JSON brut reçu du provider, conservé pour audit et replay éventuel.';
