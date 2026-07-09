-- ============================================================
-- V018 — Aligne la table wallets sur WalletEntity/domaine Wallet
--
-- WalletEntity mappait vers des colonnes "balance"/"currency_code" qui
-- n'ont jamais existé (le modèle de domaine utilise un solde unique,
-- pas le triptyque available/reserved/expiring de V001), et wallet_policy_id
-- était NOT NULL sans qu'aucun code n'assigne de politique à la création
-- (WalletPolicyRepositoryAdapter retombe déjà sur WalletPolicy.defaults()
-- quand aucune ligne n'existe pour le tenant).
-- ============================================================

ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) NOT NULL DEFAULT 'XAF';

ALTER TABLE wallets
    ALTER COLUMN wallet_policy_id DROP NOT NULL;

COMMENT ON COLUMN wallets.currency_code IS 'Devise du wallet (ISO 4217). Requis par le modèle de domaine Wallet.';
