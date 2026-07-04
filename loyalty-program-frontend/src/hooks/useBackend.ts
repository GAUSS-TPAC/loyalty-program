"use client";

/**
 * hooks/useBackend.ts
 * Hooks React centralisés pour consommer l'API backend du programme de fidélité.
 * Utilisez ces hooks dans vos composants pour récupérer les données réelles,
 * avec gestion du loading, de l'erreur et du rafraîchissement automatique.
 */

import { useState, useEffect, useCallback } from "react";
import {
    walletApi,
    memberApi,
    rulesApi,
    bonificationApi,
    systemApi,
    promoApi,
    campaignApi,
    subscriptionApi,
    apiKeyApi,
    webhookApi,
    type WalletResponse,
    type PointsAccountResponse,
    type MemberTierResponse,
    type PointsTransactionResponse,
    type WalletTransaction,
    type RuleResponse,
    type BonificationStatusResponse,
    type HealthResponse,
    type PromoCampaignResponse,
    type CampaignResponse,
    type SubscriptionPlanResponse,
    type TenantSubscriptionResponse,
    type InvoiceResponse,
    type ApiKeyResponse,
    type WebhookEndpointResponse,
    type WebhookDeliveryResponse,
} from "@/lib/api";

// ─── Générique ───────────────────────────────────────────────────────────────

interface UseQueryResult<T> {
    data: T | null;
    isLoading: boolean;
    error: string | null;
    refetch: () => void;
}

function useQuery<T>(fetchFn: () => Promise<T>): UseQueryResult<T> {
    const [data, setData] = useState<T | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const load = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await fetchFn();
            setData(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Erreur inconnue");
        } finally {
            setIsLoading(false);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        load();
    }, [load]);

    return { data, isLoading, error, refetch: load };
}

// ─── Hooks Wallet ─────────────────────────────────────────────────────────────

/** Retourne le wallet du membre connecté */
export function useWallet(): UseQueryResult<WalletResponse> {
    return useQuery(() => walletApi.getWallet());
}

/** Retourne l'historique des transactions du wallet */
export function useWalletTransactions(
    page = 0,
    size = 20
): UseQueryResult<WalletTransaction[]> {
    return useQuery(() => walletApi.getTransactions(page, size));
}

// ─── Hooks Members / Loyalty ─────────────────────────────────────────────────

/** Retourne le solde de points et le tier d'un membre */
export function useMemberPoints(
    memberId: string | null
): UseQueryResult<PointsAccountResponse> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getPoints(memberId);
    });
}

/** Retourne l'historique des points d'un membre */
export function useMemberPointsHistory(
    memberId: string | null,
    page = 0,
    size = 20
): UseQueryResult<PointsTransactionResponse[]> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getPointsHistory(memberId, page, size);
    });
}

/** Retourne le tier (niveau) d'un membre */
export function useMemberTier(
    memberId: string | null
): UseQueryResult<MemberTierResponse> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getTier(memberId);
    });
}

// ─── Hooks Règles ─────────────────────────────────────────────────────────────

/** Retourne toutes les règles du tenant */
export function useRules(): UseQueryResult<RuleResponse[]> {
    return useQuery(() => rulesApi.listRules());
}

// ─── Hooks Bonification ───────────────────────────────────────────────────────

/** Retourne le statut de l'intégration bonification */
export function useBonificationStatus(): UseQueryResult<BonificationStatusResponse> {
    return useQuery(() => bonificationApi.getStatus());
}

// ─── Hooks Système ────────────────────────────────────────────────────────────

/** Retourne la santé du backend */
export function useBackendHealth(): UseQueryResult<HealthResponse> {
    return useQuery(() => systemApi.health());
}

// ─── Hooks Codes Promo ────────────────────────────────────────────────────────

/** Retourne toutes les campagnes promo du tenant */
export function usePromos(): UseQueryResult<PromoCampaignResponse[]> {
    return useQuery(() => promoApi.listAll());
}

// ─── Hooks Campagnes ──────────────────────────────────────────────────────────

/** Retourne toutes les campagnes temporisées */
export function useCampaigns(): UseQueryResult<CampaignResponse[]> {
    return useQuery(() => campaignApi.listAll());
}

// ─── Hooks Abonnements ────────────────────────────────────────────────────────

/** Retourne la liste des plans disponibles */
export function useSubscriptionPlans(): UseQueryResult<SubscriptionPlanResponse[]> {
    return useQuery(() => subscriptionApi.listPlans());
}

/** Retourne l'abonnement courant du tenant */
export function useMySubscription(): UseQueryResult<TenantSubscriptionResponse> {
    return useQuery(() => subscriptionApi.getMySubscription());
}

/** Retourne les factures du tenant */
export function useMyInvoices(): UseQueryResult<InvoiceResponse[]> {
    return useQuery(() => subscriptionApi.getMyInvoices());
}

// ─── Hooks Developer Portal ───────────────────────────────────────────────────

/** Retourne les clés API du tenant */
export function useApiKeys(): UseQueryResult<ApiKeyResponse[]> {
    return useQuery(() => apiKeyApi.list());
}

/** Retourne les webhooks du tenant */
export function useWebhooks(): UseQueryResult<WebhookEndpointResponse[]> {
    return useQuery(() => webhookApi.list());
}

/** Retourne le journal des livraisons webhook du tenant */
export function useWebhookDeliveries(
    page = 0,
    size = 20
): UseQueryResult<WebhookDeliveryResponse[]> {
    return useQuery(() => webhookApi.listDeliveries(page, size));
}
