"use client";

import { useState } from "react";
import { useRouter } from "@/i18n/routing";
import { Gift, Lock, Mail, AlertTriangle, Cpu, Key, Building2, Eye, EyeOff } from "lucide-react";
import { useTranslations } from "next-intl";
import { LandingHeader } from "@/components/LandingHeader";
import { authApi, ApiError } from "@/lib/api";

interface OrganizationChoice {
  organizationId: string;
  organizationCode: string;
  displayName: string;
}

export default function AdminLoginPage() {
  const [mode, setMode] = useState<"credentials" | "apiKey">("credentials");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [apiKey, setApiKey] = useState("");
  const [showApiKey, setShowApiKey] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // Compte multi-organisations : le backend répond ORGANIZATION_SELECTION_REQUIRED
  // avec la liste des organisations ; on la propose et on re-soumet avec organizationId.
  const [organizations, setOrganizations] = useState<OrganizationChoice[] | null>(null);
  const [selectedOrgId, setSelectedOrgId] = useState("");

  const router = useRouter();
  const t = useTranslations("Login");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) return;
    setIsLoading(true);
    setError(null);
    try {
      const { token, organizationId } = await authApi.login({
        email: email.trim(),
        password,
        organizationId: selectedOrgId || undefined,
      });
      sessionStorage.setItem("loyalty_jwt_token", token);
      sessionStorage.setItem("loyalty_organization_id", organizationId);
      router.push("/portal");
    } catch (err) {
      if (err instanceof ApiError && err.problem?.title === "ORGANIZATION_SELECTION_REQUIRED") {
        const orgs = (err.problem.errors?.organizations ?? []) as OrganizationChoice[];
        setOrganizations(orgs);
        setSelectedOrgId(orgs[0]?.organizationId ?? "");
      } else {
        setError(err instanceof Error ? err.message : "Login failed");
      }
    } finally {
      setIsLoading(false);
    }
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
            {mode === "credentials" ? (
              <Gift className="w-8 h-8 text-primary" />
            ) : (
              <Cpu className="w-8 h-8 text-primary" />
            )}
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            {mode === "credentials" ? t("loginTitle") : t("apiKeyTitle")}
          </h1>
          <p className="text-sm text-muted-foreground">
            {mode === "credentials" ? t("adminDescription") : t("apiKeyDescription")}
          </p>
        </div>

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

        {mode === "apiKey" ? (
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
                type={showApiKey ? "text" : "password"}
                placeholder="sk_live_..."
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                required
                className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-11 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
              <button
                type="button"
                onClick={() => setShowApiKey((v) => !v)}
                tabIndex={-1}
                aria-label={showApiKey ? "Masquer la clé" : "Afficher la clé"}
                className="absolute right-3 top-3.5 text-muted-foreground/60 hover:text-foreground transition-colors"
              >
                {showApiKey ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
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
                  type="email"
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
                  type={showPassword ? "text" : "password"}
                  placeholder={t("passwordPlaceholder")}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-11 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  tabIndex={-1}
                  aria-label={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
                  className="absolute right-3 top-3.5 text-muted-foreground/60 hover:text-foreground transition-colors"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {organizations && organizations.length > 0 && (
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                  {t("organizationLabel")}
                </label>
                <p className="text-xs text-muted-foreground ml-1">{t("organizationHelp")}</p>
                <div className="relative">
                  <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                    <Building2 className="w-5 h-5" />
                  </span>
                  <select
                    value={selectedOrgId}
                    onChange={(e) => setSelectedOrgId(e.target.value)}
                    required
                    className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
                  >
                    {organizations.map((org) => (
                      <option key={org.organizationId} value={org.organizationId}>
                        {org.displayName !== "null" && org.displayName
                          ? org.displayName
                          : org.organizationCode}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            )}
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
