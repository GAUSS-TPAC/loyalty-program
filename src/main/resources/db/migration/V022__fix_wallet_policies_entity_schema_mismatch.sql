-- ============================================================
-- V022 — Aligne wallet_policies sur WalletPolicyEntity/domaine WalletPolicy
--
-- WalletPolicyEntity (et le domaine WalletPolicy associé) utilisent des
-- noms de colonnes anglais (currency_name, daily_spend_cap, max_balance,
-- otp_threshold, ...) qui ne correspondent à aucune colonne de la table
-- créée par V006 (noms français : depense_max_journaliere, solde_maximum,
-- seuil_challenge_otp, ...). Résultat : la moindre lecture de
-- wallet_policies échouait avec "column ... does not exist", ce qui
-- cassait WalletPolicyRepositoryAdapter.findByTenant (utilisé à chaque
-- crédit/débit de wallet pour appliquer les limites), avant même d'avoir
-- pu retomber sur WalletPolicy.defaults().
-- ============================================================

ALTER TABLE wallet_policies ALTER COLUMN name SET DEFAULT 'Default';

ALTER TABLE wallet_policies RENAME COLUMN recharge_max_par_operation TO max_topup_per_txn;
ALTER TABLE wallet_policies RENAME COLUMN solde_maximum TO max_balance;
ALTER TABLE wallet_policies RENAME COLUMN depense_max_journaliere TO daily_spend_cap;
ALTER TABLE wallet_policies RENAME COLUMN seuil_challenge_otp TO otp_threshold;
ALTER TABLE wallet_policies RENAME COLUMN retrait_max_par_operation TO min_withdrawal;
ALTER TABLE wallet_policies RENAME COLUMN kyc_required_at_enrollment TO kyc_required;

-- delai_minimum_avant_retrait_s (BIGINT secondes) -> withdrawal_delay_hours (INTEGER heures)
ALTER TABLE wallet_policies RENAME COLUMN delai_minimum_avant_retrait_s TO withdrawal_delay_hours;
ALTER TABLE wallet_policies
    ALTER COLUMN withdrawal_delay_hours TYPE INTEGER USING (withdrawal_delay_hours / 3600)::integer;

-- loyalty_points_expiration_s (BIGINT secondes) -> expiry_days (INTEGER jours)
ALTER TABLE wallet_policies RENAME COLUMN loyalty_points_expiration_s TO expiry_days;
ALTER TABLE wallet_policies
    ALTER COLUMN expiry_days TYPE INTEGER USING (expiry_days / 86400)::integer;

-- Champs du domaine sans colonne correspondante (currency_name/symbol =
-- libellé de la monnaie de fidélité, distinct de wallets.currency_code
-- qui est le code ISO 4217 réel — currency ISO reste inchangée, inutilisée).
ALTER TABLE wallet_policies ADD COLUMN IF NOT EXISTS exchange_rate NUMERIC(19, 6) NOT NULL DEFAULT 1.0;
ALTER TABLE wallet_policies ADD COLUMN IF NOT EXISTS currency_name VARCHAR(50) NOT NULL DEFAULT 'Credits';
ALTER TABLE wallet_policies ADD COLUMN IF NOT EXISTS currency_symbol VARCHAR(10) NOT NULL DEFAULT 'CR';
