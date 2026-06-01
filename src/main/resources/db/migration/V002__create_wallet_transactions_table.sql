-- ============================================================
-- V002 — Table wallet_transactions
-- Journal immuable (append-only) de toutes les opérations.
-- ============================================================

-- Types ENUM PostgreSQL
DO $$ BEGIN
    CREATE TYPE transaction_type AS ENUM (
        'CREDIT',
        'DEBIT',
        'REVERSAL'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE transaction_source AS ENUM (
        'TOPUP_MTN',
        'TOPUP_ORANGE',
        'TOPUP_STRIPE',
        'LOYALTY_REWARD',
        'CASHBACK',
        'BONUS',
        'PURCHASE',
        'WITHDRAWAL_MTN',
        'WITHDRAWAL_ORANGE',
        'MANUAL_ADJUSTMENT',
        'REFUND'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE transaction_status AS ENUM (
        'PENDING',
        'COMPLETED',
        'FAILED',
        'REVERSED',
        'RESERVED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── Table principale ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS wallet_transactions (

    -- Identité
    id                      UUID                NOT NULL DEFAULT gen_random_uuid(),
    wallet_id               UUID                NOT NULL,
    tenant_id               UUID                NOT NULL,

    -- Idempotence (évite les doubles crédits/débits)
    idempotency_key         VARCHAR(255)        NOT NULL,

    -- Nature
    type                    transaction_type    NOT NULL,
    source                  transaction_source  NOT NULL,
    status                  transaction_status  NOT NULL DEFAULT 'PENDING',

    -- Montant & devise
    amount                  NUMERIC(19, 4)      NOT NULL,
    currency                VARCHAR(3)          NOT NULL,           -- ISO 4217 : XAF, EUR, USD…
    balance_after           NUMERIC(19, 4)      NOT NULL,           -- Snapshot solde post-opération

    -- Liens
    payment_request_id      UUID,                                    -- NULL pour crédits internes
    original_transaction_id UUID,                                    -- Renseigné uniquement pour REVERSAL

    -- Contexte
    description             TEXT,
    metadata                TEXT,                                    -- JSON libre (règle IA, campagne…)

    -- Horodatages
    created_at              TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMPTZ,                             -- NULL tant que PENDING

    -- Contraintes
    CONSTRAINT wt_pkey                          PRIMARY KEY (id),
    CONSTRAINT wt_amount_positive               CHECK (amount > 0),
    CONSTRAINT wt_balance_after_nn              CHECK (balance_after >= 0),
    CONSTRAINT wt_currency_length               CHECK (char_length(currency) = 3),
    CONSTRAINT wt_reversal_needs_original       CHECK (
        (type = 'REVERSAL' AND original_transaction_id IS NOT NULL)
        OR type != 'REVERSAL'
    ),
    CONSTRAINT wt_completed_at_terminal         CHECK (
        (status IN ('COMPLETED', 'FAILED', 'REVERSED') AND completed_at IS NOT NULL)
        OR status NOT IN ('COMPLETED', 'FAILED', 'REVERSED')
    ),

    -- Clé étrangère vers wallets
    CONSTRAINT wt_fk_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets(id)
        ON DELETE RESTRICT,

    -- Lien auto-référentiel pour REVERSAL → transaction originale
    CONSTRAINT wt_fk_original_transaction
        FOREIGN KEY (original_transaction_id) REFERENCES wallet_transactions(id)
        ON DELETE RESTRICT
);

-- ── Contrainte d'unicité sur la clé d'idempotence par tenant ─
CREATE UNIQUE INDEX IF NOT EXISTS uq_wt_idempotency_key_tenant
    ON wallet_transactions (idempotency_key, tenant_id);

-- ── Commentaires ─────────────────────────────────────────────
COMMENT ON TABLE  wallet_transactions                   IS 'Journal immuable append-only de toutes les opérations wallet. Jamais mis à jour ni supprimé.';
COMMENT ON COLUMN wallet_transactions.idempotency_key   IS 'Clé d''idempotence unique par tenant — évite le double-traitement.';
COMMENT ON COLUMN wallet_transactions.balance_after     IS 'Snapshot du solde disponible juste après cette transaction (réconciliation rapide).';
COMMENT ON COLUMN wallet_transactions.original_transaction_id IS 'ID de la transaction annulée (obligatoire pour type=REVERSAL).';
COMMENT ON COLUMN wallet_transactions.metadata          IS 'Métadonnées JSON libres : règle IA appliquée, paramètres de campagne, etc.';
