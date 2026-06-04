"use client";

import { useState } from "react";
import { Plus, Save, Trash2, Webhook } from "lucide-react";
import { Link } from "@/i18n/routing";
import { useTranslations } from "next-intl";

export default function RulesConfiguration() {
  const [rules] = useState([
    {
      id: "rule_1",
      name: "Welcome Bonus",
      trigger: "account.created",
      conditions: "[]",
      effects: '[\n  {\n    "type": "CREDIT_POINTS",\n    "params": {\n      "amount": 500\n    }\n  }\n]',
    }
  ]);

  const t = useTranslations("Rules");

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-start">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground text-sm">
            {t("description")}
          </p>
        </div>
        <Link href="/portal/rules/new" className="flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm active:scale-95">
          <Plus className="w-4 h-4" />
          {t("addRule")}
        </Link>
      </div>

      <div className="space-y-6">
        {rules.map((rule) => (
          <div key={rule.id} className="border border-border bg-card rounded-xl shadow-sm overflow-hidden group">
            <div className="bg-secondary px-6 py-3 border-b border-border flex justify-between items-center">
              <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                <Webhook className="w-4 h-4 text-primary" />
                {t("ruleId")} <span className="font-mono text-muted-foreground ml-1">{rule.id}</span>
              </div>
              <div className="flex gap-2">
                <button className="p-1.5 text-muted-foreground hover:text-primary bg-background rounded-md border border-border shadow-sm transition-colors">
                  <Save className="w-4 h-4" />
                </button>
                <button className="p-1.5 text-muted-foreground hover:text-destructive bg-background rounded-md border border-border shadow-sm transition-colors">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
            
            <div className="p-6 grid grid-cols-12 gap-8">
              <div className="col-span-12 md:col-span-4 space-y-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("ruleName")}</label>
                  <input 
                    type="text" 
                    defaultValue={rule.name}
                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("triggerEvent")}</label>
                  <input 
                    type="text" 
                    defaultValue={rule.trigger}
                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-primary font-mono transition-all shadow-sm"
                  />
                </div>
              </div>

              <div className="col-span-12 md:col-span-8 space-y-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("conditionsArray")}</label>
                  <textarea 
                    defaultValue={rule.conditions}
                    rows={2}
                    className="w-full bg-background border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-foreground resize-none shadow-sm"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-primary tracking-wider ml-1">{t("effectsArray")}</label>
                  <textarea 
                    defaultValue={rule.effects}
                    rows={7}
                    className="w-full bg-muted border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-primary resize-none shadow-inner"
                  />
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
