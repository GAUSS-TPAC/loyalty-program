/** Exception de base du SDK Yowyob Loyalty. */
export declare class LoyaltyError extends Error {
    constructor(message: string);
}
/** Erreur renvoyée par l'API Loyalty (statut HTTP non 2xx). */
export declare class ApiError extends LoyaltyError {
    readonly statusCode: number;
    readonly body: unknown;
    constructor(message: string, statusCode: number, body?: unknown);
}
/** Clé API invalide, révoquée ou absente (HTTP 401/403). */
export declare class AuthenticationError extends ApiError {
    constructor(message: string, statusCode: number, body?: unknown);
}
/**
 * La signature du callback webhook est absente, invalide ou expirée.
 * Ne traitez JAMAIS un callback qui lève cette erreur.
 */
export declare class SignatureVerificationError extends LoyaltyError {
    constructor(message: string);
}
export interface TrackEventOptions {
    /** Date ISO-8601 de l'événement (défaut : maintenant, UTC). */
    occurredAt?: string;
    /** Données métier lues par les règles de fidélité (ex. { amount: 4990 }). */
    payload?: Record<string, unknown>;
    /** Le même événement renvoyé deux fois avec la même clé n'est traité qu'une fois. */
    idempotencyKey?: string;
}
export interface AppliedEffect {
    effectType: string;
    ruleName: string;
    details: Record<string, unknown>;
}
export interface EventProcessingResult {
    eventId: string;
    effectsApplied: AppliedEffect[];
    notifications: string[];
    processedAt: string;
}
export interface WebhookEvent {
    id: string;
    type: string;
    createdAt: string;
    /** Clé publique de l'application liée à l'endpoint (absent pour un webhook autonome). */
    application?: string;
    data: Record<string, unknown>;
}
export interface LoyaltyClientOptions {
    /** Secret webhook (whsec_…), requis pour checkCallbackIntegrity. */
    webhookSecret?: string;
    /** Timeout HTTP en millisecondes (défaut 10 000). */
    timeoutMs?: number;
}
/**
 * Client officiel de l'API Yowyob Loyalty.
 *
 * ```ts
 * const loyalty = new LoyaltyClient(
 *   process.env.LOYALTY_PUBLIC_KEY!,   // pk_live_… (identifiant, exposable)
 *   process.env.LOYALTY_PRIVATE_KEY!,  // sk_live_… (SECRÈTE — jamais côté navigateur)
 *   "https://loyalty.yowyob.com",
 *   { webhookSecret: process.env.LOYALTY_WEBHOOK_SECRET }
 * );
 * const result = await loyalty.trackEvent("purchase.completed", memberId, { payload: { amount: 4990 } });
 * ```
 */
export declare class LoyaltyClient {
    readonly publicKey: string;
    private readonly privateKey;
    static readonly DEFAULT_SIGNATURE_TOLERANCE_SECONDS = 300;
    private readonly baseUrl;
    private readonly webhookSecret?;
    private readonly timeoutMs;
    constructor(publicKey: string, privateKey: string, baseUrl?: string, options?: LoyaltyClientOptions);
    /** Envoie un événement métier (achat, trajet, inscription…) au moteur de fidélité. */
    trackEvent(eventType: string, memberId: string, options?: TrackEventOptions): Promise<EventProcessingResult>;
    /** Solde de points et palier courant d'un membre. */
    getMemberPoints(memberId: string): Promise<Record<string, unknown>>;
    /** Palier de fidélité courant d'un membre. */
    getMemberTier(memberId: string): Promise<Record<string, unknown>>;
    /** Portefeuille (wallet) d'un membre : solde monétaire et politique associée. */
    getWallet(memberId: string): Promise<Record<string, unknown>>;
    /**
     * Vérifie l'authenticité d'un callback webhook reçu de la plateforme Loyalty.
     *
     * La plateforme signe chaque callback avec le SECRET WEBHOOK (whsec_…, différent de la
     * clé privée) : signature = "sha256=" + hex(HMAC-SHA256(secret, timestamp + "." + corps brut)).
     *
     * @param headers Les en-têtes HTTP reçus (insensible à la casse)
     * @param rawBody Le corps BRUT de la requête (avant tout JSON.parse) — avec Express,
     *                utilisez `express.raw({ type: "application/json" })` sur la route
     * @param toleranceSeconds Tolérance d'horodatage anti-rejeu (défaut 300 s)
     * @returns Le payload décodé, une fois la signature validée
     */
    checkCallbackIntegrity(headers: Record<string, string | string[] | undefined>, rawBody: string | Buffer, toleranceSeconds?: number): WebhookEvent;
    private request;
}
