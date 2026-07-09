-- ============================================================
-- V021 — DELETE /api/v1/admin/webhooks/{id} échouait avec une violation
-- de contrainte FK dès qu'un endpoint avait au moins une livraison
-- journalisée (webhook_deliveries.endpoint_id -> webhook_endpoints.id
-- sans ON DELETE CASCADE). Le journal de livraison est un historique
-- rattaché à l'endpoint : il doit être supprimé avec lui.
-- ============================================================

ALTER TABLE webhook_deliveries
    DROP CONSTRAINT webhook_deliveries_endpoint_id_fkey,
    ADD CONSTRAINT webhook_deliveries_endpoint_id_fkey
        FOREIGN KEY (endpoint_id) REFERENCES webhook_endpoints (id)
        ON DELETE CASCADE;
