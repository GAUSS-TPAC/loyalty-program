"use client";

import { useState } from "react";
import { useRouter, Link } from "@/i18n/routing";
import { ArrowLeft, Save, Webhook } from "lucide-react";
import { useTranslations } from "next-intl";

export default function NewRulePage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const t = useTranslations("RulesNew");

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    trigger: "",
    priority: "10",
    conditions: "[]",
    effects: "[\n  {\n    \"type\": \"CREDIT_POINTS\",\n    \"params\": {\n      \"amount\": 100\n    }\n  }\n]"
  });

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    // Simulate API call to create rule
    setTimeout(() => {
      setIsLoading(false);
      router.push("/portal/rules");
    }, 600);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-4">
        <Link href="/portal/rules" className="p-2 text-muted-foreground hover:bg-secondary rounded-lg transition-colors">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground text-sm">
            {t("description")}
          </p>
        </div>
      </div>

      <form onSubmit={handleSave} className="border border-border bg-card rounded-xl shadow-sm overflow-hidden mt-6">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
          <Webhook className="w-5 h-5 text-primary" />
          <h3 className="font-medium text-foreground">{t("config")}</h3>
        </div>
        
        <div className="p-8 space-y-8">
          {/* Basic Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("ruleName")}</label>
              <input 
                type="text" 
                required
                placeholder={t("ruleNamePlaceholder")}
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
            </div>
            
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("triggerEvent")}</label>
              <input 
                type="text" 
                required
                placeholder={t("triggerEventPlaceholder")}
                value={formData.trigger}
                onChange={(e) => setFormData({...formData, trigger: e.target.value})}
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary text-primary font-mono transition-all shadow-sm"
              />
            </div>

            <div className="space-y-1.5 md:col-span-2">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("ruleDescription")}</label>
              <input 
                type="text" 
                placeholder={t("ruleDescriptionPlaceholder")}
                value={formData.description}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
            </div>
            
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("executionPriority")}</label>
              <input 
                type="number" 
                value={formData.priority}
                onChange={(e) => setFormData({...formData, priority: e.target.value})}
                className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
              />
              <p className="text-[10px] text-muted-foreground ml-1">{t("executionPriorityDesc")}</p>
            </div>
          </div>

          <hr className="border-border" />

          {/* JSON Configs */}
          <div className="space-y-6">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("conditionsArray")}</label>
              <textarea 
                required
                value={formData.conditions}
                onChange={(e) => setFormData({...formData, conditions: e.target.value})}
                rows={4}
                placeholder={"[\n  {\n    \"type\": \"...\"\n  }\n]"}
                className="w-full bg-background border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-foreground resize-y shadow-sm"
              />
            </div>
            
            <div className="space-y-1.5">
              <label className="text-xs font-semibold uppercase text-primary tracking-wider ml-1">{t("effectsArray")}</label>
              <textarea 
                required
                value={formData.effects}
                onChange={(e) => setFormData({...formData, effects: e.target.value})}
                rows={8}
                className="w-full bg-muted border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary font-mono text-primary resize-y shadow-inner"
              />
            </div>
          </div>
        </div>

        <div className="bg-secondary/50 px-8 py-4 border-t border-border flex justify-end gap-3">
          <Link href="/portal/rules" className="px-6 py-2.5 rounded-lg text-sm font-medium text-foreground bg-background border border-border hover:bg-secondary transition-all shadow-sm">
            {t("cancel")}
          </Link>
          <button 
            type="submit" 
            disabled={isLoading}
            className="flex items-center gap-2 bg-primary text-primary-foreground px-8 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {isLoading ? t("saving") : t("createRule")}
          </button>
        </div>
      </form>
    </div>
  );
}
