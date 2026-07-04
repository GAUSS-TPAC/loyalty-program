/**
 * api.ts — Couche d'accès centralisée au backend Loyalty Spring Boot
 *
 * Toutes les requêtes passent par /backend/* (proxy Next.js → http://localhost:8081)
 * Le token JWT est automatiquement injecté depuis sessionStorage.
 */

const BASE = "/backend";

// ─── Utilitaires de base ────────────────────────────────────────────────────

function getAuthHeaders(): HeadersInit {
    const token =
        typeof window !== "undefined"
            ? sessionStorage.getItem("loyalty_jwt_token")
            : null;
    return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

/**
 * Auth pour le portail développeur : une clé API tenant (X-Api-Key), distincte
 * du JWT admin (loyalty_jwt_token) utilisé par le portail /portal.
 */
function getDevApiKeyHeaders(): HeadersInit {
    const key =
        typeof window !== "undefined"
            ? sessionStorage.getItem("loyalty_dev_api_key")
            : null;
    return {
        "Content-Type": "application/json",
        ...(key ? { "X-Api-Key": key } : {}),
    };
}

async function requestWithHeaders<T>(
    getHeaders: () => HeadersInit,
    method: string,
    path: string,
    body?: unknown,
    extraHeaders?: HeadersInit
): Promise<T> {
    const res = await fetch(`${BASE}${path}`, {
        method,
        headers: { ...getHeaders(), ...(extraHeaders ?? {}) },
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => res.statusText);
        throw new Error(`[${res.status}] ${text}`);
    }

    const contentType = res.headers.get("content-type");
    if (contentType?.includes("application/json")) {
        return res.json();
    }
    return null as T;
}

const request = <T>(method: string, path: string, body?: unknown, extraHeaders?: HeadersInit) =>
    requestWithHeaders<T>(getAuthHeaders, method, path, body, extraHeaders);

const get = <T>(path: string) => request<T>("GET", path);
const post = <T>(path: string, body: unknown, headers?: HeadersInit) =>
    request<T>("POST", path, body, headers);
const patch = <T>(path: string, body?: unknown) =>
    request<T>("PATCH", path, body);

// Variantes authentifiées par clé API (portail développeur, /developer/*)
const requestDev = <T>(method: string, path: string, body?: unknown) =>
    requestWithHeaders<T>(getDevApiKeyHeaders, method, path, body);
const getDev = <T>(path: string) => requestDev<T>("GET", path);
const postDev = <T>(path: string, body: unknown) => requestDev<T>("POST", path, body);
const patchDev = <T>(path: string, body?: unknown) => requestDev<T>("PATCH", path, body);

// ─── Types partagés ─────────────────────────────────────────────────────────

export interface WalletResponse {
    id: string;
    memberId: string;
    tenantId: string;
    balance: number;
    currencyCode: string;
    currencySymbol: string;
    status: "ACTIVE" | "PENDING_KYC" | "FROZEN" | "CLOSED";
    dailySpendCap: number | null;
    maxBalance: number | null;
    otpThreshold: number | null;
    kycRequiredForWithdrawal: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface WalletTransaction {
    id: string;
    walletId: string;
    type: string;
    amount: number;
    balanceAfter: number;
    description: string;
    status: string;
    createdAt: string;
}

export interface PointsAccountResponse {
    memberId: string;
    tenantId: string;
    totalPoints: number;
    tier: TierLevel;
    tierLabel: string;
    nextTierPoints: number;
    progressPercent: number;
}

export type TierLevel = "BRONZE" | "SILVER" | "GOLD" | "PLATINUM";

export interface PointsTransactionResponse {
    id: string;
    memberId: string;
    points: number;
    type: string;
    description: string;
    createdAt: string;
}

export interface MemberTierResponse {
    memberId: string;
    tier: TierLevel;
    tierLabel: string;
    achievedAt: string;
}

export interface RuleConditionDto {
    type: string;
    operator: string;
    value: string;
}

export interface RuleEffectDto {
    type: string;
    value: number;
}

export interface RuleTriggerDto {
    eventType: string;
}

export interface RuleResponse {
    id: string;
    name: string;
    description: string;
    trigger: RuleTriggerDto;
    conditions: RuleConditionDto[];
    effects: RuleEffectDto[];
    priority: number;
    status: "ACTIVE" | "INACTIVE" | "DRAFT";
    validFrom: string;
    validUntil: string | null;
    tenantId: string;
    createdAt: string;
}

export interface CreateRuleRequest {
    name: string;
    description: string;
    trigger: RuleTriggerDto;
    conditions: RuleConditionDto[];
    effects: RuleEffectDto[];
    priority: number;
    validFrom: string;
    validUntil?: string | null;
}

export interface IncomingEventRequest {
    eventType: string;
    memberId: string;
    amount?: number;
    metadata?: Record<string, string>;
}

export interface EventProcessingResponse {
    eventId: string;
    memberId: string;
    pointsAwarded: number;
    rulesApplied: string[];
    processed: boolean;
    message: string;
}

export interface BonificationStatusResponse {
    enabled: boolean;
    connected: boolean;
    baseUrl: string;
    message: string;
}

export interface SubmitBonificationRequest {
    amount: number;
    clientLogin: string;
    debit: boolean;
}

export interface BonificationTransactionResponse {
    transactionId: string;
    amount: number;
    clientLogin: string;
    debit: boolean;
    status: string;
    message: string;
}

export interface HealthResponse {
    status: string;
    components?: Record<string, { status: string }>;
}

export type ApiKeyMode = "LIVE" | "TEST";

export interface ApiKeyResponse {
    id: string;
    name: string;
    keyPrefix: string;
    mode: ApiKeyMode;
    active: boolean;
    createdAt: string;
    lastUsedAt: string | null;
    rawKey?: string;
}

export interface CreateApiKeyRequest {
    name: string;
    mode?: ApiKeyMode;
}

export type WebhookDeliveryStatus = "PENDING" | "SUCCEEDED" | "FAILED" | "EXHAUSTED";

export interface WebhookEndpointResponse {
    id: string;
    url: string;
    description: string | null;
    eventTypes: string[];
    active: boolean;
    createdAt: string;
    updatedAt: string;
    secret?: string;
}

export interface CreateWebhookEndpointRequest {
    url: string;
    description?: string;
    eventTypes: string[];
}

export interface UpdateWebhookEndpointRequest {
    url?: string;
    description?: string;
    eventTypes?: string[];
    active?: boolean;
}

export interface WebhookDeliveryResponse {
    id: string;
    endpointId: string;
    eventType: string;
    status: WebhookDeliveryStatus;
    httpStatusCode: number | null;
    responseSnippet: string | null;
    attemptCount: number;
    createdAt: string;
    deliveredAt: string | null;
}

export interface TestPingResponse {
    success: boolean;
    httpStatus: number | null;
    responseSnippet: string | null;
}

// ─── API Wallet ──────────────────────────────────────────────────────────────

export const walletApi = {
    /** GET /api/v1/wallet — Consulter le wallet du membre connecté */
    getWallet: () => get<WalletResponse>("/api/v1/wallet"),

    /** GET /api/v1/wallet/transactions?page=&size= — Historique des transactions */
    getTransactions: (page = 0, size = 20) =>
        get<WalletTransaction[]>(
            `/api/v1/wallet/transactions?page=${page}&size=${size}`
        ),
};

// ─── API Members / Loyalty ───────────────────────────────────────────────────

export const memberApi = {
    /** GET /api/v1/members/{id}/points — Solde de points + tier */
    getPoints: (memberId: string) =>
        get<PointsAccountResponse>(`/api/v1/members/${memberId}/points`),

    /** GET /api/v1/members/{id}/points/history?page=&size= — Historique des points */
    getPointsHistory: (memberId: string, page = 0, size = 20) =>
        get<PointsTransactionResponse[]>(
            `/api/v1/members/${memberId}/points/history?page=${page}&size=${size}`
        ),

    /** GET /api/v1/members/{id}/tier — Niveau de fidélité */
    getTier: (memberId: string) =>
        get<MemberTierResponse>(`/api/v1/members/${memberId}/tier`),
};

// ─── API Règles de fidélité (Admin) ─────────────────────────────────────────

export const rulesApi = {
    /** GET /api/v1/admin/rules — Lister toutes les règles du tenant */
    listRules: () => get<RuleResponse[]>("/api/v1/admin/rules"),

    /** GET /api/v1/admin/rules/{id} — Détail d'une règle */
    getRule: (ruleId: string) =>
        get<RuleResponse>(`/api/v1/admin/rules/${ruleId}`),

    /** POST /api/v1/admin/rules — Créer une règle */
    createRule: (data: CreateRuleRequest) =>
        post<RuleResponse>("/api/v1/admin/rules", data),

    /** PATCH /api/v1/admin/rules/{id}/activate — Activer une règle */
    activateRule: (ruleId: string) =>
        patch<RuleResponse>(`/api/v1/admin/rules/${ruleId}/activate`),
};

// ─── API Événements ──────────────────────────────────────────────────────────

export const eventsApi = {
    /**
     * POST /api/v1/events — Soumettre un événement déclenchant le moteur de règles
     * Supporte un header Idempotency-Key optionnel
     */
    processEvent: (data: IncomingEventRequest, idempotencyKey?: string) =>
        post<EventProcessingResponse>("/api/v1/events", data, {
            ...(idempotencyKey ? { "Idempotency-Key": idempotencyKey } : {}),
        }),
};

// ─── API Bonification ────────────────────────────────────────────────────────

export const bonificationApi = {
    /** GET /api/v1/bonification/status — Statut de l'intégration */
    getStatus: () =>
        get<BonificationStatusResponse>("/api/v1/bonification/status"),

    /** POST /api/v1/bonification/transactions — Soumettre une transaction de bonification */
    submitTransaction: (data: SubmitBonificationRequest) =>
        post<BonificationTransactionResponse>(
            "/api/v1/bonification/transactions",
            data
        ),
};

// ─── API Système / Santé ─────────────────────────────────────────────────────

export const systemApi = {
    /** GET /actuator/health — Santé de l'application */
    health: () => get<HealthResponse>("/actuator/health"),
};

// ─── API Auth (Portail Admin) ─────────────────────────────────────────────────

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

export const authApi = {
    /** POST /api/v1/auth/login — Connexion admin par email/mot de passe (KernelCore) */
    login: (data: LoginRequest) => post<LoginResponse>("/api/v1/auth/login", data),
};

// ─── API Clés API (Developer Portal — authentifié par clé API) ───────────────

export const apiKeyApi = {
    /** GET /api/v1/admin/api-keys — Lister les clés API du tenant */
    list: () => getDev<ApiKeyResponse[]>("/api/v1/admin/api-keys"),

    /** POST /api/v1/admin/api-keys — Créer une clé API (rawKey affichée une seule fois) */
    create: (data: CreateApiKeyRequest) =>
        postDev<ApiKeyResponse>("/api/v1/admin/api-keys", data),

    /** DELETE /api/v1/admin/api-keys/{id} — Révoquer une clé API */
    revoke: (id: string) =>
        requestDev<void>("DELETE", `/api/v1/admin/api-keys/${id}`),
};

// ─── API Webhooks (Developer Portal — authentifié par clé API) ───────────────

export const webhookApi = {
    /** GET /api/v1/admin/webhooks — Lister les webhooks du tenant */
    list: () => getDev<WebhookEndpointResponse[]>("/api/v1/admin/webhooks"),

    /** POST /api/v1/admin/webhooks — Créer un webhook (secret affiché une seule fois) */
    create: (data: CreateWebhookEndpointRequest) =>
        postDev<WebhookEndpointResponse>("/api/v1/admin/webhooks", data),

    /** PATCH /api/v1/admin/webhooks/{id} — Mettre à jour un webhook */
    update: (id: string, data: UpdateWebhookEndpointRequest) =>
        patchDev<WebhookEndpointResponse>(`/api/v1/admin/webhooks/${id}`, data),

    /** DELETE /api/v1/admin/webhooks/{id} — Supprimer un webhook */
    remove: (id: string) =>
        requestDev<void>("DELETE", `/api/v1/admin/webhooks/${id}`),

    /** POST /api/v1/admin/webhooks/{id}/rotate-secret — Régénérer le secret */
    rotateSecret: (id: string) =>
        postDev<WebhookEndpointResponse>(`/api/v1/admin/webhooks/${id}/rotate-secret`, {}),

    /** POST /api/v1/admin/webhooks/{id}/test — Envoyer un ping de test */
    sendTestPing: (id: string) =>
        postDev<TestPingResponse>(`/api/v1/admin/webhooks/${id}/test`, {}),

    /** GET /api/v1/admin/webhooks/deliveries?page=&size= — Journal des livraisons */
    listDeliveries: (page = 0, size = 20) =>
        getDev<WebhookDeliveryResponse[]>(
            `/api/v1/admin/webhooks/deliveries?page=${page}&size=${size}`
        ),
};

// ─── API Événements (Sandbox développeur — authentifié par clé API) ─────────

export const devEventsApi = {
    /** POST /api/v1/events — Envoyer un événement de test depuis le Sandbox (X-Api-Key) */
    processEvent: (data: IncomingEventRequest) =>
        postDev<EventProcessingResponse>("/api/v1/events", data),
};
