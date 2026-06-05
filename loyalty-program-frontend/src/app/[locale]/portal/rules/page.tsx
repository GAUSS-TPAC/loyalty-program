"use client";

import { useEffect, useState } from "react";
import { Coins, Plus, Save, Trash2, Info, AlertTriangle, Share2 } from "lucide-react";
import { useTranslations } from "next-intl";

interface BonusRule {
  id: string;
  description: string;
  amountMin: number;
  amountMax: number;
  points: number;
  minDaysForIrregularClients: number;
  alwaysCredit: boolean;
}

const DEFAULT_RULES: BonusRule[] = [
  {
    id: "1",
    description: "Welcome Bonus",
    amountMin: 1000,
    amountMax: 5000,
    points: 50,
    minDaysForIrregularClients: 3,
    alwaysCredit: true,
  },
  {
    id: "2",
    description: "Premium Rule",
    amountMin: 10000,
    amountMax: 50000,
    points: 200,
    minDaysForIrregularClients: 5,
    alwaysCredit: false,
  }
];

export default function RulesConfiguration() {
  const t = useTranslations("Rules");

  // Client-side states to prevent Next.js hydration mismatch
  const [rules, setRules] = useState<BonusRule[]>([]);
  const [pointValue, setPointValue] = useState<number>(100);
  const [isLoaded, setIsLoaded] = useState(false);

  // Form states - Create Rule
  const [newDesc, setNewDesc] = useState("");
  const [newMinAmount, setNewMinAmount] = useState("");
  const [newMaxAmount, setNewMaxAmount] = useState("");
  const [newPoints, setNewPoints] = useState("");
  const [newInactivity, setNewInactivity] = useState("");
  const [newAlwaysCredit, setNewAlwaysCredit] = useState(false);

  // Form states - Point Value
  const [tempPointValue, setTempPointValue] = useState("");

  // Referral states
  const [referralActive, setReferralActive] = useState(false);
  const [referrerPoints, setReferrerPoints] = useState(100);
  const [refereePoints, setRefereePoints] = useState(50);

  // Modal / Feedback states
  const [ruleToDelete, setRuleToDelete] = useState<BonusRule | null>(null);
  const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);

  // Load from localStorage on mount
  useEffect(() => {
    const savedRules = localStorage.getItem("loyalty_rules");
    if (savedRules) {
      try {
        setRules(JSON.parse(savedRules));
      } catch (e) {
        setRules(DEFAULT_RULES);
      }
    } else {
      setRules(DEFAULT_RULES);
      localStorage.setItem("loyalty_rules", JSON.stringify(DEFAULT_RULES));
    }

    const savedPointValue = localStorage.getItem("loyalty_point_value");
    if (savedPointValue) {
      const parsed = parseFloat(savedPointValue);
      if (!isNaN(parsed)) {
        setPointValue(parsed);
      }
    } else {
      localStorage.setItem("loyalty_point_value", "100");
    }

    // Referral Settings loading
    const savedReferralActive = localStorage.getItem("loyalty_referral_active");
    setReferralActive(savedReferralActive === "true");

    const savedReferrerPoints = localStorage.getItem("loyalty_referral_referrer_points");
    setReferrerPoints(savedReferrerPoints ? parseInt(savedReferrerPoints) : 100);

    const savedRefereePoints = localStorage.getItem("loyalty_referral_referee_points");
    setRefereePoints(savedRefereePoints ? parseInt(savedRefereePoints) : 50);

    setIsLoaded(true);
  }, []);

  // Show auto-dismiss toast
  const showToast = (message: string, type: "success" | "error") => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 3000);
  };

  // Create rule handler
  const handleCreateRule = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDesc.trim()) return;

    const newRule: BonusRule = {
      id: Math.random().toString(36).substring(2, 9),
      description: newDesc,
      amountMin: Number(newMinAmount) || 0,
      amountMax: Number(newMaxAmount) || 0,
      points: Number(newPoints) || 0,
      minDaysForIrregularClients: Number(newInactivity) || 0,
      alwaysCredit: newAlwaysCredit,
    };

    const updatedRules = [...rules, newRule].sort((a, b) => a.amountMin - b.amountMin);
    setRules(updatedRules);
    localStorage.setItem("loyalty_rules", JSON.stringify(updatedRules));

    // Reset Form
    setNewDesc("");
    setNewMinAmount("");
    setNewMaxAmount("");
    setNewPoints("");
    setNewInactivity("");
    setNewAlwaysCredit(false);

    showToast(t("ruleCreatedSuccess"), "success");
  };

  // Delete rule handler
  const handleDeleteRule = () => {
    if (!ruleToDelete) return;
    const updatedRules = rules.filter((r) => r.id !== ruleToDelete.id);
    setRules(updatedRules);
    localStorage.setItem("loyalty_rules", JSON.stringify(updatedRules));
    setRuleToDelete(null);
    showToast("Rule deleted successfully!", "success");
  };

  // Save point value conversion handler
  const handleSavePointValue = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(tempPointValue.replace(",", "."));
    if (isNaN(val) || val <= 0) {
      showToast(t("ruleCreateError"), "error");
      return;
    }

    setPointValue(val);
    localStorage.setItem("loyalty_point_value", val.toString());
    setTempPointValue("");
    showToast("Point value updated!", "success");
  };

  // Save referral settings handler
  const handleSaveReferral = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem("loyalty_referral_active", referralActive.toString());
    localStorage.setItem("loyalty_referral_referrer_points", referrerPoints.toString());
    localStorage.setItem("loyalty_referral_referee_points", refereePoints.toString());
    showToast("Referral settings updated!", "success");
  };

  if (!isLoaded) {
    return <div className="space-y-6 animate-pulse" />;
  }

  return (
    <div className="space-y-6">
      {/* Toast Feedback */}
      {toast && (
        <div
          className={`fixed top-4 right-4 z-50 flex items-center gap-2.5 px-4 py-3 rounded-lg shadow-lg border text-sm transition-all ${
            toast.type === "success"
              ? "bg-emerald-50 border-emerald-200 text-emerald-800"
              : "bg-rose-50 border-rose-200 text-rose-800"
          }`}
        >
          <Info className="w-4 h-4" />
          <span>{toast.message}</span>
        </div>
      )}

      {/* Header */}
      <div className="space-y-1">
        <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground text-sm">{t("description")}</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Side: Create Rule Form */}
        <div className="lg:col-span-2 border border-border bg-card rounded-xl shadow-sm overflow-hidden">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
            <Plus className="w-5 h-5 text-primary" />
            <h3 className="font-semibold text-foreground">{t("createRuleTitle")}</h3>
          </div>

          <form onSubmit={handleCreateRule} className="p-6 space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                {t("descriptionLabel")} *
              </label>
              <input
                type="text"
                required
                value={newDesc}
                onChange={(e) => setNewDesc(e.target.value)}
                placeholder={t("descriptionPlaceholder")}
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  {t("minAmountLabel")}
                </label>
                <input
                  type="number"
                  value={newMinAmount}
                  onChange={(e) => setNewMinAmount(e.target.value)}
                  placeholder="0"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  {t("maxAmountLabel")}
                </label>
                <input
                  type="number"
                  value={newMaxAmount}
                  onChange={(e) => setNewMaxAmount(e.target.value)}
                  placeholder="0"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  {t("pointsLabel")}
                </label>
                <input
                  type="number"
                  value={newPoints}
                  onChange={(e) => setNewPoints(e.target.value)}
                  placeholder="1"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  {t("inactivityDaysLabel")}
                </label>
                <input
                  type="number"
                  value={newInactivity}
                  onChange={(e) => setNewInactivity(e.target.value)}
                  placeholder="0"
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                />
              </div>
            </div>

            <div className="flex items-center gap-2.5 pt-2">
              <input
                id="alwaysCredit"
                type="checkbox"
                checked={newAlwaysCredit}
                onChange={(e) => setNewAlwaysCredit(e.target.checked)}
                className="w-4 h-4 text-primary focus:ring-primary border-border rounded cursor-pointer"
              />
              <label htmlFor="alwaysCredit" className="text-sm text-foreground select-none cursor-pointer">
                {t("alwaysCreditLabel")}
              </label>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                type="submit"
                className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95"
              >
                <Plus className="w-4 h-4" />
                {t("createRuleButton")}
              </button>
            </div>
          </form>
        </div>

        {/* Right Side: Configuration Panels */}
        <div className="space-y-6">
          {/* Conversion Parameter */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden flex flex-col justify-between">
            <div>
              <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
                <Coins className="w-5 h-5 text-primary" />
                <h3 className="font-semibold text-foreground">
                  {t("pointValueTitle", { value: pointValue.toFixed(2) })}
                </h3>
              </div>

              <form onSubmit={handleSavePointValue} className="p-6 space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                    {t("modifyPointValue")}
                  </label>
                  <input
                    type="text"
                    placeholder="0.00"
                    value={tempPointValue}
                    onChange={(e) => setTempPointValue(e.target.value)}
                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                  />
                </div>

                <div className="flex justify-start">
                  <button
                    type="submit"
                    disabled={!tempPointValue.trim()}
                    className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm disabled:opacity-50 disabled:pointer-events-none active:scale-95"
                  >
                    <Save className="w-4 h-4" />
                    {t("saveSettings")}
                  </button>
                </div>
              </form>
            </div>

            <div className="p-6 bg-muted/20 border-t border-border/60 text-xs text-muted-foreground flex gap-2">
              <Info className="w-4 h-4 flex-shrink-0 text-primary" />
              <p>Determines point value in FCFA for virtual balance redemptions.</p>
            </div>
          </div>

          {/* Referral configuration */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden flex flex-col justify-between">
            <div>
              <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
                <Share2 className="w-5 h-5 text-primary" />
                <h3 className="font-semibold text-foreground">
                  {t("referralSettingsTitle")}
                </h3>
              </div>

              <form onSubmit={handleSaveReferral} className="p-6 space-y-4">
                <div className="flex items-center gap-2.5">
                  <input
                    id="referralActive"
                    type="checkbox"
                    checked={referralActive}
                    onChange={(e) => setReferralActive(e.target.checked)}
                    className="w-4 h-4 text-primary focus:ring-primary border-border rounded cursor-pointer"
                  />
                  <label htmlFor="referralActive" className="text-sm font-semibold text-foreground select-none cursor-pointer">
                    {t("referralActiveLabel")}
                  </label>
                </div>

                <div className="space-y-3 pt-2">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                      {t("referrerPointsLabel")}
                    </label>
                    <input
                      type="number"
                      value={referrerPoints}
                      onChange={(e) => setReferrerPoints(Number(e.target.value))}
                      className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                      {t("refereePointsLabel")}
                    </label>
                    <input
                      type="number"
                      value={refereePoints}
                      onChange={(e) => setRefereePoints(Number(e.target.value))}
                      className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                    />
                  </div>
                </div>

                <div className="flex justify-start">
                  <button
                    type="submit"
                    className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm active:scale-95"
                  >
                    <Save className="w-4 h-4" />
                    {t("saveReferralSettings")}
                  </button>
                </div>
              </form>
            </div>

            <div className="p-6 bg-muted/20 border-t border-border/60 text-xs text-muted-foreground flex gap-2">
              <Info className="w-4 h-4 flex-shrink-0 text-primary" />
              <p>Configure referral program incentives for both referrers and referees.</p>
            </div>
          </div>
        </div>
      </div>

      {/* Rules Table */}
      <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-border bg-secondary/30">
          <h3 className="font-semibold text-foreground">{t("title")}</h3>
        </div>

        <div className="overflow-x-auto">
          {rules.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground text-sm">{t("noRules")}</div>
          ) : (
            <table className="w-full text-sm text-left">
              <thead className="text-xs text-muted-foreground uppercase bg-muted/30 border-b border-border">
                <tr>
                  <th className="px-6 py-4 font-semibold">{t("tableDescription")}</th>
                  <th className="px-6 py-4 font-semibold">{t("tableMinAmount")} (FCFA)</th>
                  <th className="px-6 py-4 font-semibold">{t("tableMaxAmount")} (FCFA)</th>
                  <th className="px-6 py-4 font-semibold">{t("tablePoints")}</th>
                  <th className="px-6 py-4 font-semibold">{t("tableInactivity")}</th>
                  <th className="px-6 py-4 font-semibold">{t("tableAlwaysCredit")}</th>
                  <th className="px-6 py-4 font-semibold text-right">{t("tableActions")}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {rules.map((rule) => (
                  <tr key={rule.id} className="hover:bg-muted/10 transition-colors">
                    <td className="px-6 py-4 font-medium text-foreground">{rule.description}</td>
                    <td className="px-6 py-4 font-mono">{rule.amountMin.toLocaleString()}</td>
                    <td className="px-6 py-4 font-mono">{rule.amountMax.toLocaleString()}</td>
                    <td className="px-6 py-4 font-semibold text-primary">{rule.points}</td>
                    <td className="px-6 py-4 text-muted-foreground">{rule.minDaysForIrregularClients} d</td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                          rule.alwaysCredit ? "bg-emerald-50 text-emerald-700 border border-emerald-200" : "bg-muted text-muted-foreground border border-border"
                        }`}
                      >
                        {rule.alwaysCredit ? t("yes") : t("no")}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => setRuleToDelete(rule)}
                        className="inline-flex items-center gap-1.5 text-xs text-destructive hover:bg-destructive/10 px-2.5 py-1.5 rounded-md transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        {t("deleteButton")}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {ruleToDelete && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in-95 duration-150">
            <div className="p-6 space-y-4">
              <div className="flex items-center gap-3 text-destructive">
                <div className="w-10 h-10 rounded-full bg-destructive/10 flex items-center justify-center">
                  <AlertTriangle className="w-5 h-5 text-destructive" />
                </div>
                <h3 className="font-semibold text-lg">{t("confirmDeleteTitle")}</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                {t("confirmDeleteText", { name: ruleToDelete.description })}
              </p>
            </div>
            <div className="bg-secondary/40 px-6 py-4 border-t border-border flex justify-end gap-3">
              <button
                onClick={() => setRuleToDelete(null)}
                className="px-4 py-2 rounded-lg text-sm font-medium text-foreground bg-background border border-border hover:bg-secondary transition-all"
              >
                {t("cancel")}
              </button>
              <button
                onClick={handleDeleteRule}
                className="px-4 py-2 rounded-lg text-sm font-medium text-destructive-foreground bg-destructive hover:bg-destructive/90 transition-all shadow"
              >
                {t("deleteButton")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
