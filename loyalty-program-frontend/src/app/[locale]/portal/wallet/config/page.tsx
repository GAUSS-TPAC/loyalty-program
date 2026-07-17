"use client";

import { useState } from "react";
import {
  Wallet,
  ShieldAlert,
  ShieldCheck,
  Clock,
  AlertTriangle,
  RefreshCw,
  ArrowUpRight,
  ArrowDownLeft,
  User,
  Award,
  TrendingUp,
} from "lucide-react";
import {
  useAdminMembers,
  useMemberWallet,
  useMemberWalletTransactions,
  useMemberPoints,
  useTierPolicy,
} from "@/hooks/useBackend";
import { PointsConversionCard } from "@/components/wallet/PointsConversionCard";
import { TIER_LABELS, computeTierProgress } from "@/lib/tierProgress";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
} from "@/components/ui/sheet";
import type { WalletTransaction } from "@/lib/api";

// ─── Badge statut wallet ──────────────────────────────────────────────────────

function WalletStatusBadge({ status }: { status: string }) {
  if (status === "ACTIVE")
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-green-100 text-green-700 border border-green-200">
        <ShieldCheck className="w-4 h-4" /> Actif
      </span>
    );
  if (status === "FROZEN")
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-destructive/10 text-destructive border border-destructive/20">
        <ShieldAlert className="w-4 h-4" /> Gelé
      </span>
    );
  if (status === "PENDING_KYC")
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-orange-100 text-orange-700 border border-orange-200">
        <Clock className="w-4 h-4" /> En attente KYC
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-muted text-muted-foreground border border-border">
      {status}
    </span>
  );
}

// ─── Panneau de détail d'une transaction ───────────────────────────────────────

function TransactionDetailSheet({
  transaction,
  currencyCode,
  onClose,
}: {
  transaction: WalletTransaction | null;
  currencyCode: string;
  onClose: () => void;
}) {
  const isCredit = transaction ? transaction.amount > 0 : false;

  const rows = transaction
    ? [
      { label: "ID Transaction", value: transaction.id, mono: true },
      { label: "Type", value: transaction.type },
      { label: "Source", value: transaction.source },
      { label: "Statut", value: transaction.status },
      {
        label: "Montant",
        value: `${isCredit ? "+" : ""}${transaction.amount.toLocaleString("fr-FR")} ${currencyCode}`,
      },
      {
        label: "Solde avant",
        value: `${transaction.balanceBefore.toLocaleString("fr-FR")} ${currencyCode}`,
      },
      {
        label: "Solde après",
        value: `${transaction.balanceAfter.toLocaleString("fr-FR")} ${currencyCode}`,
      },
      {
        label: "Date",
        value: new Date(transaction.createdAt).toLocaleString("fr-FR", {
          dateStyle: "long",
          timeStyle: "medium",
        }),
      },
    ]
    : [];

  return (
    <Sheet open={transaction !== null} onOpenChange={(open) => !open && onClose()}>
      <SheetContent>
        <SheetHeader>
          <SheetTitle>Détail de la transaction</SheetTitle>
          <SheetDescription>
            Toutes les informations enregistrées pour cette opération de wallet.
          </SheetDescription>
        </SheetHeader>
        <div className="px-4 pb-4 space-y-1">
          {rows.map(({ label, value, mono }) => (
            <div
              key={label}
              className="flex items-start justify-between gap-4 py-2.5 border-b border-border last:border-0"
            >
              <span className="text-xs text-muted-foreground shrink-0">{label}</span>
              <span
                className={`text-sm text-foreground text-right break-all ${mono ? "font-mono text-xs" : "font-medium"
                  }`}
              >
                {value}
              </span>
            </div>
          ))}
        </div>
      </SheetContent>
    </Sheet>
  );
}

// ─── Composant principal ───────────────────────────────────────────────────────

