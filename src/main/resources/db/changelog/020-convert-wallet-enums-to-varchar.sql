--liquibase formatted sql

--changeset yowyob:020-convert-wallet-enums-to-varchar
-- ============================================================
-- V020 — Convertit les colonnes ENUM PostgreSQL natives de wallets/
-- wallet_transactions en VARCHAR.
--
-- wallets.status / wallet_transactions.{type,source,status} étaient les
-- SEULES colonnes de statut de tout le schéma utilisant un type ENUM
-- PostgreSQL natif (wallet_status/transaction_type/transaction_source/
-- transaction_status) : R2DBC ne caste pas automatiquement un bind
-- parameter String vers un type enum natif ("column is of type X but
-- expression is of type character varying"). Toutes les autres tables
-- (loyalty, referral, tenant, campaign, webhook, subscription, reward)
-- utilisent déjà VARCHAR pour ce même besoin — on aligne wallet dessus.
--
-- Bonus : transaction_source (ENUM) et TransactionSource (enum Java du
-- domaine) avaient divergé (BONUS/WITHDRAWAL_MTN/WITHDRAWAL_ORANGE/REFUND
-- côté DB vs REFERRAL_BONUS/CAMPAIGN_BONUS/WITHDRAWAL/REVERSAL côté Java) ;
-- VARCHAR supprime cette double source de vérité.
--
-- Les CHECK constraints existantes référencent des littéraux résolus au
-- type enum d'origine ; il faut les supprimer avant l'ALTER COLUMN TYPE
-- et les recréer après. Chaque sous-action (DROP DEFAULT / TYPE / SET
-- DEFAULT) est en outre passée en instruction ALTER TABLE séparée :
-- Postgres échoue sinon avec "operator does not exist: character
-- varying = wallet_status" en essayant de valider le nouveau DEFAULT
-- avant que le changement de type ne soit pleinement acté.
-- ============================================================

-- IF EXISTS : rend le changeset rejouable sur une base où la conversion a déjà
-- eu lieu (ex. schéma hérité de Flyway) — les paires DROP/ADD deviennent idempotentes.
ALTER TABLE wallets DROP CONSTRAINT IF EXISTS wallets_freeze_reason_check;
ALTER TABLE wallets DROP CONSTRAINT IF EXISTS wallets_frozen_at_check;
ALTER TABLE wallets DROP CONSTRAINT IF EXISTS wallets_closed_at_check;

-- Index partiels : leur prédicat WHERE fige un littéral résolu au type
-- enum d'origine au moment de la création de l'index.
DROP INDEX IF EXISTS idx_wallets_pending_kyc;

ALTER TABLE wallets ALTER COLUMN status DROP DEFAULT;
ALTER TABLE wallets ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE wallets ALTER COLUMN status SET DEFAULT 'PENDING_KYC';

ALTER TABLE wallets ADD CONSTRAINT wallets_freeze_reason_check CHECK (
    (status = 'FROZEN' AND freeze_reason IS NOT NULL)
    OR status != 'FROZEN'
);
ALTER TABLE wallets ADD CONSTRAINT wallets_frozen_at_check CHECK (
    (status = 'FROZEN' AND frozen_at IS NOT NULL)
    OR status != 'FROZEN'
);
ALTER TABLE wallets ADD CONSTRAINT wallets_closed_at_check CHECK (
    (status = 'CLOSED' AND closed_at IS NOT NULL)
    OR status != 'CLOSED'
);

CREATE INDEX IF NOT EXISTS idx_wallets_pending_kyc
    ON wallets (tenant_id, kyc_validated)
    WHERE status = 'PENDING_KYC' AND kyc_validated = FALSE;

ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS wt_reversal_needs_original;
ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS wt_completed_at_terminal;

DROP INDEX IF EXISTS idx_wt_debit_since;
DROP INDEX IF EXISTS idx_wt_reconciliation;
DROP INDEX IF EXISTS idx_wt_fraud_rapid_debits;

ALTER TABLE wallet_transactions ALTER COLUMN type TYPE VARCHAR(20) USING type::text;
ALTER TABLE wallet_transactions ALTER COLUMN source TYPE VARCHAR(30) USING source::text;
ALTER TABLE wallet_transactions ALTER COLUMN status DROP DEFAULT;
ALTER TABLE wallet_transactions ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE wallet_transactions ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE wallet_transactions ADD CONSTRAINT wt_reversal_needs_original CHECK (
    (type = 'REVERSAL' AND original_transaction_id IS NOT NULL)
    OR type != 'REVERSAL'
);
ALTER TABLE wallet_transactions ADD CONSTRAINT wt_completed_at_terminal CHECK (
    (status IN ('COMPLETED', 'FAILED', 'REVERSED') AND completed_at IS NOT NULL)
    OR status NOT IN ('COMPLETED', 'FAILED', 'REVERSED')
);

CREATE INDEX IF NOT EXISTS idx_wt_debit_since
    ON wallet_transactions (wallet_id, tenant_id, created_at)
    WHERE type = 'DEBIT' AND status = 'COMPLETED';

CREATE INDEX IF NOT EXISTS idx_wt_reconciliation
    ON wallet_transactions (wallet_id, tenant_id, status, type)
    WHERE status = 'COMPLETED';

CREATE INDEX IF NOT EXISTS idx_wt_fraud_rapid_debits
    ON wallet_transactions (wallet_id, type, created_at DESC)
    WHERE type = 'DEBIT' AND status IN ('COMPLETED', 'PENDING');

DROP TYPE IF EXISTS wallet_status;
DROP TYPE IF EXISTS transaction_type;
DROP TYPE IF EXISTS transaction_source;
DROP TYPE IF EXISTS transaction_status;
