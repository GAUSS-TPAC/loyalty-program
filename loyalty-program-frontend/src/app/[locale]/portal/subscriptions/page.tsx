"use client";

import { useState } from "react";
import {
  CreditCard, RefreshCw, Info, AlertTriangle,
  CheckCircle, Star, Zap, Users, BarChart3, Tag, Megaphone, XCircle
} from "lucide-react";
import { subscriptionApi, type SubscriptionPlanResponse, type TenantSubscriptionResponse, type InvoiceResponse } from "@/lib/api";
import { useSubscriptionPlans, useMySubscription, useMyInvoices } from "@/hooks/useBackend";

function statusBadge(status: TenantSubscriptionResponse["status"]) {
  const map: Record<string, string> = {
    TRIAL:    "bg-blue-100 text-blue-700 border-blue-200",
    ACTIVE:   "bg-green-100 text-green-700 border-green-200",
    PAST_DUE: "bg-orange-100 text-orange-700 border-orange-200",
    CANCELLED:"bg-muted text-muted-foreground border-border",
    EXPIRED:  "bg-rose-100 text-rose-700 border-rose-200",
  };
  return <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium border ${map[status] ?? map.EXPIRED}`}>{status}</span>;
}

function invoiceStatusBadge(status: InvoiceResponse["status"]) {
  const map: Record<string, string> = {
    PENDING: "bg-orange-100 text-orange-700 border-orange-200",
    PAID:    "bg-green-100 text-green-700 border-green-200",
    FAILED:  "bg-rose-100 text-rose-700 border-rose-200",
    VOID:    "bg-muted text-muted-foreground border-border",
  };
  return <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium border ${map[status] ?? map.VOID}`}>{status}</span>;
}

function PlanCard({ plan, currentPlanId, onSubscribe, loading }: {
  plan: SubscriptionPlanResponse;
  currentPlanId?: string;
  onSubscribe: (planId: string) => void;
  loading: boolean;
}) {
  const isCurrent = plan.id === currentPlanId;
  const isPro = plan.code === "PRO";
  const isEnterprise = plan.code === "ENTERPRISE";

  const features = [
    { icon: Zap, label: `${plan.features.maxRules < 0 ? "Illimité" : plan.features.maxRules} règles` },
    { icon: Users, label: plan.features.maxMembers === 0 ? "Membres illimités" : `${plan.features.maxMembers.toLocaleString()} membres` },
    { icon: Tag, label: "Codes promo", enabled: plan.features.promoCodesEnabled },
    { icon: Megaphone, label: "Campagnes", enabled: plan.features.campaignsEnabled },
    { icon: BarChart3, label: "Analytics", enabled: plan.features.analyticsEnabled },
  ];

  return (
    <div className={`relative border rounded-2xl p-6 flex flex-col gap-4 transition-all ${isCurrent ? "border-primary shadow-lg shadow-primary/10 bg-primary/5" : isPro ? "border-primary/40 bg-card shadow-md" : "border-border bg-card shadow-sm"}`}>
      {isPro && !isCurrent && (
        <div className="absolute -top-3 left-1/2 -translate-x-1/2">
          <span className="inline-flex items-center gap-1 px-3 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-primary text-primary-foreground shadow">
            <Star className="w-3 h-3" /> Recommandé
          </span>
        </div>
      )}
      {isCurrent && (
        <div className="absolute -top-3 left-1/2 -translate-x-1/2">
          <span className="inline-flex items-center gap-1 px-3 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-emerald-500 text-white shadow">
            <CheckCircle className="w-3 h-3" /> Plan actuel
          </span>
        </div>
      )}

      <div className="space-y-1 pt-2">
        <h3 className="text-lg font-bold text-foreground">{plan.name}</h3>
        {plan.description && <p className="text-xs text-muted-foreground">{plan.description}</p>}
      </div>

      <div className="space-y-0.5">
        {plan.priceMonthly === 0 ? (
          <p className="text-3xl font-bold text-foreground">Gratuit</p>
        ) : (
          <>
            <p className="text-3xl font-bold text-foreground">
              {plan.priceMonthly.toLocaleString()} <span className="text-base font-normal text-muted-foreground">{plan.currency}/mois</span>
            </p>
            {plan.priceYearly > 0 && (
              <p className="text-xs text-muted-foreground">{plan.priceYearly.toLocaleString()} {plan.currency}/an</p>
            )}
          </>
        )}
      </div>

      <ul className="space-y-2 flex-1">
        {features.map((f, i) => (
          <li key={i} className={`flex items-center gap-2 text-sm ${f.enabled === false ? "text-muted-foreground/50 line-through" : "text-foreground"}`}>
            <f.icon className={`w-3.5 h-3.5 flex-shrink-0 ${f.enabled === false ? "text-muted-foreground/40" : "text-primary"}`} />
            {f.label}
          </li>
        ))}
      </ul>

      {!isCurrent && (
        <button
          onClick={() => onSubscribe(plan.id)}
          disabled={loading}
          className={`mt-2 w-full py-2.5 rounded-xl text-sm font-semibold transition-all active:scale-95 disabled:opacity-50 ${isPro || isEnterprise ? "bg-primary text-primary-foreground hover:bg-primary/90 shadow-md" : "bg-secondary text-foreground hover:bg-secondary/80 border border-border"}`}
        >
          {loading ? <RefreshCw className="w-4 h-4 animate-spin mx-auto" /> : plan.priceMonthly === 0 ? "Choisir ce plan" : "Souscrire"}
        </button>
      )}
    </div>
  );
}

