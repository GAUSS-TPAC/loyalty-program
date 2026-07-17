"use client";

import { use, useState, useEffect } from "react";
import { Link } from "@/i18n/routing";
import {
  ArrowLeft,
  User,
  Activity,
  AlertTriangle,
  RefreshCcw,
  Award,
  TrendingUp,
  Clock,
  Wallet,
  Lock,
  Unlock,
} from "lucide-react";
import {
  useMemberPoints,
  useMemberTier,
  useMemberPointsHistory,
  useTierPolicy,
} from "@/hooks/useBackend";
import { memberApi, type WalletResponse } from "@/lib/api";
import { TIER_LABELS, computeTierProgress } from "@/lib/tierProgress";

export default function MemberDetailView({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const memberId = resolvedParams.id;

  const { data: pointsData, isLoading: pointsLoading, refetch: refetchPoints } = useMemberPoints(memberId);
  const { data: tierData, isLoading: tierLoading, refetch: refetchTier } = useMemberTier(memberId);
  const { data: history, isLoading: historyLoading, refetch: refetchHistory } = useMemberPointsHistory(memberId);
  const { data: tierPolicy } = useTierPolicy();
  const tierProgress = pointsData
    ? computeTierProgress(pointsData.lifetimeEarned, tierPolicy?.thresholds)
    : null;

  const [wallet, setWallet] = useState<WalletResponse | null>(null);
  const [walletLoading, setWalletLoading] = useState(true);
  const [walletError, setWalletError] = useState<string | null>(null);
  const [freezing, setFreezing] = useState(false);

  const loadWallet = () => {
    setWalletLoading(true);
    setWalletError(null);
    memberApi
      .getWallet(memberId)
      .then(setWallet)
      .catch((err) => setWalletError(err instanceof Error ? err.message : "Wallet not found"))
      .finally(() => setWalletLoading(false));
  };

  useEffect(() => {
    loadWallet();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [memberId]);

  const handleRefresh = () => {
    refetchPoints();
    refetchTier();
    refetchHistory();
    loadWallet();
  };

  const handleToggleFreeze = async () => {
    if (!wallet) return;
    setFreezing(true);
    try {
      if (wallet.status === "FROZEN") {
        await memberApi.unfreezeWallet(memberId);
      } else {
        await memberApi.freezeWallet(memberId, "Manual freeze from admin portal");
      }
      loadWallet();
    } finally {
      setFreezing(false);
    }
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link
            href="/portal/members"
            className="p-2.5 text-muted-foreground hover:bg-secondary rounded-xl transition-all border border-border bg-card shadow-sm"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div className="space-y-1">
            <h1 className="text-3xl font-bold tracking-tight text-foreground flex items-center gap-2">
              <User className="w-8 h-8 text-primary/40" />
              Détails du Membre
            </h1>
            <p className="text-muted-foreground text-xs font-mono uppercase tracking-widest bg-secondary/50 px-2 py-0.5 rounded-md inline-block">
              UUID : {memberId}
            </p>
          </div>
        </div>

        <button
          onClick={handleRefresh}
          className="flex items-center gap-2 text-xs font-bold text-primary hover:text-primary/80 border border-primary/20 px-4 py-2 rounded-xl bg-primary/5 hover:bg-primary/10 transition-all active:scale-95"
        >
          <RefreshCcw
            className={`w-4 h-4 ${
              pointsLoading || tierLoading || historyLoading || walletLoading ? "animate-spin" : ""
            }`}
          />
          Rafraîchir les données
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-8 mt-6">
        <div className="md:col-span-4 space-y-8">
          <div className="border border-border bg-card rounded-2xl p-8 shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <h3 className="font-bold text-sm uppercase tracking-widest text-foreground flex items-center gap-2">
                <Wallet className="w-4 h-4 text-primary" /> Wallet
              </h3>
              {wallet && (
                <span
                  className={`text-[10px] font-black uppercase tracking-widest px-2 py-1 rounded-full border ${
                    wallet.status === "ACTIVE"
                      ? "bg-emerald-50 text-emerald-700 border-emerald-100"
                      : "bg-rose-50 text-rose-700 border-rose-100"
                  }`}
                >
                  {wallet.status}
                </span>
              )}
            </div>

            {walletLoading ? (
              <div className="h-10 bg-muted animate-pulse rounded-lg" />
            ) : walletError ? (
              <p className="text-xs text-destructive flex items-center gap-1.5">
                <AlertTriangle className="w-3.5 h-3.5" /> {walletError}
              </p>
            ) : wallet ? (
              <div className="space-y-4">
                <div className="flex items-baseline gap-2 text-primary">
                  <span className="text-4xl font-black font-mono tracking-tighter">
                    {wallet.balance.toLocaleString()}
                  </span>
                  <span className="text-sm font-bold uppercase tracking-widest">{wallet.currencyCode}</span>
                </div>
                <button
                  onClick={handleToggleFreeze}
                  disabled={freezing}
                  className="w-full flex items-center justify-center gap-2 text-xs font-bold border border-border px-4 py-2.5 rounded-xl hover:bg-secondary transition-all active:scale-95 disabled:opacity-50"
                >
                  {wallet.status === "FROZEN" ? (
                    <>
                      <Unlock className="w-4 h-4" /> Dégeler le wallet
                    </>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" /> Geler le wallet
                    </>
                  )}
                </button>
              </div>
            ) : null}
          </div>

          <div className="border border-border bg-card rounded-2xl p-8 shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Programme Tier</h3>
              <Award className="w-5 h-5 text-primary" />
            </div>

            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-muted-foreground">Rang Actuel</span>
                {tierLoading ? (
                  <div className="h-6 w-20 bg-muted animate-pulse rounded" />
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-black uppercase tracking-widest bg-primary text-white shadow-sm ring-4 ring-primary/10">
                    {tierData ? TIER_LABELS[tierData.tierLevel] : "Bronze"}
                  </span>
                )}
              </div>

              <div className="space-y-2.5">
                <div className="flex justify-between text-[10px] font-black uppercase tracking-widest">
                  <span className="text-muted-foreground">Progression</span>
                  <span className="text-primary font-mono">{Math.round(tierProgress?.progressPercent ?? 0)}%</span>
                </div>
                <div className="h-3 bg-secondary rounded-full overflow-hidden border border-border shadow-inner">
                  <div
                    className="h-full bg-primary transition-all duration-1000"
                    style={{ width: `${tierProgress?.progressPercent ?? 0}%` }}
                  />
                </div>
                <p className="text-[10px] text-center text-muted-foreground italic">
                  {tierProgress && tierProgress.pointsToNext > 0
                    ? `+${tierProgress.pointsToNext} points avant le rang supérieur`
                    : "Rang maximum atteint"}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="md:col-span-8 space-y-8">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex items-center gap-5 group hover:border-primary/30 transition-colors">
              <div className="p-3 bg-emerald-50 rounded-2xl text-emerald-600 group-hover:scale-110 transition-transform">
                <TrendingUp className="w-6 h-6" />
              </div>
              <div>
                <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">
                  Points Totaux
                </p>
                <p className="text-xl font-bold text-foreground font-mono">
                  {pointsData?.availablePoints?.toLocaleString() ?? "0"} <span className="text-xs">pts</span>
                </p>
              </div>
            </div>
            <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex items-center gap-5 group hover:border-primary/30 transition-colors">
              <div className="p-3 bg-primary/5 rounded-2xl text-primary group-hover:scale-110 transition-transform">
                <Activity className="w-6 h-6" />
              </div>
              <div>
                <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">
                  Transactions de points
                </p>
                <p className="text-xl font-bold text-foreground font-mono">{history?.length ?? 0} <span className="text-xs">ops</span></p>
              </div>
            </div>
          </div>

          <div className="border border-border bg-card rounded-2xl shadow-sm overflow-hidden flex flex-col group">
            <div className="bg-secondary/50 px-8 py-5 border-b border-border flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-card rounded-lg shadow-sm border border-border">
                  <Clock className="w-5 h-5 text-primary" />
                </div>
                <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Historique des points</h3>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-[10px] text-muted-foreground uppercase bg-muted/30 border-b border-border font-black tracking-widest">
                  <tr>
                    <th className="px-8 py-5">Date UTC</th>
                    <th className="px-8 py-5">Transaction ID</th>
                    <th className="px-8 py-5">Source</th>
                    <th className="px-8 py-5 text-right">Points</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {historyLoading ? (
                    [1, 2, 3].map((i) => (
                      <tr key={i} className="animate-pulse">
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-32" /></td>
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-24" /></td>
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-40" /></td>
                        <td className="px-8 py-5 text-right"><div className="h-4 bg-muted rounded w-16 ml-auto" /></td>
                      </tr>
                    ))
                  ) : history && history.length > 0 ? (
                    history.map((tx, index) => (
                      <tr
                        key={tx.id}
                        className={`border-b border-border/40 hover:bg-muted/10 transition-colors group/row ${
                          index % 2 === 0 ? "bg-background" : "bg-muted/5"
                        }`}
                      >
                        <td className="px-8 py-5 text-[10px] font-mono text-muted-foreground">
                          {new Date(tx.createdAt).toLocaleString()}
                        </td>
                        <td className="px-8 py-5 font-mono text-[10px] text-muted-foreground group-hover/row:text-primary transition-colors">
                          {tx.id.substring(0, 8)}…
                        </td>
                        <td className="px-8 py-5">
                          <span className="text-[10px] font-black uppercase tracking-widest bg-secondary text-primary px-2 py-1 rounded border border-border shadow-sm">
                            {tx.type}
                          </span>
                          <p className="text-[10px] text-muted-foreground mt-1.5 italic font-sans">{tx.source}</p>
                        </td>
                        <td
                          className={`px-8 py-5 font-black font-mono text-right text-base ${
                            tx.type === "CREDIT" ? "text-emerald-600" : "text-rose-600"
                          }`}
                        >
                          {tx.type === "CREDIT" ? "+" : "-"}
                          {tx.amount}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={4} className="px-8 py-10 text-center text-xs italic text-muted-foreground">
                        Aucun historique de points trouvé pour ce membre.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="bg-secondary/30 border border-border rounded-2xl p-6 flex items-start gap-4 shadow-inner">
            <div className="p-2.5 bg-card rounded-xl border border-border">
              <AlertTriangle className="w-5 h-5 text-orange-500" />
            </div>
            <div className="space-y-1">
              <p className="text-xs font-bold uppercase tracking-widest text-foreground">
                Gestion d&apos;identité externe
              </p>
              <p className="text-[11px] text-muted-foreground leading-relaxed">
                Ce backend ne stocke ni nom ni email de membre — l&apos;identité (nom, email) est gérée par un
                système externe. Seules les données réellement possédées par ce service (wallet, points, tier)
                sont affichées ici.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
