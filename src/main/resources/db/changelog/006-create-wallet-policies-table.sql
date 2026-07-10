--liquibase formatted sql

--changeset yowyob:006-create-wallet-policies-table
-- ============================================================
-- V006 — Table wallet_policies
-- Configuration des règles et limites par tenant.
-- Référencée par wallets.wallet_policy_id (FK ajoutée ici
-- via ALTER TABLE pour éviter la dépendance circulaire).
-- ============================================================

CREATE TABLE IF NOT EXISTS wallet_policies (

    -- Identité
    id                              UUID            NOT NULL DEFAULT gen_random_uuid(),
    tenant_id                       UUID            NOT NULL,
    name                            VARCHAR(100)    NOT NULL,       -- Ex. "Standard", "VIP", "Beta"

    -- Limites de recharge
    recharge_max_par_operation      NUMERIC(19, 4),                 -- NULL = pas de limite par opération
    solde_maximum                   NUMERIC(19, 4),                 -- NULL = pas de plafond

    -- Limites de débit
    depense_max_journaliere         NUMERIC(19, 4),                 -- NULL = pas de plafond journalier
    seuil_challenge_otp             NUMERIC(19, 4),                 -- NULL = jamais de challenge OTP

    -- Paramètres de retrait
    delai_minimum_avant_retrait_s   BIGINT,                         -- Durée en secondes (NULL = pas de délai)
    retrait_max_par_operation       NUMERIC(19, 4),                 -- NULL = pas de limite par retrait

    -- Onboarding
    kyc_required_at_enrollment      BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Fidélité
    currency                        VARCHAR(3)      NOT NULL DEFAULT 'XAF',   -- ISO 4217
    loyalty_points_expiration_s     BIGINT,                         -- Durée validité points en secondes (NULL = jamais)

    -- État de la politique
    is_default                      BOOLEAN         NOT NULL DEFAULT FALSE,    -- Une seule politique par défaut par tenant
    is_active                       BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Horodatages
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Contraintes
    CONSTRAINT wp_pkey                              PRIMARY KEY (id),
    CONSTRAINT wp_name_not_empty                    CHECK (char_length(trim(name)) > 0),
    CONSTRAINT wp_currency_length                   CHECK (char_length(currency) = 3),
    CONSTRAINT wp_recharge_max_positive             CHECK (recharge_max_par_operation IS NULL OR recharge_max_par_operation > 0),
    CONSTRAINT wp_solde_maximum_positive            CHECK (solde_maximum IS NULL OR solde_maximum > 0),
    CONSTRAINT wp_depense_max_positive              CHECK (depense_max_journaliere IS NULL OR depense_max_journaliere > 0),
    CONSTRAINT wp_retrait_max_positive              CHECK (retrait_max_par_operation IS NULL OR retrait_max_par_operation > 0),
    CONSTRAINT wp_seuil_otp_positive                CHECK (seuil_challenge_otp IS NULL OR seuil_challenge_otp > 0),
    CONSTRAINT wp_delai_retrait_positive            CHECK (delai_minimum_avant_retrait_s IS NULL OR delai_minimum_avant_retrait_s >= 0)
);

-- ── Une seule politique par défaut par tenant ────────────────
CREATE UNIQUE INDEX IF NOT EXISTS uq_wp_default_per_tenant
    ON wallet_policies (tenant_id)
    WHERE is_default = TRUE;

-- ── Index recherche par tenant ────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_wp_tenant_active
    ON wallet_policies (tenant_id, is_active);

-- ── Trigger updated_at ───────────────────────────────────────
CREATE TRIGGER wallet_policies_set_updated_at
    BEFORE UPDATE ON wallet_policies
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ── Ajout de la FK wallets → wallet_policies (rétroactif) ────
-- La table wallet_policies n'existait pas lors de V001.
-- On ajoute la contrainte maintenant que les deux tables existent.
ALTER TABLE wallets
    ADD CONSTRAINT wallets_fk_wallet_policy
        FOREIGN KEY (wallet_policy_id) REFERENCES wallet_policies(id)
        ON DELETE RESTRICT;

-- ── Commentaires ─────────────────────────────────────────────
COMMENT ON TABLE  wallet_policies                               IS 'Politique de wallet par tenant : limites de recharge, débit, retrait, seuils OTP, devise, expiration points.';
COMMENT ON COLUMN wallet_policies.recharge_max_par_operation   IS 'Montant maximum par opération de recharge. NULL = illimité.';
COMMENT ON COLUMN wallet_policies.solde_maximum                IS 'Solde total maximum autorisé sur le wallet. NULL = illimité.';
COMMENT ON COLUMN wallet_policies.depense_max_journaliere      IS 'Plafond de dépenses sur une fenêtre glissante de 24h. NULL = illimité.';
COMMENT ON COLUMN wallet_policies.seuil_challenge_otp          IS 'Montant à partir duquel un challenge OTP est requis avant débit. NULL = jamais.';
COMMENT ON COLUMN wallet_policies.delai_minimum_avant_retrait_s IS 'Délai minimum en secondes entre un crédit et un retrait (anti dépôt-retrait immédiat).';
COMMENT ON COLUMN wallet_policies.loyalty_points_expiration_s  IS 'Durée de validité des points loyalty en secondes. NULL = pas d''expiration.';
COMMENT ON COLUMN wallet_policies.is_default                   IS 'Politique appliquée aux nouveaux wallets du tenant. Unique par tenant.';
