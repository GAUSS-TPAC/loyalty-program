--liquibase formatted sql

--changeset yowyob:007-add-optimistic-locking-columns
-- ============================================================
-- V007 — Ajout des colonnes de version pour l'optimistic locking
-- Requises par @Version de Spring Data R2DBC sur WalletEntity
-- et PaymentRequestEntity.
-- ============================================================

-- Colonne version sur wallets (WalletEntity @Version)
ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Colonne version sur payment_requests (PaymentRequestEntity @Version)
ALTER TABLE payment_requests
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN wallets.version
    IS 'Version pour l''optimistic locking R2DBC. Incrémentée à chaque UPDATE pour prévenir les conflits concurrents sur le solde.';

COMMENT ON COLUMN payment_requests.version
    IS 'Version pour l''optimistic locking R2DBC. Prévient les mises à jour concurrentes lors de la réception simultanée de webhooks.';
