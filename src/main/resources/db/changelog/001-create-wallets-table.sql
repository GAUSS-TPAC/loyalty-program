--liquibase formatted sql

--changeset yowyob:001-create-wallets-table splitStatements:false
-- ============================================================
-- V001 — Table wallets
-- Agrégat racine : un wallet par membre par tenant.
-- ============================================================

-- Types ENUM PostgreSQL (idempotents via DO $$)
DO $$ BEGIN
    CREATE TYPE wallet_status AS ENUM (
        'PENDING_KYC',
        'ACTIVE',
        'FROZEN',
        'CLOSED'
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ── Table principale ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS wallets (

    -- Identité
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    member_id           UUID            NOT NULL,
    tenant_id           UUID            NOT NULL,

    -- Soldes (valeurs monétaires avec précision financière)
    -- NUMERIC(19,4) → jusqu'à 999 999 999 999 999,9999 unités
    available_balance   NUMERIC(19, 4)  NOT NULL DEFAULT 0.0000,
    reserved_balance    NUMERIC(19, 4)  NOT NULL DEFAULT 0.0000,
    expiring_balance    NUMERIC(19, 4)  NOT NULL DEFAULT 0.0000,

    -- État & politique
    status              wallet_status   NOT NULL DEFAULT 'PENDING_KYC',
    wallet_policy_id    UUID            NOT NULL,

    -- KYC
    kyc_validated       BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Gel
    freeze_reason       TEXT,
    frozen_at           TIMESTAMPTZ,

    -- Clôture
    closed_at           TIMESTAMPTZ,

    -- Horodatages
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Contraintes
    CONSTRAINT wallets_pkey                     PRIMARY KEY (id),
    CONSTRAINT wallets_available_balance_nn     CHECK (available_balance >= 0),
    CONSTRAINT wallets_reserved_balance_nn      CHECK (reserved_balance  >= 0),
    CONSTRAINT wallets_expiring_balance_nn      CHECK (expiring_balance  >= 0),
    CONSTRAINT wallets_freeze_reason_check      CHECK (
        (status = 'FROZEN' AND freeze_reason IS NOT NULL)
        OR status != 'FROZEN'
    ),
    CONSTRAINT wallets_frozen_at_check          CHECK (
        (status = 'FROZEN' AND frozen_at IS NOT NULL)
        OR status != 'FROZEN'
    ),
    CONSTRAINT wallets_closed_at_check          CHECK (
        (status = 'CLOSED' AND closed_at IS NOT NULL)
        OR status != 'CLOSED'
    )
);

-- ── Contrainte d'unicité : un seul wallet par membre et par tenant ─
CREATE UNIQUE INDEX IF NOT EXISTS uq_wallets_member_tenant
    ON wallets (member_id, tenant_id);

-- ── Trigger : mise à jour automatique de updated_at ───────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER wallets_set_updated_at
    BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── Commentaires de documentation ────────────────────────────
COMMENT ON TABLE  wallets                   IS 'Agrégat racine Wallet : porte-monnaie électronique d''un membre au sein d''un tenant.';
COMMENT ON COLUMN wallets.available_balance IS 'Solde immédiatement utilisable (crédits - débits - réservations).';
COMMENT ON COLUMN wallets.reserved_balance  IS 'Montant immobilisé pendant un traitement asynchrone (retrait en cours).';
COMMENT ON COLUMN wallets.expiring_balance  IS 'Montant de points loyalty expirant prochainement (indicatif).';
COMMENT ON COLUMN wallets.wallet_policy_id  IS 'Référence vers wallet_policies (règles et limites du tenant).';
COMMENT ON COLUMN wallets.kyc_validated     IS 'KYC validé : requis pour les retraits vers Mobile Money.';
COMMENT ON COLUMN wallets.freeze_reason     IS 'Motif du gel (obligatoire quand status=FROZEN).';
