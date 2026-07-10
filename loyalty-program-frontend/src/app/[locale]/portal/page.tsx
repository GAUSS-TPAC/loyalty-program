"use client";

import {
  Coins,
  Target,
  Activity,
  ArrowUpRight,
  ArrowDownRight,
  ShieldCheck,
  Zap,
  LayoutDashboard,
  RefreshCw,
  ChevronRight
} from "lucide-react";
import { useTranslations } from "next-intl";
import { useAdminMembers, useRules, useBackendHealth } from "@/hooks/useBackend";

export default function DashboardPage() {
  const t = useTranslations("Dashboard");

  // Real data hooks
  const { data: members, isLoading: membersLoading, refetch: refetchMembers } = useAdminMembers(0, 100);
  const { data: rules, isLoading: rulesLoading, refetch: refetchRules } = useRules();
  const { data: health, isLoading: healthLoading, error: healthError, refetch: refetchHealth } = useBackendHealth();

  const handleRefresh = () => {
    refetchMembers();
    refetchRules();
    refetchHealth();
  };

  // Metrics calculation
  const activeRulesCount = rules?.filter(r => r.status === 'ACTIVE').length ?? 0;
  // Tant que le premier health check n'a pas répondu, le statut est inconnu —
  // ne pas afficher "OFFLINE" avant d'avoir une réponse réelle du backend.
  const healthChecked = health !== null || healthError !== null;
  const isSystemHealthy = health?.status === 'UP';
  const globalWalletBalance = members?.reduce((sum, m) => sum + m.balance, 0) ?? 0;
  const walletCurrency = members?.[0]?.currencyCode ?? "";

  const stats = [
    {
      title: "Solde Global Wallet",
      value: members ? `${globalWalletBalance.toLocaleString()} ${walletCurrency}` : "—",
      change: "+12.5%",
      isPositive: true,
      icon: Coins,
      loading: membersLoading
    },
    {
      title: "Règles Actives",
      value: activeRulesCount.toString(),
      change: `sur ${rules?.length ?? 0} totales`,
      isPositive: true,
      icon: Zap,
      loading: rulesLoading
    },
    {
      title: "Statut Système",
      value: isSystemHealthy ? "Opérationnel" : "Alerte",
      change: isSystemHealthy ? "Flux optimal" : "Vérifiez logs",
      isPositive: isSystemHealthy,
      icon: Activity,
      loading: healthLoading
    },
    {
      title: "Taux d'Engagement",
      value: "100%",
      change: "+2.1%",
      isPositive: true,
      icon: Target,
      loading: false
    }
  ];

  return (
    <div className="space-y-8">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            <span className="text-primary">{t("welcome")}</span>, Admin
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Dashboard consolidé du programme de fidélité.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            className="p-2 text-muted-foreground hover:bg-secondary rounded-lg transition-colors border border-border"
            title="Rafraîchir les données"
          >
            <RefreshCw className={`w-4 h-4 ${membersLoading || rulesLoading || healthLoading ? 'animate-spin' : ''}`} />
          </button>
          <div className={`px-4 py-2 rounded-full flex items-center gap-2.5 border text-xs font-bold tracking-tight shadow-sm ${!healthChecked ? 'bg-amber-50 border-amber-200 text-amber-700' : isSystemHealthy ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-rose-50 border-rose-200 text-rose-700'
            }`}>
            <div className={`w-2.5 h-2.5 rounded-full ${!healthChecked ? 'bg-amber-500 animate-pulse' : isSystemHealthy ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
            BACKEND : {!healthChecked ? 'VÉRIFICATION...' : isSystemHealthy ? 'ONLINE' : 'OFFLINE'}
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, i) => (
          <div key={i} className="bg-card border border-border rounded-2xl p-6 shadow-sm hover:shadow-md transition-all group relative overflow-hidden">
            <div className="absolute top-0 right-0 w-24 h-24 bg-primary/5 rounded-full -mr-12 -mt-12 transition-transform group-hover:scale-150 duration-500" />

            <div className="flex items-center justify-between mb-4">
              <div className="p-3 bg-secondary rounded-xl group-hover:bg-primary/20 transition-colors relative z-10">
                <stat.icon className="w-5 h-5 text-primary" />
              </div>
              {stat.loading ? (
                <div className="h-4 w-12 bg-muted animate-pulse rounded" />
              ) : (
                <div className={`flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider ${stat.isPositive ? 'text-emerald-600' : 'text-rose-600'}`}>
                  {stat.isPositive ? <ArrowUpRight className="w-3 h-3" /> : <ArrowDownRight className="w-3 h-3" />}
                  {stat.change}
                </div>
              )}
            </div>
            <div className="space-y-1 relative z-10">
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">{stat.title}</p>
              {stat.loading ? (
                <div className="h-8 w-32 bg-muted animate-pulse rounded mt-1" />
              ) : (
                <p className="text-2xl font-bold tracking-tight text-foreground font-mono">{stat.value}</p>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

        {/* System Health Summary */}
        <div className="lg:col-span-2 border border-border bg-card rounded-2xl shadow-sm overflow-hidden flex flex-col">
          <div className="bg-secondary/50 px-6 py-4 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-3 font-bold text-foreground">
              <Activity className="w-5 h-5 text-primary" />
              Services Infrastructure
            </div>
            <span className="text-[10px] font-bold bg-primary/10 text-primary px-2 py-0.5 rounded border border-primary/20">AWS CAMEROUN</span>
          </div>

          <div className="p-8 space-y-8 flex-1">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
              <div className="space-y-5">
                {health?.components ? (
                  Object.entries(health.components).map(([name, comp]) => (
                    <div key={name} className="space-y-2.5">
                      <div className="flex justify-between items-center text-xs font-bold uppercase tracking-widest text-muted-foreground">
                        <span>{name}</span>
                        <span className={`font-mono ${comp.status === 'UP' ? 'text-emerald-600' : 'text-rose-600'}`}>{comp.status}</span>
                      </div>
                      <div className="h-2 bg-secondary rounded-full overflow-hidden">
                        <div className={`h-full w-full transition-all duration-1000 ${comp.status === 'UP' ? 'bg-emerald-500' : 'bg-rose-500'}`} />
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="space-y-2.5">
                    <div className="flex justify-between items-center text-xs font-bold uppercase tracking-widest text-muted-foreground">
                      <span>API Backend</span>
                      <span className={`font-mono ${!healthChecked ? 'text-amber-600' : isSystemHealthy ? 'text-emerald-600' : 'text-rose-600'}`}>
                        {!healthChecked ? 'VÉRIFICATION' : isSystemHealthy ? 'UP' : 'DOWN'}
                      </span>
                    </div>
                    <div className="h-2 bg-secondary rounded-full overflow-hidden">
                      <div className={`h-full transition-all duration-1000 ${!healthChecked ? 'bg-amber-500 w-1/2' : isSystemHealthy ? 'bg-emerald-500 w-full' : 'bg-rose-500 w-full'}`} />
                    </div>
                    <p className="text-[10px] text-muted-foreground normal-case tracking-normal pt-1">
                      Détail par composant (DB, Kafka, Redis) indisponible : /actuator/health n&apos;expose pas les sous-systèmes hors accès authentifié.
                    </p>
                  </div>
                )}
              </div>

              <div className="bg-muted/30 rounded-2xl p-6 border border-border flex flex-col justify-center space-y-4">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-card rounded-xl shadow-sm border border-border">
                    <ShieldCheck className="w-6 h-6 text-emerald-600" />
                  </div>
                  <div>
                    <p className="font-bold text-sm">Protection Idempotente</p>
                    <p className="text-[11px] text-muted-foreground">Redis Cache actif pour éviter les doubles transactions.</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="p-2.5 bg-card rounded-xl shadow-sm border border-border">
                    <LayoutDashboard className="w-6 h-6 text-primary" />
                  </div>
                  <div>
                    <p className="font-bold text-sm">Gestion des Tenances</p>
                    <p className="text-[11px] text-muted-foreground">Isolation stricte des données par TenantContextHolder.</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="px-6 py-4 bg-muted/20 border-t border-border flex justify-between items-center">
            <span className="text-[10px] text-muted-foreground font-mono uppercase tracking-widest">Version v1.2.4-stable</span>
            <button className="text-[10px] uppercase font-bold text-primary hover:underline flex items-center gap-1.5">
              Consulter Actuator <ChevronRight className="w-3 h-3" />
            </button>
          </div>
        </div>

        {/* Quick Actions / Moteur */}
        <div className="space-y-8">
          <div className="bg-primary text-primary-foreground rounded-2xl p-8 shadow-xl shadow-primary/20 relative overflow-hidden flex flex-col justify-between group">
            <div className="absolute -bottom-8 -right-8 opacity-10 group-hover:scale-110 transition-transform duration-700">
              <Zap className="w-48 h-48 stroke-[1]" />
            </div>

            <div className="space-y-4 relative z-10">
              <div className="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
                <Zap className="w-6 h-6 fill-white" />
              </div>
              <div className="space-y-2">
                <h3 className="text-xl font-bold tracking-tight">Moteur de Règles</h3>
                <p className="text-sm opacity-80 leading-relaxed">
                  Le moteur de fidélité est prêt à transformer vos transactions en récompenses.
                </p>
              </div>
            </div>

            <div className="pt-8 relative z-10">
              <button className="w-full bg-white text-primary py-3 rounded-xl font-bold text-sm shadow-lg hover:shadow-xl transition-all active:scale-95">
                Lancer un test Event
              </button>
            </div>
          </div>

          <div className="border border-border bg-card rounded-2xl p-6 shadow-sm flex items-center gap-4">
            <div className="w-3 h-3 rounded-full bg-emerald-500 animate-pulse ring-4 ring-emerald-500/20" />
            <div>
              <p className="text-xs font-bold text-foreground">Worker Kafka</p>
              <p className="text-[10px] text-muted-foreground">En attente d&apos;événements de bonification.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
