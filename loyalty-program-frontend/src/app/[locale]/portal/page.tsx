"use client";

import { useState } from "react";
import {
  Coins,
  Target,
  Activity,
  ShieldCheck,
  Zap,
  Key,
  Gift,
  RefreshCw,
  History,
  Filter,
  AppWindow,
  Info,
  ArrowUpCircle,
  ArrowDownCircle,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { Link } from "@/i18n/routing";
import { useAdminMembers, useRules, useBackendHealth, useAdminLogs } from "@/hooks/useBackend";

type TxFilter = "ALL" | "CREDIT" | "DEBIT";

const FILTER_LABELS: Record<TxFilter, string> = {
  ALL: "Toutes",
  CREDIT: "Crédits",
  DEBIT: "Débits",
};

export default function DashboardPage() {
  const t = useTranslations("Dashboard");

  // Real data hooks
  const { data: members, isLoading: membersLoading, refetch: refetchMembers } = useAdminMembers(0, 100);
  const { data: rules, isLoading: rulesLoading, refetch: refetchRules } = useRules();
  const { data: health, isLoading: healthLoading, error: healthError, refetch: refetchHealth } = useBackendHealth();
  const { data: logs, isLoading: logsLoading, refetch: refetchLogs } = useAdminLogs(0, 10);

  const [txFilter, setTxFilter] = useState<TxFilter>("ALL");

  const handleRefresh = () => {
    refetchMembers();
    refetchRules();
    refetchHealth();
    refetchLogs();
  };

  // Metrics
  const activeRulesCount = rules?.filter(r => r.status === 'ACTIVE').length ?? 0;
  // Tant que le premier health check n'a pas répondu, le statut est inconnu —
  // ne pas afficher "OFFLINE" avant d'avoir une réponse réelle du backend.
  const healthChecked = health !== null || healthError !== null;
  const isSystemHealthy = health?.status === 'UP';
  const globalWalletBalance = members?.reduce((sum, m) => sum + m.balance, 0) ?? 0;
  const walletCurrency = members?.[0]?.currencyCode ?? "FCFA";

  const cycleFilter = () =>
    setTxFilter((f) => (f === "ALL" ? "CREDIT" : f === "CREDIT" ? "DEBIT" : "ALL"));

  const filteredLogs = (logs ?? []).filter(
    (l) => txFilter === "ALL" || l.type === txFilter
  );

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            <span className="text-primary">{t("welcome")}</span>
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Espace développeur — vue d&apos;ensemble de votre tenant.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            className="p-2 text-muted-foreground hover:bg-secondary rounded-lg transition-colors border border-border"
            title="Rafraîchir les données"
          >
            <RefreshCw className={`w-4 h-4 ${membersLoading || rulesLoading || healthLoading || logsLoading ? 'animate-spin' : ''}`} />
          </button>
          <div className={`px-4 py-2 rounded-full flex items-center gap-2.5 border text-xs font-bold tracking-tight shadow-sm ${!healthChecked ? 'bg-amber-50 border-amber-200 text-amber-700' : isSystemHealthy ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-rose-50 border-rose-200 text-rose-700'
            }`}>
            <div className={`w-2.5 h-2.5 rounded-full ${!healthChecked ? 'bg-amber-500 animate-pulse' : isSystemHealthy ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
            BACKEND : {!healthChecked ? 'VÉRIFICATION...' : isSystemHealthy ? 'ONLINE' : 'OFFLINE'}
          </div>
        </div>
      </div>

      {/* Bannière info (style "Info sécurité" My-CoolPay) */}
      <div className="bg-emerald-50 border border-emerald-200 rounded-xl px-6 py-5 space-y-3">
        <div className="flex items-center gap-2.5">
          <Info className="w-5 h-5 text-emerald-700" />
          <h3 className="font-bold text-emerald-900">Info sécurité</h3>
        </div>
        <p className="text-sm text-emerald-800">
          Protégez vos intégrations : utilisez une clé API en mode <strong>TEST</strong> pour vos
          développements et réservez la clé <strong>LIVE</strong> à la production.
        </p>
        <Link
          href="/portal/api-keys"
          className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm"
        >
          <Key className="w-4 h-4" />
          Gérer les clés API
        </Link>
      </div>

      {/* Carte solde + tuiles stats (style "Mon solde" My-CoolPay) */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm text-center space-y-4 flex flex-col justify-between">
          <div className="space-y-2">
            <p className="text-sm font-bold text-foreground">Solde global wallet</p>
            {membersLoading ? (
              <div className="h-9 w-28 bg-muted animate-pulse rounded mx-auto" />
            ) : (
              <p className="text-3xl font-bold tracking-tight text-foreground">
                {globalWalletBalance.toLocaleString()}{" "}
                <span className="text-xs font-semibold text-muted-foreground uppercase">{walletCurrency}</span>
              </p>
            )}
          </div>
          <div className="flex items-center justify-center gap-6 pt-2 border-t border-border">
            <Link href="/portal/bonification" className="flex items-center gap-1.5 text-xs font-semibold text-primary hover:underline">
              <ArrowUpCircle className="w-4 h-4" /> Créditer
            </Link>
            <Link href="/portal/members" className="flex items-center gap-1.5 text-xs font-semibold text-primary hover:underline">
              <AppWindow className="w-4 h-4" /> Applications
            </Link>
          </div>
        </div>

        {[
          {
            title: "Membres",
            value: members?.length.toString() ?? "—",
            hint: "annuaire du tenant",
            icon: Target,
            loading: membersLoading,
          },
          {
            title: "Règles actives",
            value: activeRulesCount.toString(),
            hint: `sur ${rules?.length ?? 0} totales`,
            icon: Zap,
            loading: rulesLoading,
          },
          {
            title: "Statut système",
            value: !healthChecked ? "…" : isSystemHealthy ? "Opérationnel" : "Alerte",
            hint: !healthChecked ? "vérification" : isSystemHealthy ? "flux optimal" : "vérifiez les logs",
            icon: Activity,
            loading: healthLoading,
          },
        ].map((stat) => (
          <div key={stat.title} className="bg-card border border-border rounded-2xl p-6 shadow-sm hover:shadow-md transition-all group relative overflow-hidden">
            <div className="absolute top-0 right-0 w-24 h-24 bg-primary/5 rounded-full -mr-12 -mt-12 transition-transform group-hover:scale-150 duration-500" />
            <div className="p-3 bg-secondary rounded-xl group-hover:bg-primary/20 transition-colors relative z-10 w-fit mb-4">
              <stat.icon className="w-5 h-5 text-primary" />
            </div>
            <div className="space-y-1 relative z-10">
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">{stat.title}</p>
              {stat.loading ? (
                <div className="h-8 w-24 bg-muted animate-pulse rounded mt-1" />
              ) : (
                <p className="text-2xl font-bold tracking-tight text-foreground font-mono">{stat.value}</p>
              )}
              <p className="text-[10px] text-muted-foreground">{stat.hint}</p>
            </div>
          </div>
        ))}
      </div>

      {/* En-tête section Historique (style My-CoolPay) */}
      <div className="flex items-start gap-4 pt-2">
        <div className="p-4 bg-card border border-border rounded-xl shadow-sm">
          <History className="w-6 h-6 text-foreground" />
        </div>
        <div className="space-y-1">
          <h2 className="text-xl font-bold tracking-tight text-foreground">Historique</h2>
          <p className="text-sm text-muted-foreground">
            Voici les dernières transactions de points traitées par le moteur de règles de votre tenant.
          </p>
        </div>
      </div>

      {/* Table des transactions (style "MES TRANSACTIONS" My-CoolPay) */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-border flex items-center justify-between gap-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-foreground">
            Mes transactions
          </h3>
          <button
            onClick={cycleFilter}
            className="inline-flex items-center gap-2 bg-emerald-600 text-white px-4 py-2 rounded-lg text-xs font-semibold hover:bg-emerald-700 transition-all shadow-sm"
            title="Basculer le filtre Toutes / Crédits / Débits"
          >
            <Filter className="w-3.5 h-3.5" />
            Filtrer la liste{txFilter !== "ALL" ? ` : ${FILTER_LABELS[txFilter]}` : ""}
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs font-bold text-foreground border-b border-border">
                <th className="text-left px-6 py-3">Description</th>
                <th className="text-left px-4 py-3">Effectuée le</th>
                <th className="text-left px-4 py-3">Via</th>
                <th className="text-left px-4 py-3">Statut</th>
                <th className="text-right px-4 py-3">Montant</th>
                <th className="text-right px-6 py-3">Solde après</th>
              </tr>
            </thead>
            <tbody>
              {logsLoading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-6">
                    <div className="h-5 bg-muted animate-pulse rounded" />
                  </td>
                </tr>
              ) : filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-6 text-center text-sm text-muted-foreground bg-muted/20">
                    Aucune transaction pour le moment
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log) => (
                  <tr key={log.id} className="border-b border-border last:border-0 hover:bg-muted/10 transition-colors">
                    <td className="px-6 py-3.5">
                      <div className="flex items-center gap-2.5">
                        {log.type === "CREDIT" ? (
                          <ArrowUpCircle className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                        ) : (
                          <ArrowDownCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
                        )}
                        <span className="font-medium text-foreground">
                          {log.type === "CREDIT" ? "Crédit de points" : "Débit de points"}
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3.5 text-muted-foreground whitespace-nowrap">
                      {new Date(log.createdAt).toLocaleString("fr-FR", {
                        dateStyle: "short",
                        timeStyle: "short",
                      })}
                    </td>
                    <td className="px-4 py-3.5">
                      <span className="text-xs font-mono text-muted-foreground">{log.source}</span>
                    </td>
                    <td className="px-4 py-3.5">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-700 border border-emerald-500/20">
                        Complétée
                      </span>
                    </td>
                    <td className={`px-4 py-3.5 text-right font-mono font-bold ${log.type === "CREDIT" ? "text-emerald-600" : "text-rose-600"}`}>
                      {log.type === "CREDIT" ? "+" : "-"}{log.amount.toLocaleString()} pts
                    </td>
                    <td className="px-6 py-3.5 text-right font-mono text-muted-foreground">
                      {log.balanceAfter.toLocaleString()} pts
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="px-6 py-3 bg-muted/20 border-t border-border flex justify-between items-center">
          <span className="text-[10px] text-muted-foreground font-mono uppercase tracking-widest">
            {filteredLogs.length} transaction{filteredLogs.length > 1 ? "s" : ""} affichée{filteredLogs.length > 1 ? "s" : ""}
          </span>
          <Link href="/portal/logs" className="text-[10px] uppercase font-bold text-primary hover:underline">
            Voir tout le journal
          </Link>
        </div>
      </div>

      {/* Raccourcis */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { href: "/portal/bonification", icon: Gift, label: "Bonification" },
          { href: "/portal/rules", icon: Zap, label: "Règles" },
          { href: "/portal/wallet/config", icon: Coins, label: "Wallet" },
          { href: "/portal/api-keys", icon: ShieldCheck, label: "Clés API" },
        ].map(({ href, icon: Icon, label }) => (
          <Link
            key={href}
            href={href}
            className="border border-border bg-card rounded-2xl p-4 shadow-sm flex flex-col items-center gap-2 text-center hover:border-primary/50 transition-colors"
          >
            <Icon className="w-5 h-5 text-primary" />
            <span className="text-xs font-bold text-foreground">{label}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
