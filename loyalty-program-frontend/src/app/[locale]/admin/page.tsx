"use client";

import { Building2, Coins, RefreshCw, AlertTriangle, CheckCircle2 } from "lucide-react";
import { usePlatformTenants } from "@/hooks/useBackend";

const STATUS_STYLES: Record<string, string> = {
  ACTIVE: "bg-emerald-50 border-emerald-200 text-emerald-700",
  TRIAL: "bg-blue-50 border-blue-200 text-blue-700",
  PAST_DUE: "bg-amber-50 border-amber-200 text-amber-700",
  CANCELLED: "bg-muted border-border text-muted-foreground",
  EXPIRED: "bg-muted border-border text-muted-foreground",
};

export default function PlatformOrganisationsPage() {
  const { data: tenants, isLoading, error, refetch } = usePlatformTenants();

  const totalsByCurrency = (tenants ?? []).reduce<Record<string, number>>((acc, t) => {
    acc[t.currency] = (acc[t.currency] ?? 0) + t.totalPaidAmount;
    return acc;
  }, {});
  const activeCount = tenants?.filter((t) => t.subscriptionStatus === "ACTIVE").length ?? 0;

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            Organisations
          </h1>
          <p className="text-muted-foreground text-sm">
            Tenants abonnés au service loyalty (Kernel Core + facturation locale).
          </p>
        </div>
        <button
          onClick={refetch}
          className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? "animate-spin" : ""}`} /> Rafraîchir
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-secondary rounded-xl">
              <Building2 className="w-5 h-5 text-primary" />
            </div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
              Organisations abonnées
            </p>
          </div>
          <p className="text-2xl font-bold font-mono">{tenants?.length ?? "—"}</p>
        </div>
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-secondary rounded-xl">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            </div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
              Abonnements actifs
            </p>
          </div>
          <p className="text-2xl font-bold font-mono">{activeCount}</p>
        </div>
        <div className="bg-card border border-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2.5 bg-secondary rounded-xl">
              <Coins className="w-5 h-5 text-primary" />
            </div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
              Revenu encaissé (factures payées)
            </p>
          </div>
          {Object.keys(totalsByCurrency).length === 0 ? (
            <p className="text-2xl font-bold font-mono">—</p>
          ) : (
            <div className="space-y-0.5">
              {Object.entries(totalsByCurrency).map(([currency, total]) => (
                <p key={currency} className="text-2xl font-bold font-mono">
                  {total.toLocaleString()} {currency}
                </p>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Table */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5">
            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <div>
              <p className="font-semibold">Impossible de charger les organisations</p>
              <p className="text-xs mt-0.5 text-destructive/80">{error}</p>
            </div>
          </div>
        ) : !tenants || tenants.length === 0 ? (
          <div className="p-8 text-center text-sm text-muted-foreground italic">
            Aucune organisation abonnée au service loyalty pour l&apos;instant.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">Organisation</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Plan</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Statut</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Fin de période</th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">Payé</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {tenants.map((t, i) => (
                  <tr key={t.tenantId} className={i % 2 === 0 ? "bg-background" : "bg-muted/10"}>
                    <td className="px-6 py-4">
                      <p className="font-medium text-foreground">{t.tenantName}</p>
                      <p className="text-xs font-mono text-muted-foreground">{t.tenantId}</p>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-mono text-primary bg-primary/10 px-2 py-1 rounded-md">
                        {t.planName} ({t.planCode})
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium border ${STATUS_STYLES[t.subscriptionStatus] ?? "bg-muted border-border text-muted-foreground"
                          }`}
                      >
                        {t.subscriptionStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-muted-foreground text-xs">
                      {t.currentPeriodEnd ? new Date(t.currentPeriodEnd).toLocaleDateString("fr-FR") : "—"}
                    </td>
                    <td className="px-6 py-4 font-mono font-semibold text-right">
                      {t.totalPaidAmount.toLocaleString()} {t.currency}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
