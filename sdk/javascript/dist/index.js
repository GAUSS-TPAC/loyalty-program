import { createHmac, timingSafeEqual } from "node:crypto";
/** Exception de base du SDK Yowyob Loyalty. */
export class LoyaltyError extends Error {
    constructor(message) {
        super(message);
        this.name = "LoyaltyError";
    }
}
/** Erreur renvoyée par l'API Loyalty (statut HTTP non 2xx). */
export class ApiError extends LoyaltyError {
    statusCode;
    body;
    constructor(message, statusCode, body = null) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
        this.name = "ApiError";
    }
}
/** Clé API invalide, révoquée ou absente (HTTP 401/403). */
export class AuthenticationError extends ApiError {
    constructor(message, statusCode, body = null) {
        super(message, statusCode, body);
        this.name = "AuthenticationError";
    }
}
/**
 * La signature du callback webhook est absente, invalide ou expirée.
 * Ne traitez JAMAIS un callback qui lève cette erreur.
 */
export class SignatureVerificationError extends LoyaltyError {
    constructor(message) {
        super(message);
        this.name = "SignatureVerificationError";
    }
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
export class LoyaltyClient {
    publicKey;
    privateKey;
    static DEFAULT_SIGNATURE_TOLERANCE_SECONDS = 300;
    baseUrl;
    webhookSecret;
    timeoutMs;
    constructor(publicKey, privateKey, baseUrl = "https://loyalty.yowyob.com", options = {}) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.baseUrl = baseUrl.replace(/\/+$/, "");
        this.webhookSecret = options.webhookSecret;
        this.timeoutMs = options.timeoutMs ?? 10_000;
    }
    /** Envoie un événement métier (achat, trajet, inscription…) au moteur de fidélité. */
    async trackEvent(eventType, memberId, options = {}) {
        const headers = {};
        if (options.idempotencyKey) {
            headers["Idempotency-Key"] = options.idempotencyKey;
        }
        return this.request("POST", `/api/v1/apps/${encodeURIComponent(this.publicKey)}/events`, {
            eventType,
            memberId,
            occurredAt: options.occurredAt ?? new Date().toISOString(),
            payload: options.payload ?? {},
        }, headers);
    }
    /** Solde de points et palier courant d'un membre. */
    async getMemberPoints(memberId) {
        return this.request("GET", `/api/v1/members/${encodeURIComponent(memberId)}/points`);
    }
    /** Palier de fidélité courant d'un membre. */
    async getMemberTier(memberId) {
        return this.request("GET", `/api/v1/members/${encodeURIComponent(memberId)}/tier`);
    }
    /** Portefeuille (wallet) d'un membre : solde monétaire et politique associée. */
    async getWallet(memberId) {
        return this.request("GET", `/api/v1/members/${encodeURIComponent(memberId)}/wallet`);
    }
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
    checkCallbackIntegrity(headers, rawBody, toleranceSeconds = LoyaltyClient.DEFAULT_SIGNATURE_TOLERANCE_SECONDS) {
        if (!this.webhookSecret) {
            throw new LoyaltyError("webhookSecret non configuré : passez-le dans les options du constructeur de LoyaltyClient.");
        }
        const normalized = new Map();
        for (const [name, value] of Object.entries(headers)) {
            if (value === undefined)
                continue;
            normalized.set(name.toLowerCase(), Array.isArray(value) ? value[0] : value);
        }
        const signature = normalized.get("x-webhook-signature");
        const timestamp = normalized.get("x-webhook-timestamp");
        if (!signature || !timestamp) {
            throw new SignatureVerificationError("En-têtes X-Webhook-Signature ou X-Webhook-Timestamp absents.");
        }
        if (Math.abs(Date.now() / 1000 - Number(timestamp)) > toleranceSeconds) {
            throw new SignatureVerificationError("Horodatage du callback hors tolérance (rejeu possible).");
        }
        const body = typeof rawBody === "string" ? rawBody : rawBody.toString("utf8");
        const expected = "sha256=" + createHmac("sha256", this.webhookSecret).update(`${timestamp}.${body}`).digest("hex");
        const a = Buffer.from(expected);
        const b = Buffer.from(signature);
        if (a.length !== b.length || !timingSafeEqual(a, b)) {
            throw new SignatureVerificationError("Signature du callback invalide.");
        }
        try {
            return JSON.parse(body);
        }
        catch {
            throw new SignatureVerificationError("Corps du callback illisible (JSON attendu).");
        }
    }
    async request(method, path, body, extraHeaders = {}) {
        const headers = {
            Accept: "application/json",
            "X-Api-Key": this.privateKey,
            ...extraHeaders,
        };
        const init = {
            method,
            headers,
            signal: AbortSignal.timeout(this.timeoutMs),
        };
        if (body !== undefined) {
            headers["Content-Type"] = "application/json";
            init.body = JSON.stringify(body);
        }
        let response;
        try {
            response = await fetch(this.baseUrl + path, init);
        }
        catch (cause) {
            throw new ApiError(`Erreur réseau vers l'API Loyalty : ${cause.message}`, 0);
        }
        const text = await response.text();
        let decoded = null;
        try {
            decoded = text ? JSON.parse(text) : null;
        }
        catch {
            decoded = null;
        }
        if (response.status === 401 || response.status === 403) {
            throw new AuthenticationError(`Clé API invalide ou révoquée (HTTP ${response.status}).`, response.status, decoded);
        }
        if (!response.ok) {
            const detail = decoded?.detail ??
                decoded?.title ??
                `Erreur API Loyalty (HTTP ${response.status}).`;
            throw new ApiError(detail, response.status, decoded);
        }
        return (decoded ?? {});
    }
}
