"use client";

/**
 * hooks/useBackend.ts
 * Hooks React centralisés pour consommer l'API backend du programme de fidélité.
 * Utilisez ces hooks dans vos composants pour récupérer les données réelles,
 * avec gestion du loading, de l'erreur et du rafraîchissement automatique.
 */

import { useState, useEffect, useCallback, useRef } from "react";
import {
    memberApi,
    rulesApi,
    systemApi,
    promoApi,
    campaignApi,
    subscriptionApi,
    apiKeyApi,
    webhookApi,
    adminMembersApi,
    tierPolicyApi,
    adminLogsApi,
    platformApi,
    type PlatformTenantResponse,
    type WalletResponse,
    type PointsAccountResponse,
    type MemberTierResponse,
    type PointsTransactionResponse,
    type WalletTransaction,
    type RuleResponse,
    type HealthResponse,
    type PromoCampaignResponse,
    type CampaignResponse,
    type SubscriptionPlanResponse,
    type TenantSubscriptionResponse,
    type InvoiceResponse,
    type ApiKeyResponse,
    type WebhookEndpointResponse,
    type WebhookDeliveryResponse,
    type MemberSummaryResponse,
    type TierPolicyResponse,
    type PointsTransactionLogResponse,
    type ApiKeyPointsFlowResponse,
} from "@/lib/api";

// ─── Générique ───────────────────────────────────────────────────────────────

interface UseQueryResult<T> {
    data: T | null;
    isLoading: boolean;
    error: string | null;
    refetch: () => void;
}

function useQuery<T>(fetchFn: () => Promise<T>, deps: unknown[] = []): UseQueryResult<T> {
    const [data, setData] = useState<T | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // fetchFn est recréée à chaque rendu (closure sur memberId/page/etc.) ; on la lit
    // via ref pour que `load` reste une référence stable tout en appelant la version
    // la plus récente (évite le bug de closure figée d'un useCallback à deps []).
    const fetchFnRef = useRef(fetchFn);
    useEffect(() => {
        fetchFnRef.current = fetchFn;
    });

    const load = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await fetchFnRef.current();
            setData(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Erreur inconnue");
        } finally {
            setIsLoading(false);
        }
    }, []);

    // La liste `deps` est dynamique (générique) : on la réduit à une clé stable pour
    // fournir un littéral de tableau à useEffect (exigé par le linter react-hooks).
    const depsKey = JSON.stringify(deps);
    useEffect(() => {
        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [depsKey]);

    return { data, isLoading, error, refetch: load };
}

// ─── Hooks Wallet ─────────────────────────────────────────────────────────────

/** Retourne le wallet d'un membre donné (portail admin : pas de wallet "personnel") */
export function useMemberWallet(
    memberId: string | null
): UseQueryResult<WalletResponse> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getWallet(memberId);
    }, [memberId]);
}

/** Retourne l'historique des transactions du wallet d'un membre donné */
export function useMemberWalletTransactions(
    memberId: string | null,
    page = 0,
    size = 20
): UseQueryResult<WalletTransaction[]> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getWalletTransactions(memberId, page, size);
    }, [memberId, page, size]);
}

// ─── Hooks Members / Loyalty ─────────────────────────────────────────────────

/** Retourne le solde de points et le tier d'un membre */
export function useMemberPoints(
    memberId: string | null
): UseQueryResult<PointsAccountResponse> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getPoints(memberId);
    }, [memberId]);
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
    }, [memberId, page, size]);
}

/** Retourne le tier (niveau) d'un membre */
export function useMemberTier(
    memberId: string | null
): UseQueryResult<MemberTierResponse> {
    return useQuery(() => {
        if (!memberId) return Promise.reject(new Error("memberId manquant"));
        return memberApi.getTier(memberId);
    }, [memberId]);
}

// ─── Hooks Règles ─────────────────────────────────────────────────────────────

/** Retourne toutes les règles du tenant */
export function useRules(): UseQueryResult<RuleResponse[]> {
    return useQuery(() => rulesApi.listRules());
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
    return useQuery(() => webhookApi.listDeliveries(page, size), [page, size]);
}

// ─── Hooks Admin (Annuaire Membres / Tier Policy / Logs) ─────────────────────

/** Retourne l'annuaire des membres (portefeuilles) du tenant */
export function useAdminMembers(
    page = 0,
    size = 20
): UseQueryResult<MemberSummaryResponse[]> {
    return useQuery(() => adminMembersApi.list(page, size), [page, size]);
}

/** Retourne la politique de tier (paliers) du tenant */
export function useTierPolicy(): UseQueryResult<TierPolicyResponse> {
    return useQuery(() => tierPolicyApi.get());
}

/** Retourne le journal tenant-wide des transactions de points */
export function useAdminLogs(
    page = 0,
    size = 20
): UseQueryResult<PointsTransactionLogResponse[]> {
    return useQuery(() => adminLogsApi.listPointsTransactions(page, size), [page, size]);
}

/** Retourne le flux de points (crédité/débité) agrégé par clé API */
export function useApiKeyPointsFlow(): UseQueryResult<ApiKeyPointsFlowResponse[]> {
    return useQuery(() => adminLogsApi.flowByApiKey());
}

// ─── Hooks Console Plateforme ─────────────────────────────────────────────────

/** Retourne les organisations abonnées au service loyalty (console plateforme) */
export function usePlatformTenants(): UseQueryResult<PlatformTenantResponse[]> {
    return useQuery(() => platformApi.listTenants());
}
