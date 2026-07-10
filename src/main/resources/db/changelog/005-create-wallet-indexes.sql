--liquibase formatted sql

--changeset yowyob:005-create-wallet-indexes
-- ============================================================
-- V005 — Index de performance
-- Tous les index applicatifs séparés des tables (DDL propre).
-- ============================================================

-- ════════════════════════════════════════════════════════════
-- TABLE : wallets
-- ════════════════════════════════════════════════════════════

-- Recherche par tenant (liste de wallets d'un tenant, pagination admin)
CREATE INDEX IF NOT EXISTS idx_wallets_tenant_id
    ON wallets (tenant_id);

-- Recherche par statut pour les opérations batch (ex. wallets FROZEN à purger)
CREATE INDEX IF NOT EXISTS idx_wallets_status
    ON wallets (status, tenant_id);

-- Wallets dont le KYC n'est pas encore validé (monitoring onboarding)
CREATE INDEX IF NOT EXISTS idx_wallets_pending_kyc
    ON wallets (tenant_id, kyc_validated)
    WHERE status = 'PENDING_KYC' AND kyc_validated = FALSE;

-- Tri par date de création (liste des nouveaux membres)
CREATE INDEX IF NOT EXISTS idx_wallets_created_at
    ON wallets (tenant_id, created_at DESC);

-- ════════════════════════════════════════════════════════════
-- TABLE : wallet_transactions
-- ════════════════════════════════════════════════════════════

-- Index principal : historique d'un wallet trié par date (F6 — GetTransactionHistory)
CREATE INDEX IF NOT EXISTS idx_wt_wallet_created_at
    ON wallet_transactions (wallet_id, tenant_id, created_at DESC);

-- Filtre par type (CREDIT/DEBIT/REVERSAL)
CREATE INDEX IF NOT EXISTS idx_wt_wallet_type
    ON wallet_transactions (wallet_id, tenant_id, type, created_at DESC);

-- Filtre par source (TOPUP_MTN, PURCHASE, LOYALTY_REWARD…)
CREATE INDEX IF NOT EXISTS idx_wt_wallet_source
    ON wallet_transactions (wallet_id, tenant_id, source, created_at DESC);

-- Filtre par statut (PENDING, COMPLETED, FAILED…)
CREATE INDEX IF NOT EXISTS idx_wt_wallet_status
    ON wallet_transactions (wallet_id, tenant_id, status, created_at DESC);

-- Calcul rapide des débits sur une fenêtre glissante 24h
-- (F3 — vérification plafond journalier depense_max_journaliere)
CREATE INDEX IF NOT EXISTS idx_wt_debit_since
    ON wallet_transactions (wallet_id, tenant_id, created_at)
    WHERE type = 'DEBIT' AND status = 'COMPLETED';

-- Lookup par PaymentRequest (jointure lors de la confirmation webhook)
CREATE INDEX IF NOT EXISTS idx_wt_payment_request_id
    ON wallet_transactions (payment_request_id)
    WHERE payment_request_id IS NOT NULL;

-- Réconciliation : somme des transactions COMPLETED d'un wallet
CREATE INDEX IF NOT EXISTS idx_wt_reconciliation
    ON wallet_transactions (wallet_id, tenant_id, status, type)
    WHERE status = 'COMPLETED';

-- Détection de fraude : débits rapides (F10)
-- Requête : SELECT COUNT(*) WHERE wallet_id=? AND type='DEBIT' AND created_at > NOW() - INTERVAL '5 minutes'
CREATE INDEX IF NOT EXISTS idx_wt_fraud_rapid_debits
    ON wallet_transactions (wallet_id, type, created_at DESC)
    WHERE type = 'DEBIT' AND status IN ('COMPLETED', 'PENDING');

-- ════════════════════════════════════════════════════════════
-- TABLE : payment_requests
-- ════════════════════════════════════════════════════════════

-- Recherche par wallet (liste des paiements d'un membre)
CREATE INDEX IF NOT EXISTS idx_pr_wallet_id
    ON payment_requests (wallet_id, tenant_id, created_at DESC);

-- Recherche par provider (reporting, monitoring)
CREATE INDEX IF NOT EXISTS idx_pr_provider_status
    ON payment_requests (provider, status, created_at DESC);

-- ════════════════════════════════════════════════════════════
-- TABLE : wallet_audit_logs
-- ════════════════════════════════════════════════════════════

-- Historique d'audit d'un wallet trié par date décroissante (admin API)
CREATE INDEX IF NOT EXISTS idx_wal_wallet_occurred_at
    ON wallet_audit_logs (wallet_id, tenant_id, occurred_at DESC);

-- Filtre par type d'action (ex. lister tous les FREEZE d'un tenant)
CREATE INDEX IF NOT EXISTS idx_wal_action
    ON wallet_audit_logs (tenant_id, action, occurred_at DESC);

-- Filtre par acteur (tracer toutes les actions d'un admin donné)
CREATE INDEX IF NOT EXISTS idx_wal_actor
    ON wallet_audit_logs (actor_id, tenant_id, occurred_at DESC);
