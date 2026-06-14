"use client";

import { useState } from "react";
import {
  Coins,
  Plus,
  CheckCircle,
  AlertTriangle,
  Info,
  RefreshCw,
  Zap,
  Code2,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { rulesApi, type RuleResponse } from "@/lib/api";
import { useRules } from "@/hooks/useBackend";

// ─── Helpers ──────────────────────────────────────────────────────────────────

function statusBadge(status: RuleResponse["status"]) {
  if (status === "ACTIVE")
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-green-100 text-green-700 border border-green-200">
        <CheckCircle className="w-3 h-3" /> Actif
      </span>
    );
  if (status === "INACTIVE")
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-muted text-muted-foreground border border-border">
        Inactif
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-orange-100 text-orange-700 border border-orange-200">
      Brouillon
    </span>
  );
}

// ─── Composant principal ───────────────────────────────────────────────────────

export default function RulesConfiguration() {
  const t = useTranslations("Rules");
  const { data: rules, isLoading, error, refetch } = useRules();

  // ── Form state ──────────────────────────────────────────────────────────────
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [eventType, setEventType] = useState("purchase.completed");
  const [effectValue, setEffectValue] = useState("");
  const [priority, setPriority] = useState("1");
  const [validFrom, setValidFrom] = useState(
    new Date().toISOString().split("T")[0]
  );

  // ── UI state ─────────────────────────────────────────────────────────────────
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [activatingId, setActivatingId] = useState<string | null>(null);
  const [toast, setToast] = useState<{
    message: string;
    type: "success" | "error";
  } | null>(null);

  const showToast = (message: string, type: "success" | "error") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3500);
  };

  // ── Créer une règle ──────────────────────────────────────────────────────────
  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !effectValue) return;
    setIsSubmitting(true);
    try {
      await rulesApi.createRule({
        name,
        description,
        trigger: { eventType },
        conditions: [],
        effects: [{ type: "CREDIT_POINTS", value: Number(effectValue) }],
        priority: Number(priority),
        validFrom: new Date(validFrom).toISOString(),
        validUntil: null,
      });
      showToast("Règle créée avec succès !", "success");
      setName("");
      setDescription("");
      setEffectValue("");
      setPriority("1");
      refetch();
    } catch (err) {
      showToast(
        err instanceof Error ? err.message : "Erreur lors de la création",
        "error"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  // ── Activer une règle ────────────────────────────────────────────────────────
  const handleActivate = async (ruleId: string) => {
    setActivatingId(ruleId);
    try {
      await rulesApi.activateRule(ruleId);
      showToast("Règle activée !", "success");
      refetch();
    } catch (err) {
      showToast(
        err instanceof Error ? err.message : "Erreur d'activation",
        "error"
      );
    } finally {
      setActivatingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Toast */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg border text-sm transition-all ${toast.type === "success"
              ? "bg-emerald-50 border-emerald-200 text-emerald-800"
              : "bg-rose-50 border-rose-200 text-rose-800"
            }`}
        >
          <Info className="w-4 h-4" />
          <span>{toast.message}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">
            {t("title")}
          </h1>
          <p className="text-muted-foreground text-sm">{t("description")}</p>
        </div>
        <button
          onClick={refetch}
          className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground border border-border px-3 py-2 rounded-lg hover:bg-secondary transition-all"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Rafraîchir
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ── Formulaire création ─────────────────────────────────────────── */}
        <div className="lg:col-span-2 border border-border bg-card rounded-xl shadow-sm overflow-hidden">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
            <Plus className="w-5 h-5 text-primary" />
            <h3 className="font-semibold text-foreground">
              {t("createRuleTitle")}
            </h3>
          </div>

          <form onSubmit={handleCreate} className="p-6 space-y-4">
            {/* Nom */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                Nom de la règle *
              </label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Ex: Bonus achat premium"
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
            </div>

            {/* Description */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                Description
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Description optionnelle..."
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Déclencheur */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Événement déclencheur *
                </label>
                <select
                  value={eventType}
                  onChange={(e) => setEventType(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                >
                  <option value="purchase.completed">purchase.completed</option>
                  <option value="account.created">account.created</option>
                  <option value="review.posted">review.posted</option>
                  <option value="referral.converted">referral.converted</option>
                  <option value="topup.completed">topup.completed</option>
                </select>
              </div>

              {/* Points attribués */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Points attribués *
                </label>
                <input
                  type="number"
                  min="1"
                  required
                  value={effectValue}
                  onChange={(e) => setEffectValue(e.target.value)}
                  placeholder="Ex: 100"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>

              {/* Priorité */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Priorité
                </label>
                <input
                  type="number"
                  min="1"
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>

              {/* Date de début */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Valide à partir du
                </label>
                <input
                  type="date"
                  value={validFrom}
                  onChange={(e) => setValidFrom(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
              >
                {isSubmitting ? (
                  <RefreshCw className="w-4 h-4 animate-spin" />
                ) : (
                  <Plus className="w-4 h-4" />
                )}
                {t("createRuleButton")}
              </button>
            </div>
          </form>
        </div>

        {/* ── Panneau info ────────────────────────────────────────────────── */}
        <div className="space-y-6">
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
              <Coins className="w-5 h-5 text-primary" />
              <h3 className="font-semibold text-foreground">Moteur de règles</h3>
            </div>
            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between p-3 rounded-lg bg-muted/30 border border-border">
                <span className="text-sm font-medium text-foreground">
                  Règles chargées
                </span>
                <span className="text-xs font-bold text-primary bg-secondary border border-border px-2.5 py-0.5 rounded font-mono">
                  {rules?.length ?? "—"}
                </span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg bg-muted/30 border border-border">
                <span className="text-sm font-medium text-foreground">
                  Règles actives
                </span>
                <span className="text-xs font-bold text-green-700 bg-green-50 border border-green-200 px-2.5 py-0.5 rounded font-mono">
                  {rules?.filter((r) => r.status === "ACTIVE").length ?? "—"}
                </span>
              </div>
            </div>
            <div className="p-4 bg-muted/20 border-t border-border text-xs text-muted-foreground flex gap-2">
              <Info className="w-4 h-4 flex-shrink-0 text-primary" />
              <p>
                Les règles sont évaluées dans l&apos;ordre de priorité à chaque
                événement reçu.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* ── Table des règles ─────────────────────────────────────────────────── */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
          <Code2 className="w-5 h-5 text-primary" />
          <h3 className="font-semibold text-foreground">
            Règles configurées ({rules?.length ?? 0})
          </h3>
        </div>

        {/* Loading */}
        {isLoading && (
          <div className="p-8 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-10 bg-muted rounded-lg animate-pulse" />
            ))}
          </div>
        )}

        {/* Erreur */}
        {error && (
          <div className="p-6 flex items-start gap-3 text-sm text-destructive bg-destructive/5 border-b border-destructive/10">
            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <div>
              <p className="font-semibold">Impossible de charger les règles</p>
              <p className="text-xs mt-0.5 text-destructive/80">{error}</p>
              <p className="text-xs mt-1 text-muted-foreground">
                Vérifiez que le backend est démarré sur le port 8081.
              </p>
            </div>
          </div>
        )}

        {/* Vide */}
        {!isLoading && !error && rules?.length === 0 && (
          <div className="p-8 text-center text-muted-foreground text-sm italic">
            {t("noRules")}
          </div>
        )}

        {/* Données */}
        {!isLoading && !error && rules && rules.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Nom
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Déclencheur
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Effet
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Priorité
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider">
                    Statut
                  </th>
                  <th className="px-6 py-4 font-semibold tracking-wider text-right">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {rules.map((rule, index) => (
                  <tr
                    key={rule.id}
                    className={`hover:bg-secondary/50 transition-colors ${index % 2 === 0 ? "bg-background" : "bg-muted/10"
                      }`}
                  >
                    <td className="px-6 py-4">
                      <p className="font-medium text-foreground">{rule.name}</p>
                      {rule.description && (
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {rule.description}
                        </p>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-mono text-primary bg-primary/10 px-2 py-1 rounded-md">
                        {rule.trigger?.eventType ?? "—"}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-semibold text-primary font-mono">
                      +
                      {rule.effects?.[0]?.value ?? "—"}{" "}
                      <span className="text-xs font-normal text-muted-foreground">
                        pts
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono text-muted-foreground">
                      #{rule.priority}
                    </td>
                    <td className="px-6 py-4">{statusBadge(rule.status)}</td>
                    <td className="px-6 py-4 text-right">
                      {rule.status !== "ACTIVE" && (
                        <button
                          onClick={() => handleActivate(rule.id)}
                          disabled={activatingId === rule.id}
                          className="inline-flex items-center gap-1.5 text-xs font-medium text-green-700 hover:bg-green-50 border border-green-200 px-2.5 py-1.5 rounded-md transition-colors disabled:opacity-50"
                        >
                          {activatingId === rule.id ? (
                            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                          ) : (
                            <Zap className="w-3.5 h-3.5" />
                          )}
                          Activer
                        </button>
                      )}
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
