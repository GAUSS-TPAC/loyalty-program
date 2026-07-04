"use client";

import { useEffect, useState } from "react";
import { Building2, Save, AlertTriangle, CheckCircle2 } from "lucide-react";
import { useTranslations } from "next-intl";
import { useTierPolicy } from "@/hooks/useBackend";
import { tierPolicyApi, type TierThreshold } from "@/lib/api";

export default function EstablishmentConfiguration() {
  const t = useTranslations("Establishment");
  const { data: policy, isLoading, error, refetch } = useTierPolicy();

  const [criterion, setCriterion] = useState("LIFETIME_POINTS");
  const [thresholdsJson, setThresholdsJson] = useState("[]");
  const [maintainPeriod, setMaintainPeriod] = useState("ROLLING_YEAR");
  const [maintainThresholdPoints, setMaintainThresholdPoints] = useState(0);
  const [downgradeGraceDays, setDowngradeGraceDays] = useState(30);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (!policy) return;
    setCriterion(policy.criterion);
    setThresholdsJson(JSON.stringify(policy.thresholds, null, 2));
    setMaintainPeriod(policy.maintainPeriod);
    setMaintainThresholdPoints(policy.maintainThresholdPoints);
    setDowngradeGraceDays(policy.downgradeGraceDays);
  }, [policy]);

  const handleSave = async () => {
    setSaving(true);
    setSaveError(null);
    setSaved(false);
    try {
      const thresholds: TierThreshold[] = JSON.parse(thresholdsJson);
      await tierPolicyApi.upsert({
        criterion,
        thresholds,
        maintainPeriod,
        maintainThresholdPoints,
        downgradeGraceDays,
      });
      setSaved(true);
      refetch();
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : "Invalid thresholds JSON");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="space-y-1">
        <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground text-sm">{t("description")}</p>
      </div>

      <div className="border border-border bg-card rounded-xl p-8 shadow-sm space-y-8 mt-6">
        <div className="flex items-center gap-3 pb-4 border-b border-border">
          <div className="w-10 h-10 rounded-full bg-secondary flex items-center justify-center">
            <Building2 className="w-5 h-5 text-primary" />
          </div>
          <div>
            <h3 className="font-medium text-foreground">{t("tenantPreferences")}</h3>
            <p className="text-xs text-muted-foreground">{t("tenantPreferencesDesc")}</p>
          </div>
        </div>

        {isLoading ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-10 bg-muted animate-pulse rounded-lg" />
            ))}
          </div>
        ) : error ? (
          <div className="text-sm text-destructive flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : (
          <div className="space-y-6">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                {t("tierStrategy")}
              </label>
              <select
                value={criterion}
                onChange={(e) => setCriterion(e.target.value)}
                className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
              >
                <option value="LIFETIME_POINTS">{t("lifetimePoints")}</option>
                <option value="YEARLY_POINTS">{t("yearlyPoints")}</option>
                <option value="MANUAL">{t("manualUpgrade")}</option>
              </select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Maintain Period
                </label>
                <input
                  type="text"
                  value={maintainPeriod}
                  onChange={(e) => setMaintainPeriod(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  Downgrade Grace Days
                </label>
                <input
                  type="number"
                  value={downgradeGraceDays}
                  onChange={(e) => setDowngradeGraceDays(Number(e.target.value))}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                />
              </div>
            </div>

            <div className="space-y-1.5 pt-4">
              <label className="text-xs font-semibold uppercase text-primary tracking-wider ml-1">
                {t("tierThresholds")}
              </label>
              <textarea
                value={thresholdsJson}
                onChange={(e) => setThresholdsJson(e.target.value)}
                rows={8}
                className="w-full bg-muted border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-primary resize-none shadow-inner"
              />
              <p className="text-[10px] text-muted-foreground ml-1">
                {'[{"level":"BRONZE","threshold":0,"multiplier":1}, ...]'}
              </p>
            </div>
          </div>
        )}

        <div className="pt-6 mt-6 border-t border-border flex items-center justify-end gap-4">
          {saveError && (
            <span className="text-xs text-destructive flex items-center gap-1.5">
              <AlertTriangle className="w-3.5 h-3.5" />
              {saveError}
            </span>
          )}
          {saved && !saveError && (
            <span className="text-xs text-green-600 flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5" />
              Saved
            </span>
          )}
          <button
            onClick={handleSave}
            disabled={saving || isLoading}
            className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {t("saveChanges")}
          </button>
        </div>
      </div>
    </div>
  );
}
