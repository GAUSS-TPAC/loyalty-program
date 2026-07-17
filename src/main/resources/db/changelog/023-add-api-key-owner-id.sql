--liquibase formatted sql

--changeset yowyob:023-add-api-key-owner-id
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS owner_id UUID;
