"use client";

import { useState } from "react";
import { useRouter } from "@/i18n/routing";
import { Gift, Lock, Mail, AlertTriangle, Cpu, Key, ShieldCheck } from "lucide-react";
import { useTranslations } from "next-intl";
import { LandingHeader } from "@/components/LandingHeader";
import { authApi, LoginResponse } from "@/lib/api";

export default function AdminLoginPage() {
  const [mode, setMode] = useState<"credentials" | "apiKey">("credentials");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [apiKey, setApiKey] = useState("");
  // Défi MFA en cours : le backend a renvoyé mfaRequired=true + mfaToken
  // (code envoyé par email), on attend la saisie du code.
  const [mfaToken, setMfaToken] = useState<string | null>(null);
  const [mfaCode, setMfaCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const router = useRouter();
  const t = useTranslations("Login");

  const completeLogin = (response: LoginResponse) => {
    sessionStorage.setItem("loyalty_jwt_token", response.token);
    sessionStorage.setItem("loyalty_organization_id", response.organizationId);
    router.push("/portal");
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      const response = await authApi.login({ email: email.trim(), password });
      if (response.mfaRequired && response.mfaToken) {
        setMfaToken(response.mfaToken);
        setMfaCode("");
      } else {
        completeLogin(response);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfirmMfa = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!mfaToken || !mfaCode.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      const response = await authApi.confirmMfa({ mfaToken, code: mfaCode.trim() });
      completeLogin(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  const resetMfa = () => {
    setMfaToken(null);
    setMfaCode("");
    setError(null);
  };

  // Onboarding développeur : la clé API du tenant vaut authentification côté
  // backend, elle débloque directement le portail.
  const handleApiKeyLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!apiKey.trim()) return;
    sessionStorage.setItem("loyalty_dev_api_key", apiKey.trim());
    router.push("/portal");
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-6 pt-24 relative overflow-hidden bg-background">
      <LandingHeader />

      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-[0.4]">
        <div className="absolute -top-64 -left-64 w-[800px] h-[800px] bg-secondary rounded-full blur-3xl mix-blend-multiply" />
        <div className="absolute -bottom-64 -right-64 w-[600px] h-[600px] bg-[#d7ccc8] rounded-full blur-3xl mix-blend-multiply opacity-50" />
      </div>

      <div className="w-full max-w-md z-10 space-y-8 bg-card p-10 rounded-xl shadow-xl shadow-primary/5 border border-border">
        <div className="space-y-3 text-center">
          <div className="mx-auto w-16 h-16 rounded-2xl bg-secondary flex items-center justify-center mb-6 shadow-sm border border-border">
            {mfaToken ? (
              <ShieldCheck className="w-8 h-8 text-primary" />
            ) : mode === "credentials" ? (
              <Gift className="w-8 h-8 text-primary" />
            ) : (
              <Cpu className="w-8 h-8 text-primary" />
            )}
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            {mfaToken ? t("mfaTitle") : mode === "credentials" ? t("loginTitle") : t("apiKeyTitle")}
          </h1>
          <p className="text-sm text-muted-foreground">
            {mfaToken ? t("mfaDescription") : mode === "credentials" ? t("adminDescription") : t("apiKeyDescription")}
          </p>
        </div>

        {!mfaToken && (
        <div className="grid grid-cols-2 gap-1 p-1 rounded-lg bg-secondary/60 border border-border">
          <button
            type="button"
            onClick={() => { setMode("credentials"); setError(null); }}
            className={`flex items-center justify-center gap-2 py-2 rounded-md text-xs font-semibold transition-all ${
              mode === "credentials"
                ? "bg-card text-foreground shadow-sm"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Mail className="w-3.5 h-3.5" />
            {t("credentialsTab")}
          </button>
          <button
            type="button"
            onClick={() => { setMode("apiKey"); setError(null); }}
            className={`flex items-center justify-center gap-2 py-2 rounded-md text-xs font-semibold transition-all ${
              mode === "apiKey"
                ? "bg-card text-foreground shadow-sm"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            <Key className="w-3.5 h-3.5" />
            {t("apiKeyTab")}
          </button>
        </div>
        )}

        {mfaToken ? (
        <form onSubmit={handleConfirmMfa} className="space-y-6 pt-2">
          {error && (
            <div className="bg-destructive/10 border border-destructive/20 text-destructive px-4 py-3 rounded-lg text-xs font-medium flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
              {t("mfaCodeLabel")}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                <ShieldCheck className="w-5 h-5" />
              </span>
              <input
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                autoFocus
                placeholder={t("mfaCodePlaceholder")}
                value={mfaCode}
                onChange={(e) => setMfaCode(e.target.value)}
                required
                className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary tracking-[0.3em]"
              />
            </div>
          </div>
          <button
            type="submit"
            disabled={isLoading || !mfaCode.trim()}
            className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
          >
            {isLoading ? t("mfaVerifying") : t("mfaConfirm")}
          </button>
          <button
            type="button"
            onClick={resetMfa}
            className="w-full text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            {t("mfaBack")}
          </button>
        </form>
        ) : mode === "apiKey" ? (
        <form onSubmit={handleApiKeyLogin} className="space-y-6 pt-2">
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
              {t("apiKeyLabel")}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                <Key className="w-5 h-5" />
              </span>
              <input
                type="password"
                placeholder="sk_live_..."
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                required
                className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>
          </div>
          <button
            type="submit"
            disabled={!apiKey.trim()}
            className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
          >
            {t("unlockPortal")}
          </button>
        </form>
        ) : (
        <form onSubmit={handleLogin} className="space-y-6 pt-2">
          {error && (
            <div className="bg-destructive/10 border border-destructive/20 text-destructive px-4 py-3 rounded-lg text-xs font-medium flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="space-y-4">
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                {t("emailLabel")}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                  <Mail className="w-5 h-5" />
                </span>
                <input
                  type="text"
                  autoComplete="username"
                  placeholder={t("emailPlaceholder")}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                {t("passwordLabel")}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                  <Lock className="w-5 h-5" />
                </span>
                <input
                  type="password"
                  placeholder={t("passwordPlaceholder")}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
                />
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading || !email.trim() || !password.trim()}
            className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
          >
            {isLoading ? t("authenticating") : t("accessDashboard")}
          </button>
        </form>
        )}
      </div>

      <div className="absolute bottom-8 text-center w-full text-xs text-muted-foreground/60 tracking-wider">
        {t("footer")} &copy; {new Date().getFullYear()}
      </div>
    </main>
  );
}
