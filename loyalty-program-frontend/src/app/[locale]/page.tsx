"use client";

import { useState } from "react";
import { useRouter } from "@/i18n/routing";
import { Shield } from "lucide-react";
import { useTranslations } from "next-intl";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

export default function APIKeyGateway() {
  const [apiKey, setApiKey] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const t = useTranslations("Login");

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!apiKey.trim()) return;

    setIsLoading(true);
    setTimeout(() => {
      sessionStorage.setItem("loyalty_api_key", apiKey);
      router.push("/portal/rules");
    }, 800);
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-6 relative overflow-hidden bg-background">
      {/* Language Switcher */}
      <div className="absolute top-6 right-6 z-20">
        <LanguageSwitcher />
      </div>

      {/* Decorative circles */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-[0.4]">
        <div className="absolute -top-64 -left-64 w-[800px] h-[800px] bg-secondary rounded-full blur-3xl mix-blend-multiply" />
        <div className="absolute -bottom-64 -right-64 w-[600px] h-[600px] bg-[#d7ccc8] rounded-full blur-3xl mix-blend-multiply opacity-50" />
      </div>

      <div className="w-full max-w-md z-10 space-y-8 bg-card p-10 rounded-xl shadow-xl shadow-primary/5 border border-border">
        <div className="space-y-3 text-center">
          <div className="mx-auto w-16 h-16 rounded-2xl bg-secondary flex items-center justify-center mb-6 shadow-sm border border-border">
            <Shield className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">{t("organizationAccess")}</h1>
          <p className="text-sm text-muted-foreground">
            {t("description")}
          </p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6 pt-4">
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
              {t("apiKeyLabel")}
            </label>
            <div className="relative">
              <input
                type="password"
                placeholder={t("apiKeyPlaceholder")}
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                required
                className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
          </div>
          <button
            type="submit"
            disabled={isLoading || !apiKey.trim()}
            className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
          >
            {isLoading ? t("authenticating") : t("accessDashboard")}
          </button>
        </form>
      </div>
      
      <div className="absolute bottom-8 text-center w-full text-xs text-muted-foreground/60 tracking-wider">
        {t("footer")} &copy; {new Date().getFullYear()}
      </div>
    </main>
  );
}