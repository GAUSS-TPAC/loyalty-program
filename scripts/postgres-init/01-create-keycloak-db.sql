-- Keycloak must not share the application database: Flyway's baseline-on-migrate
-- treats a non-empty "public" schema as an already-migrated baseline, which
-- silently skips V001 (wallets table) and breaks V002+.
CREATE DATABASE keycloak_db;
