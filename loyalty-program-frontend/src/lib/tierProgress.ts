import type { TierLevel, TierThreshold } from "@/lib/api";

export const TIER_LABELS: Record<TierLevel, string> = {
    BRONZE: "Bronze",
    SILVER: "Argent",
    GOLD: "Or",
    PLATINUM: "Platine",
};

export interface TierProgress {
    progressPercent: number;
    pointsToNext: number;
    nextTierLevel: TierLevel | null;
}

/**
 * Calcule la progression vers le tier suivant à partir des seuils de la
 * politique de tier du tenant (TierPolicyResponse.thresholds) et du total de
 * points à vie du membre (PointsAccountResponse.lifetimeEarned). Le backend
 * n'expose pas ce calcul directement — les seuils bruts si, donc on le fait
 * côté client.
 */
export function computeTierProgress(
    lifetimeEarned: number,
    thresholds: TierThreshold[] | undefined
): TierProgress {
    if (!thresholds || thresholds.length === 0) {
        return { progressPercent: 0, pointsToNext: 0, nextTierLevel: null };
    }

    const sorted = [...thresholds].sort((a, b) => a.threshold - b.threshold);
    const next = sorted.find((t) => t.threshold > lifetimeEarned);

    if (!next) {
        // Déjà au tier maximum
        return { progressPercent: 100, pointsToNext: 0, nextTierLevel: null };
    }

    const previous = [...sorted].reverse().find((t) => t.threshold <= lifetimeEarned);
    const floor = previous?.threshold ?? 0;
    const span = next.threshold - floor;
    const progressPercent = span > 0 ? Math.min(100, Math.max(0, ((lifetimeEarned - floor) / span) * 100)) : 0;

    return {
        progressPercent,
        pointsToNext: next.threshold - lifetimeEarned,
        nextTierLevel: next.level as TierLevel,
    };
}
