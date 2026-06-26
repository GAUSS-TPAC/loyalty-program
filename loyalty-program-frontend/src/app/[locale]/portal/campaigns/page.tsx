"use client";

import { useState } from "react";
import { Megaphone, Plus, RefreshCw, Info, AlertTriangle, Play, Pause, XCircle } from "lucide-react";
import { campaignApi, type CampaignResponse, type CreateCampaignRequest } from "@/lib/api";
import { useCampaigns } from "@/hooks/useBackend";

function statusBadge(status: CampaignResponse["status"]) {
  const map = {
    DRAFT:      { label: "Brouillon",  cls: "bg-orange-100 text-orange-700 border-orange-200" },
    ACTIVE:     { label: "Actif",      cls: "bg-green-100 text-green-700 border-green-200" },
    PAUSED:     { label: "En pause",   cls: "bg-yellow-100 text-yellow-700 border-yellow-200" },
    COMPLETED:  { label: "Terminé",    cls: "bg-blue-100 text-blue-700 border-blue-200" },
    CANCELLED:  { label: "Annulé",     cls: "bg-muted text-muted-foreground border-border" },
  };
  const s = map[status] ?? map.CANCELLED;
  return <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium border ${s.cls}`}>{s.label}</span>;
}

function typeLabel(type: CampaignResponse["campaignType"]) {
  return type === "BONUS_MULTIPLIER" ? "Multiplicateur" : "Bonus fixe";
}

export default function CampaignsPage() {
  const { data: campaigns, isLoading, error, refetch } = useCampaigns();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [campaignType, setCampaignType] = useState("BONUS_MULTIPLIER");
  const [targetEventType, setTargetEventType] = useState("");
  const [bonusMultiplier, setBonusMultiplier] = useState("2");
  const [bonusPoints, setBonusPoints] = useState("50");
  const [startDate, setStartDate] = useState(new Date().toISOString().split("T")[0]);
  const [endDate, setEndDate] = useState("");

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [actionId, setActionId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  const showToast = (message: string, type: "success" | "error") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setIsSubmitting(true);
    try {
      const body: CreateCampaignRequest = {
        name,
        description: description || undefined,
        campaignType,
        targetEventType: targetEventType || undefined,
        startDate: new Date(startDate).toISOString(),
        endDate: endDate ? new Date(endDate).toISOString() : undefined,
        ...(campaignType === "BONUS_MULTIPLIER"
          ? { bonusMultiplier: Number(bonusMultiplier) }
          : { bonusPoints: Number(bonusPoints) }),
      };
      await campaignApi.create(body);
      showToast("Campagne créée !", "success");
      setName(""); setDescription(""); setTargetEventType(""); setEndDate("");
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAction = async (c: CampaignResponse, action: "activate" | "pause" | "cancel") => {
    setActionId(c.id + action);
    try {
      if (action === "activate") await campaignApi.activate(c.id);
      else if (action === "pause") await campaignApi.pause(c.id);
      else await campaignApi.cancel(c.id);
      showToast("Campagne mise à jour", "success");
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Erreur", "error");
    } finally {
      setActionId(null);
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
          <h1 className="text-3xl font-semibold tracking-tight">Campagnes</h1>
          <p className="text-muted-foreground text-sm">Campagnes temporisées avec multiplicateurs ou bonus de points.</p>
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
              <div className="md:col-span-2 space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Nom *</label>
                <input required value={name} onChange={e => setName(e.target.value)} placeholder="Ex: Double points novembre" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="md:col-span-2 space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Description</label>
                <input value={description} onChange={e => setDescription(e.target.value)} placeholder="Description optionnelle..." className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Type *</label>
                <select value={campaignType} onChange={e => setCampaignType(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                  <option value="BONUS_MULTIPLIER">Multiplicateur de points</option>
                  <option value="FLAT_BONUS">Bonus fixe de points</option>
                </select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">
                  {campaignType === "BONUS_MULTIPLIER" ? "Multiplicateur (x)" : "Points bonus"}
                </label>
                {campaignType === "BONUS_MULTIPLIER" ? (
                  <input type="number" min="1.1" step="0.1" value={bonusMultiplier} onChange={e => setBonusMultiplier(e.target.value)} placeholder="Ex: 2 (= x2)" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
                ) : (
                  <input type="number" min="1" value={bonusPoints} onChange={e => setBonusPoints(e.target.value)} placeholder="Ex: 50" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" />
                )}
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">Événement cible</label>
                <select value={targetEventType} onChange={e => setTargetEventType(e.target.value)} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary">
                  <option value="">Tous les événements</option>
                  <option value="purchase.completed">purchase.completed</option>
                  <option value="account.created">account.created</option>
                  <option value="review.posted">review.posted</option>
                  <option value="referral.converted">referral.converted</option>
                  <option value="topup.completed">topup.completed</option>
                </select>
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
            <Megaphone className="w-5 h-5 text-primary" />
            <h3 className="font-semibold">Résumé</h3>
          </div>
          <div className="p-6 space-y-3">
            {[
              { label: "Total", value: campaigns?.length ?? "—", color: "text-foreground" },
              { label: "Actives", value: campaigns?.filter(c => c.status === "ACTIVE").length ?? "—", color: "text-emerald-600" },
              { label: "En pause", value: campaigns?.filter(c => c.status === "PAUSED").length ?? "—", color: "text-yellow-600" },
              { label: "Brouillon", value: campaigns?.filter(c => c.status === "DRAFT").length ?? "—", color: "text-orange-600" },
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
          <Megaphone className="w-5 h-5 text-primary" />
          <h3 className="font-semibold">Campagnes ({campaigns?.length ?? 0})</h3>
        </div>

        {isLoading && <div className="p-8 space-y-3">{[1,2,3].map(i => <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />)}</div>}

        {error && (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5">
            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <div><p className="font-semibold">Impossible de charger les campagnes</p><p className="text-xs mt-0.5">{error}</p></div>
          </div>
        )}

        {!isLoading && !error && campaigns?.length === 0 && (
          <div className="p-8 text-center text-muted-foreground text-sm italic">Aucune campagne configurée.</div>
        )}

        {!isLoading && !error && campaigns && campaigns.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">Campagne</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Type</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Bonus</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Période</th>
                  <th className="px-6 py-4 font-semibold tracking-wider">Statut</th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {campaigns.map((c, i) => (
                  <tr key={c.id} className={`hover:bg-secondary/50 transition-colors ${i % 2 === 0 ? "bg-background" : "bg-muted/10"}`}>
                    <td className="px-6 py-4">
                      <p className="font-medium text-foreground">{c.name}</p>
                      {c.description && <p className="text-xs text-muted-foreground mt-0.5">{c.description}</p>}
                    </td>
                    <td className="px-6 py-4 text-xs text-muted-foreground">{typeLabel(c.campaignType)}</td>
                    <td className="px-6 py-4 font-semibold text-primary font-mono">
                      {c.campaignType === "BONUS_MULTIPLIER" ? `x${c.bonusMultiplier}` : `+${c.bonusPoints} pts`}
                    </td>
                    <td className="px-6 py-4 text-xs text-muted-foreground">
                      {new Date(c.startDate).toLocaleDateString("fr-FR")}
                      {c.endDate ? ` → ${new Date(c.endDate).toLocaleDateString("fr-FR")}` : " (sans fin)"}
                    </td>
                    <td className="px-6 py-4">{statusBadge(c.status)}</td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end gap-1.5">
                        {c.status === "DRAFT" && (
                          <button onClick={() => handleAction(c, "activate")} disabled={actionId === c.id + "activate"} className="inline-flex items-center gap-1 text-xs font-medium text-green-700 hover:bg-green-50 border border-green-200 px-2 py-1 rounded-md transition-colors disabled:opacity-50">
                            {actionId === c.id + "activate" ? <RefreshCw className="w-3 h-3 animate-spin" /> : <Play className="w-3 h-3" />} Activer
                          </button>
                        )}
                        {c.status === "ACTIVE" && (
                          <button onClick={() => handleAction(c, "pause")} disabled={actionId === c.id + "pause"} className="inline-flex items-center gap-1 text-xs font-medium text-yellow-700 hover:bg-yellow-50 border border-yellow-200 px-2 py-1 rounded-md transition-colors disabled:opacity-50">
                            {actionId === c.id + "pause" ? <RefreshCw className="w-3 h-3 animate-spin" /> : <Pause className="w-3 h-3" />} Pause
                          </button>
                        )}
                        {c.status === "PAUSED" && (
                          <button onClick={() => handleAction(c, "activate")} disabled={actionId === c.id + "activate"} className="inline-flex items-center gap-1 text-xs font-medium text-green-700 hover:bg-green-50 border border-green-200 px-2 py-1 rounded-md transition-colors disabled:opacity-50">
                            {actionId === c.id + "activate" ? <RefreshCw className="w-3 h-3 animate-spin" /> : <Play className="w-3 h-3" />} Reprendre
                          </button>
                        )}
                        {!["COMPLETED", "CANCELLED"].includes(c.status) && (
                          <button onClick={() => handleAction(c, "cancel")} disabled={actionId === c.id + "cancel"} className="inline-flex items-center gap-1 text-xs font-medium text-rose-700 hover:bg-rose-50 border border-rose-200 px-2 py-1 rounded-md transition-colors disabled:opacity-50">
                            {actionId === c.id + "cancel" ? <RefreshCw className="w-3 h-3 animate-spin" /> : <XCircle className="w-3 h-3" />} Annuler
                          </button>
                        )}
                      </div>
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
