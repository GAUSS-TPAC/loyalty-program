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

async function request<T>(
    method: string,
    path: string,
    body?: unknown,
    extraHeaders?: HeadersInit
): Promise<T> {
    const res = await fetch(`${BASE}${path}`, {
        method,
        headers: { ...getAuthHeaders(), ...(extraHeaders ?? {}) },
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

const get = <T>(path: string) => request<T>("GET", path);
const post = <T>(path: string, body: unknown, headers?: HeadersInit) =>
    request<T>("POST", path, body, headers);
const patch = <T>(path: string, body?: unknown) =>
    request<T>("PATCH", path, body);

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

// ─── Types Codes Promo ───────────────────────────────────────────────────────

export interface PromoCampaignResponse {
    id: string;
    tenantId: string;
    code: string;
    name: string;
    discountType: "PERCENTAGE" | "FIXED_AMOUNT" | "FREE_ITEM";
    discountValue: number;
    minOrderAmount: number | null;
    maxUses: number;
    perMemberLimit: number;
    startDate: string;
    endDate: string | null;
    active: boolean;
    createdAt: string;
}

export interface CreatePromoRequest {
    code: string;
    name: string;
    discountType: string;
    discountValue: number;
    minOrderAmount?: number;
    maxUses: number;
    perMemberLimit: number;
    startDate: string;
    endDate?: string;
}

export interface ValidatePromoResponse {
    valid: boolean;
    discountApplied: number;
    campaignName: string;
    message: string;
}

// ─── Types Campagnes ─────────────────────────────────────────────────────────

export interface CampaignResponse {
    id: string;
    tenantId: string;
    name: string;
    description: string | null;
    campaignType: "BONUS_MULTIPLIER" | "FLAT_BONUS";
    targetEventType: string | null;
    bonusMultiplier: number | null;
    bonusPoints: number | null;
    startDate: string;
    endDate: string | null;
    status: "DRAFT" | "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
    createdAt: string;
}

export interface CreateCampaignRequest {
    name: string;
    description?: string;
    campaignType: string;
    targetEventType?: string;
    bonusMultiplier?: number;
    bonusPoints?: number;
    startDate: string;
    endDate?: string;
}

// ─── Types Abonnements ───────────────────────────────────────────────────────

export interface PlanFeaturesResponse {
    maxRules: number;
    maxMembers: number;
    maxEventsPerMonth: number;
    referralEnabled: boolean;
    campaignsEnabled: boolean;
    promoCodesEnabled: boolean;
    analyticsEnabled: boolean;
}

export interface SubscriptionPlanResponse {
    id: string;
    code: string;
    name: string;
    description: string | null;
    priceMonthly: number;
    priceYearly: number;
    currency: string;
    features: PlanFeaturesResponse;
    active: boolean;
    createdAt: string;
}

export interface TenantSubscriptionResponse {
    id: string;
    tenantId: string;
    planId: string;
    status: "TRIAL" | "ACTIVE" | "PAST_DUE" | "CANCELLED" | "EXPIRED";
    billingCycle: "MONTHLY" | "YEARLY";
    currentPeriodStart: string;
    currentPeriodEnd: string;
    trialEndDate: string | null;
    cancelledAt: string | null;
    createdAt: string;
}

export interface InvoiceResponse {
    id: string;
    tenantId: string;
    subscriptionId: string;
    planId: string;
    amount: number;
    currency: string;
    status: "PENDING" | "PAID" | "FAILED" | "VOID";
    periodStart: string;
    periodEnd: string;
    dueDate: string;
    paidAt: string | null;
    createdAt: string;
}

// ─── API Codes Promo ─────────────────────────────────────────────────────────

export const promoApi = {
    listAll: () => get<PromoCampaignResponse[]>("/api/v1/promo/admin/campaigns"),
    listActive: () => get<PromoCampaignResponse[]>("/api/v1/promo/campaigns"),
    create: (data: CreatePromoRequest) =>
        post<PromoCampaignResponse>("/api/v1/promo/admin/campaigns", data),
    activate: (id: string) =>
        patch<PromoCampaignResponse>(`/api/v1/promo/admin/campaigns/${id}/activate`),
    deactivate: (id: string) =>
        patch<PromoCampaignResponse>(`/api/v1/promo/admin/campaigns/${id}/deactivate`),
    validate: (code: string, orderAmount: number) =>
        post<ValidatePromoResponse>("/api/v1/promo/validate", { code, orderAmount }),
};

// ─── API Campagnes ───────────────────────────────────────────────────────────

export const campaignApi = {
    listAll: () => get<CampaignResponse[]>("/api/v1/campaigns"),
    listActive: () => get<CampaignResponse[]>("/api/v1/campaigns/active"),
    create: (data: CreateCampaignRequest) =>
        post<CampaignResponse>("/api/v1/campaigns", data),
    activate: (id: string) =>
        patch<CampaignResponse>(`/api/v1/campaigns/${id}/activate`),
    pause: (id: string) =>
        patch<CampaignResponse>(`/api/v1/campaigns/${id}/pause`),
    cancel: (id: string) =>
        patch<CampaignResponse>(`/api/v1/campaigns/${id}/cancel`),
};

// ─── API Abonnements ─────────────────────────────────────────────────────────

export const subscriptionApi = {
    listPlans: () => get<SubscriptionPlanResponse[]>("/api/v1/subscription-plans"),
    getMySubscription: () => get<TenantSubscriptionResponse>("/api/v1/subscriptions/me"),
    getMyInvoices: () => get<InvoiceResponse[]>("/api/v1/subscriptions/me/invoices"),
    subscribe: (planId: string, billingCycle: string) =>
        post<TenantSubscriptionResponse>("/api/v1/subscriptions", { planId, billingCycle }),
    startTrial: (planId: string, trialDays = 14) =>
        post<TenantSubscriptionResponse>("/api/v1/subscriptions/trial", { planId, trialDays }),
    changePlan: (newPlanId: string) =>
        patch<TenantSubscriptionResponse>("/api/v1/subscriptions/me/plan", { newPlanId }),
    cancel: () => request<void>("DELETE", "/api/v1/subscriptions/me"),
};
