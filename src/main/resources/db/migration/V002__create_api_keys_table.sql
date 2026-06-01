CREATE TABLE IF NOT EXISTS public.api_keys (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,
    key_hash     VARCHAR(255) NOT NULL UNIQUE,
    scopes       TEXT[]       NOT NULL DEFAULT '{}',
    revoked_at   TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_api_keys_tenant_id ON public.api_keys(tenant_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_key_hash  ON public.api_keys(key_hash);
