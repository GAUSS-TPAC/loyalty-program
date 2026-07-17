"use client";

import { useState } from "react";
import { Coins, Pencil, X } from "lucide-react";
import { toast } from "sonner";
import { useWalletPolicy } from "@/hooks/useBackend";
import { walletApi } from "@/lib/api";

// Affiche et permet de modifier le taux de conversion points <-> monnaie du tenant
// (WalletPolicy.exchangeRate cote backend).

export function PointsConversionCard() {
  const { data: policy, isLoading, refetch } = useWalletPolicy();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  // La correspondance s'exprime sur le montant : 1 point vaut `amount` unites de `currency`.
  const [form, setForm] = useState({ amount: "", currency: "" });

  const startEditing = () => {
    if (!policy) return;
    setForm({
      amount: String(policy.exchangeRate),
      currency: policy.currencySymbol,
    });
    setIsEditing(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const amount = Number(form.amount);
    const currency = form.currency.trim().toUpperCase();
    if (!currency || !Number.isFinite(amount) || amount <= 0) {
      toast.error("Renseignez un montant strictement positif et une devise (ex. XAF).");
      return;
    }
    setIsSaving(true);
    try {
      await walletApi.updatePointsConversion({
        currencyName: currency,
        currencySymbol: currency,
        exchangeRate: amount,
      });
      toast.success("Correspondance des points mise à jour");
      setIsEditing(false);
      refetch();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Échec de la mise à jour");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="bg-card border border-border rounded-2xl p-6 shadow-sm space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-secondary rounded-xl">
            <Coins className="w-5 h-5 text-primary" />
          </div>
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
              Correspondance des points
            </p>
            <p className="text-[10px] text-muted-foreground">
              montant que vaut 1 point de fidélité (ex. 1 pt = 0.001 XAF)
            </p>
          </div>
        </div>
        {!isEditing && (
          <button
            onClick={startEditing}
            disabled={isLoading || !policy}
            className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all disabled:opacity-50"
          >
            <Pencil className="w-3.5 h-3.5" /> Modifier
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="h-8 w-48 bg-muted animate-pulse rounded" />
      ) : !isEditing ? (
        <p className="text-2xl font-bold tracking-tight text-foreground font-mono">
          1 pt ={" "}
          {policy ? (
            <>
              {policy.exchangeRate.toLocaleString("fr-FR", { maximumFractionDigits: 6 })}{" "}
              <span className="text-sm font-semibold text-muted-foreground uppercase">
                {policy.currencySymbol}
              </span>
              {policy.currencyName !== policy.currencySymbol && (
                <>
                  {" "}
                  <span className="text-sm font-normal text-muted-foreground">
                    ({policy.currencyName})
                  </span>
                </>
              )}
            </>
          ) : (
            "—"
          )}
        </p>
      ) : (
        <form onSubmit={handleSave} className="grid grid-cols-1 sm:grid-cols-[1fr_1fr_auto] gap-3 items-end">
          <div className="space-y-1">
            <label className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
              1 point = (montant)
            </label>
            <input
              type="number"
              step="any"
              min="0"
              value={form.amount}
              onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))}
              placeholder="0.001"
              className="w-full px-3 py-2 text-sm font-mono rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1">
            <label className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
              Devise
            </label>
            <input
              type="text"
              maxLength={10}
              value={form.currency}
              onChange={(e) => setForm((f) => ({ ...f, currency: e.target.value }))}
              placeholder="XAF"
              className="w-full px-3 py-2 text-sm font-mono uppercase rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="flex items-center gap-2">
            <button
              type="submit"
              disabled={isSaving}
              className="px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-all shadow-sm disabled:opacity-50"
            >
              {isSaving ? "…" : "Enregistrer"}
            </button>
            <button
              type="button"
              onClick={() => setIsEditing(false)}
              disabled={isSaving}
              className="p-2 rounded-md border border-border text-muted-foreground hover:bg-secondary transition-all"
              title="Annuler"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
