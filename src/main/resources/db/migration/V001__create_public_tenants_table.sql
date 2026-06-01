CREATE TABLE IF NOT EXISTS public.tenants (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING_SETUP',
    plan        VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    config      JSONB        NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_tenants_slug   ON public.tenants(slug);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON public.tenants(status);