export default function WalletConfigPage() {
  const { data: members, isLoading: membersLoading } = useAdminMembers(0, 100);
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  // Tant que l'utilisateur n'a rien choisi explicitement, on retombe sur le premier
  // membre de la liste (dérivé au rendu, pas de setState dans un effet).
  const effectiveMemberId = selectedMemberId ?? members?.[0]?.memberId ?? null;
  const [selectedTx, setSelectedTx] = useState<WalletTransaction | null>(null);

  const {
    data: wallet,
    isLoading: walletLoading,
    error: walletError,
    refetch: refetchWallet,
  } = useMemberWallet(effectiveMemberId);
  const {
    data: transactions,
    isLoading: txLoading,
    error: txError,
    refetch: refetchTx,
  } = useMemberWalletTransactions(effectiveMemberId, 0, 20);
  const {
    data: points,
    isLoading: pointsLoading,
    refetch: refetchPoints,
  } = useMemberPoints(effectiveMemberId);
  const { data: tierPolicy } = useTierPolicy();
  const tierProgress = points
    ? computeTierProgress(points.lifetimeEarned, tierPolicy?.thresholds)
    : null;

  const policy = wallet
    ? {
      currencyCode: wallet.currencyCode,
      dailySpendCap: wallet.dailySpendCap,
      maxBalance: wallet.maxBalance,
      otpThreshold: wallet.otpThreshold,
      kycRequired: wallet.kycRequiredForWithdrawal,
    }
    : null;

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">
            Portefeuille & Politique
          </h1>
          <p className="text-muted-foreground text-sm">
            Points, solde, correspondance et historique des transactions du membre.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <User className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
            <select
              value={effectiveMemberId ?? ""}
              onChange={(e) => setSelectedMemberId(e.target.value || null)}
              disabled={membersLoading || !members || members.length === 0}
              className="appearance-none text-xs font-medium border border-border rounded-lg pl-8 pr-3 py-2 bg-card hover:bg-secondary transition-all disabled:opacity-50 font-mono"
            >
              {membersLoading && <option>Chargement…</option>}
              {!membersLoading && (!members || members.length === 0) && (
                <option>Aucun membre</option>
              )}
              {members?.map((m) => (
                <option key={m.memberId} value={m.memberId}>
                  {m.memberId.substring(0, 8)}… ({m.balance.toLocaleString()} {m.currencyCode})
                </option>
              ))}
            </select>
          </div>
          <button
            onClick={() => {
              refetchWallet();
              refetchTx();
              refetchPoints();
            }}
            className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Rafraîchir
          </button>
        </div>
      </div>

      {/* ── Erreur ──────────────────────────────────────────────────────────── */}
      {walletError && (
        <div className="p-4 flex items-start gap-3 text-sm text-destructive bg-destructive/5 border border-destructive/20 rounded-xl">
          <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
          <div>
            <p className="font-semibold">Impossible de charger le wallet</p>
            <p className="text-xs mt-0.5 text-destructive/80">{walletError}</p>
            <p className="text-xs mt-1 text-muted-foreground">
              Vérifiez que le backend Spring Boot est démarré (port 8081) et
              qu&apos;un token JWT valide est présent.
            </p>
          </div>
        </div>
      )}

      {/* ── Carte points de fidélité ─────────────────────────────────────────── */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="bg-secondary px-5 py-3.5 border-b border-border flex items-center gap-2">
          <Award className="w-4 h-4 text-primary" />
          <h3 className="font-semibold text-sm text-foreground">Points de fidélité</h3>
        </div>
        <div className="p-5">
          {pointsLoading ? (
            <div className="h-12 bg-muted rounded-lg animate-pulse" />
          ) : points ? (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <p className="text-xs text-muted-foreground">Solde de points</p>
                <p className="text-2xl font-bold text-foreground font-mono">
                  {points.availablePoints.toLocaleString("fr-FR")} pts
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Niveau</p>
                <p className="text-sm font-semibold text-foreground mt-1">
                  {TIER_LABELS[points.tierLevel]}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground flex items-center gap-1">
                  <TrendingUp className="w-3 h-3" /> Progression vers le niveau suivant
                </p>
                <div className="mt-1.5 h-2 bg-muted rounded-full overflow-hidden">
                  <div
                    className="h-full bg-primary rounded-full transition-all"
                    style={{ width: `${tierProgress?.progressPercent ?? 0}%` }}
                  />
                </div>
                <p className="text-[10px] text-muted-foreground mt-1">
                  {tierProgress && tierProgress.pointsToNext > 0
                    ? `${tierProgress.pointsToNext.toLocaleString("fr-FR")} pts restants`
                    : "Niveau maximum atteint"}
                </p>
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">— Aucune donnée de points —</p>
          )}
        </div>
      </div>

      {/* ── Correspondance des points (taux de conversion du tenant) ─────────── */}
      <PointsConversionCard />

      {/* ── Carte solde ──────────────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Solde */}
        <div className="md:col-span-2 border border-border bg-card rounded-xl p-6 shadow-sm flex flex-col justify-between gap-4">
          <div className="flex items-center justify-between">
            <p className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">
              Solde actuel
            </p>
            {wallet && <WalletStatusBadge status={wallet.status} />}
          </div>

          {walletLoading ? (
            <div className="h-12 bg-muted rounded-lg animate-pulse" />
          ) : wallet ? (
            <p className="text-4xl font-bold text-foreground font-mono">
              {wallet.balance.toLocaleString("fr-FR")}{" "}
              <span className="text-xl font-semibold text-muted-foreground">
                {wallet.currencyCode}
              </span>
            </p>
          ) : (
            <p className="text-4xl font-bold text-muted/50 font-mono">— CR</p>
          )}

          <div className="grid grid-cols-2 gap-3 pt-2 border-t border-border">
            <div>
              <p className="text-xs text-muted-foreground">ID Wallet</p>
              <p className="text-xs font-mono text-foreground mt-0.5 truncate">
                {wallet?.id ?? "—"}
              </p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Dernière MAJ</p>
              <p className="text-xs font-mono text-foreground mt-0.5">
                {wallet?.updatedAt
                  ? new Date(wallet.updatedAt).toLocaleString("fr-FR")
                  : "—"}
              </p>
            </div>
          </div>
        </div>

        {/* Politique */}
        <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
          <div className="bg-secondary px-5 py-3.5 border-b border-border flex items-center gap-2">
            <Wallet className="w-4 h-4 text-primary" />
            <h3 className="font-semibold text-sm text-foreground">
              Politique tenant
            </h3>
          </div>
          <div className="p-5 space-y-3">
            {walletLoading &&
              [1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="h-5 bg-muted rounded animate-pulse"
                />
              ))}
            {policy && (
              <>
                {[
                  {
                    label: "Plafond journalier",
                    val: policy.dailySpendCap
                      ? `${policy.dailySpendCap.toLocaleString()} ${policy.currencyCode}`
                      : "Illimité",
                  },
                  {
                    label: "Solde maximum",
                    val: policy.maxBalance
                      ? `${policy.maxBalance.toLocaleString()} ${policy.currencyCode}`
                      : "Illimité",
                  },
                  {
                    label: "Seuil OTP",
                    val: policy.otpThreshold
                      ? `≥ ${policy.otpThreshold.toLocaleString()} ${policy.currencyCode}`
                      : "Désactivé",
                  },
                  {
                    label: "KYC requis (retrait)",
                    val: policy.kycRequired ? "Oui" : "Non",
                  },
                ].map(({ label, val }) => (
                  <div
                    key={label}
                    className="flex items-center justify-between text-sm"
                  >
                    <span className="text-muted-foreground text-xs">
                      {label}
                    </span>
                    <span className="font-medium text-foreground text-xs">
                      {val}
                    </span>
                  </div>
                ))}
              </>
            )}
          </div>
        </div>
      </div>

      {/* ── Historique des transactions ───────────────────────────────────── */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
          <Clock className="w-5 h-5 text-primary" />
          <h3 className="font-semibold text-foreground">
            Historique des transactions
          </h3>
          <span className="text-xs text-muted-foreground font-normal ml-1">
            (cliquez une ligne pour le détail)
          </span>
        </div>

        {/* Loading */}
        {txLoading && (
          <div className="p-6 space-y-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />
            ))}
          </div>
        )}

        {/* Erreur */}
        {txError && (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5">
            <AlertTriangle className="w-4 h-4 mt-0.5" />
            <p>{txError}</p>
          </div>
        )}

        {/* Vide */}
        {!txLoading && !txError && (!transactions || transactions.length === 0) && (
          <div className="p-8 text-center text-muted-foreground text-sm italic">
            Aucune transaction trouvée.
          </div>
        )}

        {/* Données */}
        {!txLoading && transactions && transactions.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Source
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">
                    Montant
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">
                    Solde après
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {transactions.map((tx, index) => {
                  const isCredit = tx.amount > 0;
                  return (
                    <tr
                      key={tx.id}
                      onClick={() => setSelectedTx(tx)}
                      className={`hover:bg-secondary/50 transition-colors cursor-pointer ${index % 2 === 0 ? "bg-background" : "bg-muted/10"
                        }`}
                    >
                      <td className="px-6 py-4 text-xs text-muted-foreground font-mono whitespace-nowrap">
                        {new Date(tx.createdAt).toLocaleString("fr-FR")}
                      </td>
                      <td className="px-6 py-4 text-xs font-mono text-muted-foreground">
                        {tx.id.substring(0, 8)}…
                      </td>
                      <td className="px-6 py-4">
                        <span className="text-xs font-medium text-primary bg-primary/10 px-2 py-1 rounded-md">
                          {tx.type}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-xs text-muted-foreground font-mono">
                        {tx.source}
                      </td>
                      <td
                        className={`px-6 py-4 font-mono font-semibold text-right text-sm ${isCredit ? "text-green-600" : "text-foreground"
                          }`}
                      >
                        <span className="inline-flex items-center gap-1 justify-end">
                          {isCredit ? (
                            <ArrowUpRight className="w-3.5 h-3.5" />
                          ) : (
                            <ArrowDownLeft className="w-3.5 h-3.5" />
                          )}
                          {isCredit ? "+" : ""}
                          {tx.amount.toLocaleString("fr-FR")}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-mono text-right text-sm">
                        {tx.balanceAfter?.toLocaleString("fr-FR") ?? "—"}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Note si backend non démarré */}
      {!walletLoading && !walletError && !wallet && (
        <div className="p-4 text-xs text-muted-foreground bg-muted/30 border border-border rounded-xl flex items-start gap-2">
          <AlertTriangle className="w-4 h-4 text-orange-500 flex-shrink-0 mt-0.5" />
          <span>
            <strong className="text-foreground">Backend non connecté.</strong>{" "}
            Démarrez le serveur Spring Boot et configurez un token JWT dans{" "}
            <code className="font-mono bg-muted px-1 rounded">sessionStorage(&quot;loyalty_jwt_token&quot;)</code>.
          </span>
        </div>
      )}

      <TransactionDetailSheet
        transaction={selectedTx}
        currencyCode={wallet?.currencyCode ?? ""}
        onClose={() => setSelectedTx(null)}
      />
    </div>
  );
}
