"use client";

import { useState } from "react";
import { Tag, Plus, RefreshCw, CheckCircle, XCircle, Info, AlertTriangle } from "lucide-react";
import { promoApi, type PromoCampaignResponse, type CreatePromoRequest } from "@/lib/api";
import { usePromos } from "@/hooks/useBackend";

function statusBadge(active: boolean) {
  return active ? (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-green-100 text-green-700 border border-green-200">
      <CheckCircle className="w-3 h-3" /> Actif
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-muted text-muted-foreground border border-border">
      <XCircle className="w-3 h-3" /> Inactif
    </span>
  );
}

function discountLabel(campaign: PromoCampaignResponse) {
  if (campaign.discountType === "PERCENTAGE") return `${campaign.discountValue}%`;
  if (campaign.discountType === "FIXED_AMOUNT") return `${campaign.discountValue} XAF`;
  return "Gratuit";
}

export default function PromoPage() {
  const { data: promos, isLoading, error, refetch } = usePromos();

  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [discountType, setDiscountType] = useState("PERCENTAGE");
  const [discountValue, setDiscountValue] = useState("");
  const [minOrderAmount, setMinOrderAmount] = useState("");
  const [maxUses, setMaxUses] = useState("100");
  const [perMemberLimit, setPerMemberLimit] = useState("1");
  const [startDate, setStartDate] = useState(new Date().toISOString().split("T")[0]);
  const [endDate, setEndDate] = useState("");

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [togglingId, setTogglingId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !code.trim() || !discountValue) return;
    setIsSubmitting(true);
    try {
      const body: CreatePromoRequest = {
        code: code.toUpperCase(),
        name,
        discountType,
        discountValue: Number(discountValue),
        maxUses: Number(maxUses),
        perMemberLimit: Number(perMemberLimit),
        startDate: new Date(startDate).toISOString(),
        ...(minOrderAmount ? { minOrderAmount: Number(minOrderAmount) } : {}),
        ...(endDate ? { endDate: new Date(endDate).toISOString() } : {}),
      };
      await promoApi.create(body);
      showToast("Campagne promo créée !", "success");
      setName(""); setCode(""); setDiscountValue(""); setMinOrderAmount(""); setEndDate("");
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleToggle = async (campaign: PromoCampaignResponse) => {
    setTogglingId(campaign.id);
    try {
      if (campaign.active) {
        await promoApi.deactivate(campaign.id);
        showToast("Campagne désactivée", "success");
      } else {
        await promoApi.activate(campaign.id);
        showToast("Campagne activée !", "success");
      }
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {toast && (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg border text-sm transition-all ${toast.type === "success" ? "bg-emerald-50 border-emerald-200 text-emerald-800" : "bg-rose-50 border-rose-200 text-rose-800"}`}>
          <Info className="w-4 h-4" />{toast.message}
        </div>
      )}

      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">Codes Promo</h1>
          <p className="text-muted-foreground text-sm">Créez et gérez vos campagnes promotionnelles.</p>
        </div>
        <button onClick={refetch} className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all">
          <RefreshCw className="w-3.5 h-3.5" /> Rafraîchir
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Formulaire */}
        <div className="lg:col-span-2 border border-border bg-card rounded-xl shadow-sm overflow-hidden">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
            <Plus className="w-5 h-5 text-primary" />
            <h3 className="font-semibold">Nouvelle campagne</h3>
          </div>
          <form onSubmit={handleCreate} className="p-6 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Nom *</label>
                <input required value={name} onChange={e => setName(e.target.value)} placeholder="Ex: Soldes d'été" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Code *</label>
                <input required value={code} onChange={e => setCode(e.target.value.toUpperCase())} placeholder="Ex: SUMMER25" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Type de remise *</label>
                <select value={discountType} onChange={e => setDiscountType(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                  <option value="PERCENTAGE">Pourcentage (%)</option>
                  <option value="FIXED_AMOUNT">Montant fixe (XAF)</option>
                  <option value="FREE_ITEM">Article gratuit</option>
                </select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Valeur *</label>
                <input required type="number" min="0" value={discountValue} onChange={e => setDiscountValue(e.target.value)} placeholder={discountType === "PERCENTAGE" ? "Ex: 20" : "Ex: 5000"} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Montant min. commande</label>
                <input type="number" min="0" value={minOrderAmount} onChange={e => setMinOrderAmount(e.target.value)} placeholder="Ex: 10000" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Utilisations max.</label>
                <input type="number" min="1" value={maxUses} onChange={e => setMaxUses(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Limite par membre</label>
                <input type="number" min="0" value={perMemberLimit} onChange={e => setPerMemberLimit(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Date de début *</label>
                <input type="date" required value={startDate} onChange={e => setStartDate(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Date de fin</label>
                <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
            </div>
            <div className="pt-2 flex justify-end">
              <button type="submit" disabled={isSubmitting} className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50">
                {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Plus className="w-4 h-4" />}
                Créer la campagne
              </button>
            </div>
          </form>
        </div>

        {/* Stats */}
        <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden h-fit">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
            <Tag className="w-5 h-5 text-primary" />
            <h3 className="font-semibold">Résumé</h3>
          </div>
          <div className="p-6 space-y-3">
            {[
              { label: "Total campagnes", value: promos?.length ?? "—", color: "text-foreground" },
              { label: "Campagnes actives", value: promos?.filter(p => p.active).length ?? "—", color: "text-emerald-600" },
              { label: "Campagnes inactives", value: promos?.filter(p => !p.active).length ?? "—", color: "text-muted-foreground" },
            ].map(s => (
              <div key={s.label} className="flex items-center justify-between p-3 rounded-lg bg-muted/30 border border-border">
                <span className="text-sm font-medium text-foreground">{s.label}</span>
                <span className={`text-xs font-bold font-mono px-2.5 py-0.5 rounded bg-secondary border border-border ${s.color}`}>{s.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
          <Tag className="w-5 h-5 text-primary" />
          <h3 className="font-semibold">Campagnes configurées ({promos?.length ?? 0})</h3>
        </div>

        {isLoading && (
          <div className="p-8 space-y-3">{[1, 2, 3].map(i => <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />)}</div>
        )}

        {error && (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5">
            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <div><p className="font-semibold">Impossible de charger les campagnes</p><p className="text-xs mt-0.5 text-destructive/80">{error}</p></div>
          </div>
        )}

        {!isLoading && !error && promos?.length === 0 && (
          <div className="p-8 text-center text-muted-foreground text-sm italic">Aucune campagne promo configurée.</div>
        )}

        {!isLoading && !error && promos && promos.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">Campagne</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Code</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Remise</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Utilisations</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Statut</th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {promos.map((p, i) => (
                  <tr key={p.id} className={`hover:bg-secondary/50 transition-colors ${i % 2 === 0 ? "bg-background" : "bg-muted/10"}`}>
                    <td className="px-6 py-4">
                      <p className="font-medium text-foreground">{p.name}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {new Date(p.startDate).toLocaleDateString("fr-FR")}
                        {p.endDate ? ` → ${new Date(p.endDate).toLocaleDateString("fr-FR")}` : " (sans fin)"}
                      </p>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-mono text-primary bg-primary/10 px-2 py-1 rounded-md">{p.code}</span>
                    </td>
                    <td className="px-6 py-4 font-semibold text-primary font-mono">{discountLabel(p)}</td>
                    <td className="px-6 py-4 text-muted-foreground font-mono text-xs">
                      <span className="text-foreground font-bold">—</span> / {p.maxUses}
                    </td>
                    <td className="px-6 py-4">{statusBadge(p.active)}</td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleToggle(p)}
                        disabled={togglingId === p.id}
                        className={`inline-flex items-center gap-1.5 text-xs font-medium border px-2.5 py-1.5 rounded-md transition-colors disabled:opacity-50 ${p.active ? "text-rose-700 hover:bg-rose-50 border-rose-200" : "text-green-700 hover:bg-green-50 border-green-200"}`}
                      >
                        {togglingId === p.id ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : p.active ? <XCircle className="w-3.5 h-3.5" /> : <CheckCircle className="w-3.5 h-3.5" />}
                        {p.active ? "Désactiver" : "Activer"}
                      </button>
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
