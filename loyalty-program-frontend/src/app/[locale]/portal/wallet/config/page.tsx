"use client";

import { Save, Wallet } from "lucide-react";
import { useState } from "react";
import { useTranslations } from "next-intl";

export default function WalletConfigPage() {
  const [isSaving, setIsSaving] = useState(false);
  const t = useTranslations("Wallet");

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setTimeout(() => setIsSaving(false), 800);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="space-y-1">
        <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground text-sm">
          {t("description")}
        </p>
      </div>

      <form onSubmit={handleSave} className="border border-border bg-card rounded-xl shadow-sm overflow-hidden mt-6">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
          <Wallet className="w-5 h-5 text-primary" />
          <h3 className="font-medium text-foreground">{t("globalRules")}</h3>
        </div>

        <div className="p-8 space-y-8">
          {/* Currency Info */}
          <div>
            <h4 className="text-sm font-semibold uppercase text-primary tracking-wider mb-4 border-b border-border pb-2">{t("virtualCurrency")}</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("currencyName")}</label>
                <input type="text" defaultValue="RidnCoins" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("currencySymbol")}</label>
                <input type="text" defaultValue="RC" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("exchangeRate")}</label>
                <input type="number" step="0.01" defaultValue="1.00" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
            </div>
          </div>

          {/* Limits */}
          <div>
            <h4 className="text-sm font-semibold uppercase text-primary tracking-wider mb-4 border-b border-border pb-2">{t("transactionLimits")}</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("dailySpendCap")}</label>
                <input type="number" placeholder="No limit" defaultValue="10000" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("maxBalance")}</label>
                <input type="number" placeholder="No limit" defaultValue="50000" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("maxTopUp")}</label>
                <input type="number" placeholder="No limit" defaultValue="5000" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("minWithdrawal")}</label>
                <input type="number" placeholder="No limit" defaultValue="500" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
            </div>
          </div>

          {/* Security & Delays */}
          <div>
            <h4 className="text-sm font-semibold uppercase text-primary tracking-wider mb-4 border-b border-border pb-2">{t("securityDelays")}</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("otpThreshold")}</label>
                <input type="number" placeholder="Amount above which OTP is required" defaultValue="2000" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
                <p className="text-[10px] text-muted-foreground ml-1">{t("otpDesc")}</p>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("withdrawalDelay")}</label>
                <input type="number" defaultValue="24" className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm" />
              </div>
              
              <div className="space-y-3 pt-2 md:col-span-2">
                <label className="flex items-center gap-3">
                  <input type="checkbox" defaultChecked className="w-4 h-4 text-primary bg-background border-border rounded focus:ring-primary" />
                  <span className="text-sm font-medium text-foreground">{t("requireKyc")}</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-secondary/50 px-8 py-4 border-t border-border flex justify-end">
          <button 
            type="submit" 
            disabled={isSaving}
            className="flex items-center gap-2 bg-primary text-primary-foreground px-8 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {isSaving ? t("saving") : t("savePolicy")}
          </button>
        </div>
      </form>
    </div>
  );
}
