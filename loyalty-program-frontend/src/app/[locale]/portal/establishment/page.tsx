"use client";

import { Building2, Save } from "lucide-react";
import { useTranslations } from "next-intl";

export default function EstablishmentConfiguration() {
  const t = useTranslations("Establishment");

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="space-y-1">
        <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground text-sm">
          {t("description")}
        </p>
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

        <div className="space-y-6">
          <div className="space-y-1.5">
            <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("tenantId")}</label>
            <input 
              type="text" 
              defaultValue="tenant_default_8x9a"
              disabled
              className="w-full bg-secondary border border-border rounded-lg px-4 py-2.5 text-sm text-muted-foreground cursor-not-allowed opacity-80"
            />
            <p className="text-[10px] text-muted-foreground ml-1 mt-1">{t("autoAssigned")}</p>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("tierStrategy")}</label>
            <select className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm">
              <option value="LIFETIME_POINTS">{t("lifetimePoints")}</option>
              <option value="YEARLY_POINTS">{t("yearlyPoints")}</option>
              <option value="MANUAL">{t("manualUpgrade")}</option>
            </select>
          </div>

          <div className="space-y-1.5 pt-4">
            <label className="text-xs font-semibold uppercase text-primary tracking-wider ml-1">{t("tierThresholds")}</label>
            <textarea 
              defaultValue={JSON.stringify({
                BRONZE: 0,
                SILVER: 1000,
                GOLD: 5000,
                PLATINUM: 20000
              }, null, 2)}
              rows={8}
              className="w-full bg-muted border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-primary resize-none shadow-inner"
            />
          </div>
        </div>

        <div className="pt-6 mt-6 border-t border-border flex justify-end">
          <button className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95">
            <Save className="w-4 h-4" />
            {t("saveChanges")}
          </button>
        </div>
      </div>
    </div>
  );
}
