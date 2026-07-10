--liquibase formatted sql

--changeset yowyob:016-add-api-key-mode
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS mode VARCHAR(10) NOT NULL DEFAULT 'LIVE';