export default function SubscriptionsPage() {
  const { data: plans, isLoading: plansLoading, error: plansError } = useSubscriptionPlans();
  const { data: subscription, isLoading: subLoading, error: subError, refetch: refetchSub } = useMySubscription();
  const { data: invoices, isLoading: invoicesLoading, refetch: refetchInvoices } = useMyInvoices();

  const [subscribingId, setSubscribingId] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  const handleSubscribe = async (planId: string) => {
    setSubscribingId(planId);
    try {
      if (subscription) {
        await subscriptionApi.changePlan(planId);
        showToast("Plan mis à jour !", "success");
      } else {
        await subscriptionApi.subscribe(planId, "MONTHLY");
        showToast("Abonnement activé !", "success");
      }
      refetchSub();
      refetchInvoices();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setSubscribingId(null);
    }
  };

  const handleCancel = async () => {
    if (!confirm("Confirmer l'annulation de l'abonnement ?")) return;
    setCancelling(true);
    try {
      await subscriptionApi.cancel();
      showToast("Abonnement annulé", "success");
      refetchSub();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setCancelling(false);
    }
  };

  const currentPlan = plans?.find(p => p.id === subscription?.planId);

  return (
    <div className="space-y-8">
      {toast && (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg border text-sm transition-all ${toast.type === "success" ? "bg-emerald-50 border-emerald-200 text-emerald-800" : "bg-rose-50 border-rose-200 text-rose-800"}`}>
          <Info className="w-4 h-4" />{toast.message}
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">Abonnement</h1>
          <p className="text-muted-foreground text-sm">Gérez votre plan SaaS et consultez vos factures.</p>
        </div>
        <button onClick={() => { refetchSub(); refetchInvoices(); }} className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all">
          <RefreshCw className="w-3.5 h-3.5" /> Rafraîchir
        </button>
      </div>

      {/* Abonnement courant */}
      {(subLoading || subscription) && (
        <div className="border border-border bg-card rounded-2xl shadow-sm overflow-hidden">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <CreditCard className="w-5 h-5 text-primary" />
              <h3 className="font-semibold">Abonnement actuel</h3>
            </div>
            {subscription && !["CANCELLED", "EXPIRED"].includes(subscription.status) && (
              <button onClick={handleCancel} disabled={cancelling} className="inline-flex items-center gap-1.5 text-xs font-medium text-rose-700 hover:bg-rose-50 border border-rose-200 px-3 py-1.5 rounded-md transition-colors disabled:opacity-50">
                {cancelling ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <XCircle className="w-3.5 h-3.5" />}
                Annuler l&apos;abonnement
              </button>
            )}
          </div>
          <div className="p-6">
            {subLoading ? (
              <div className="space-y-2">{[1,2,3].map(i => <div key={i} className="h-6 bg-muted rounded-lg animate-pulse" />)}</div>
            ) : subError ? (
              <div className="flex items-center gap-2 text-sm text-muted-foreground italic">
                <Info className="w-4 h-4" /> Aucun abonnement actif — choisissez un plan ci-dessous.
              </div>
            ) : subscription ? (
              <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                {[
                  { label: "Plan", value: currentPlan?.name ?? subscription.planId.substring(0, 8) },
                  { label: "Statut", value: statusBadge(subscription.status) },
                  { label: "Cycle", value: subscription.billingCycle },
                  { label: "Renouvellement", value: new Date(subscription.currentPeriodEnd).toLocaleDateString("fr-FR") },
                ].map((item, i) => (
                  <div key={i} className="space-y-1">
                    <p className="text-xs font-semibold uppercase text-muted-foreground tracking-widest">{item.label}</p>
                    <div className="text-sm font-semibold text-foreground">{item.value}</div>
                  </div>
                ))}
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Plans */}
      <div>
        <h2 className="text-lg font-semibold mb-4">Choisir un plan</h2>
        {plansLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[1,2,3].map(i => <div key={i} className="h-64 bg-muted rounded-2xl animate-pulse" />)}
          </div>
        ) : plansError ? (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5 rounded-xl border border-destructive/10">
            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <p>Impossible de charger les plans : {plansError}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start pt-4">
            {plans?.filter(p => p.active).map(plan => (
              <PlanCard
                key={plan.id}
                plan={plan}
                currentPlanId={subscription?.planId}
                onSubscribe={handleSubscribe}
                loading={subscribingId === plan.id}
              />
            ))}
          </div>
        )}
      </div>

      {/* Factures */}
      <div className="border border-border bg-card rounded-2xl shadow-sm overflow-hidden">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
          <CreditCard className="w-5 h-5 text-primary" />
          <h3 className="font-semibold">Historique des factures ({invoices?.length ?? 0})</h3>
        </div>

        {invoicesLoading ? (
          <div className="p-8 space-y-3">{[1,2].map(i => <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />)}</div>
        ) : !invoices || invoices.length === 0 ? (
          <div className="p-8 text-center text-muted-foreground text-sm italic">Aucune facture.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">Période</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Montant</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Échéance</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Statut</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Payée le</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {invoices.map((inv, i) => (
                  <tr key={inv.id} className={`hover:bg-secondary/50 transition-colors ${i % 2 === 0 ? "bg-background" : "bg-muted/10"}`}>
                    <td className="px-6 py-4 text-xs text-muted-foreground">
                      {new Date(inv.periodStart).toLocaleDateString("fr-FR")} → {new Date(inv.periodEnd).toLocaleDateString("fr-FR")}
                    </td>
                    <td className="px-6 py-4 font-semibold font-mono text-foreground">{inv.amount.toLocaleString()} {inv.currency}</td>
                    <td className="px-6 py-4 text-xs text-muted-foreground">{new Date(inv.dueDate).toLocaleDateString("fr-FR")}</td>
                    <td className="px-6 py-4">{invoiceStatusBadge(inv.status)}</td>
                    <td className="px-6 py-4 text-xs text-muted-foreground">{inv.paidAt ? new Date(inv.paidAt).toLocaleDateString("fr-FR") : "—"}</td>
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
