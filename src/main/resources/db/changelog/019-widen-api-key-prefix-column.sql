--liquibase formatted sql

--changeset yowyob:019-widen-api-key-prefix-column
-- ============================================================
-- V019 — Élargit api_keys.key_prefix
--
-- ApiKeyService.generateRawKey() génère un préfixe de 12 caractères
-- (raw.substring(0, 12)) mais la colonne était VARCHAR(8), ce qui
-- provoquait "value too long for type character varying(8)" à chaque
-- création de clé.
-- ============================================================

ALTER TABLE api_keys ALTER COLUMN key_prefix TYPE VARCHAR(16);
