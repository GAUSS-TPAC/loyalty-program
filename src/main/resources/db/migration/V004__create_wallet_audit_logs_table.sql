-- ============================================================
-- V004 — Table wallet_audit_logs
-- Piste d'audit immuable pour toutes les actions sensibles.
-- Jamais mise à jour ni supprimée (append-only).
-- ============================================================

CREATE TABLE IF NOT EXISTS wallet_audit_logs (

    -- Identité
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    wallet_id               UUID            NOT NULL,
    tenant_id               UUID            NOT NULL,

    -- Acteur
    actor_id                VARCHAR(255)    NOT NULL,    -- UUID admin, "SYSTEM", ID service IA…
    actor_type              VARCHAR(50)     NOT NULL,    -- 'ADMIN' | 'SYSTEM' | 'AI_SERVICE' | 'MEMBER'

    -- Action
    action                  VARCHAR(100)    NOT NULL,    -- 'FREEZE' | 'UNFREEZE' | 'CLOSE' | 'FRAUD_DETECTED' | etc.
    reason                  TEXT            NOT NULL,    -- Motif obligatoire pour toute action sensible

    -- Contexte de transition
    previous_status         VARCHAR(50),                 -- Statut du wallet AVANT l'action
    new_status              VARCHAR(50),                 -- Statut du wallet APRÈS l'action

    -- Lien vers transaction (si applicable)
    related_transaction_id  UUID,                        -- NULL pour les actions purement administratives

    -- Métadonnées libres
    metadata                TEXT,                        -- JSON : contexte IA, paramètres de fraude…

    -- Contexte réseau
    ip_address              INET,                        -- Adresse IP de l'acteur (type PostgreSQL INET)
    user_agent              TEXT,                        -- User-Agent HTTP

    -- Horodatage (non modifiable)
    occurred_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Contraintes
    CONSTRAINT wal_pkey                     PRIMARY KEY (id),
    CONSTRAINT wal_actor_type_check         CHECK (
        actor_type IN ('ADMIN', 'SYSTEM', 'AI_SERVICE', 'MEMBER')
    ),
    CONSTRAINT wal_action_not_empty         CHECK (char_length(trim(action)) > 0),
    CONSTRAINT wal_reason_not_empty         CHECK (char_length(trim(reason)) > 0),

    -- Clé étrangère souple vers wallets
    -- ON DELETE RESTRICT : un wallet avec des logs ne peut pas être supprimé
    CONSTRAINT wal_fk_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets(id)
        ON DELETE RESTRICT,

    -- Auto-référence souple vers wallet_transactions
    CONSTRAINT wal_fk_related_transaction
        FOREIGN KEY (related_transaction_id) REFERENCES wallet_transactions(id)
        ON DELETE RESTRICT
);

-- ── Commentaires ─────────────────────────────────────────────
COMMENT ON TABLE  wallet_audit_logs                     IS 'Piste d''audit immuable. Chaque action sensible (gel, dégel, clôture, fraude) y est tracée. Jamais de UPDATE ni DELETE.';
COMMENT ON COLUMN wallet_audit_logs.actor_id            IS 'Identifiant de l''auteur : UUID d''admin, "SYSTEM" pour actions automatiques, ID service IA.';
COMMENT ON COLUMN wallet_audit_logs.actor_type          IS 'Type d''auteur : ADMIN | SYSTEM | AI_SERVICE | MEMBER.';
COMMENT ON COLUMN wallet_audit_logs.action              IS 'Code de l''action : FREEZE, UNFREEZE, CLOSE, FRAUD_DETECTED, KYC_VALIDATED, POLICY_OVERRIDE…';
COMMENT ON COLUMN wallet_audit_logs.reason              IS 'Motif détaillé obligatoire. Exigence réglementaire et de compliance.';
COMMENT ON COLUMN wallet_audit_logs.previous_status     IS 'Statut du wallet avant action (snapshot pour traçabilité complète).';
COMMENT ON COLUMN wallet_audit_logs.new_status          IS 'Statut du wallet après action.';
COMMENT ON COLUMN wallet_audit_logs.ip_address          IS 'Adresse IP (IPv4 ou IPv6). Type INET PostgreSQL pour validation automatique.';
COMMENT ON COLUMN wallet_audit_logs.occurred_at         IS 'Horodatage UTC de l''action. Immuable après création.';
