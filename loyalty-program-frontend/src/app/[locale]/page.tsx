"use client";

import { useEffect, useState } from "react";
import { useRouter } from "@/i18n/routing";
import { Gift, Key, User, Lock, Mail, Info } from "lucide-react";
import { useTranslations } from "next-intl";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

interface Member {
  id: string;
  name: string;
  email: string;
  balance: number;
  status: string;
  referrals: string[];
  referredBy?: string | null;
}

const DEFAULT_MEMBERS: Member[] = [
  { id: "usr_9921", name: "Amandine Dubois", email: "amandine@example.com", balance: 5400, status: "ACTIVE", referrals: [] },
  { id: "usr_9922", name: "Jean Dupont", email: "jean.dupont@example.com", balance: 1200, status: "PENDING_KYC", referrals: [] },
  { id: "usr_9923", name: "Alice Martin", email: "alice.martin@example.com", balance: 850, status: "ACTIVE", referrals: [] },
  { id: "usr_9924", name: "Paul Lambert", email: "paul.lambert@example.com", balance: 0, status: "FROZEN", referrals: [] }
];

export default function APIKeyGateway() {
  const [activeTab, setActiveTab] = useState<"api" | "credentials">("api");
  const [isRegistering, setIsRegistering] = useState(false);
  const [apiKey, setApiKey] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [refCode, setRefCode] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  const router = useRouter();
  const t = useTranslations("Login");

  // Read ref code from URL on mount
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const ref = params.get("ref");
    if (ref) {
      setRefCode(ref);
      // Automatically show registration if referred
      setIsRegistering(true);
      setActiveTab("credentials");
    }
  }, []);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    setTimeout(() => {
      if (activeTab === "api") {
        if (!apiKey.trim()) {
          setIsLoading(false);
          return;
        }
        sessionStorage.setItem("loyalty_api_key", apiKey);
      } else {
        if (!username.trim() || !password.trim()) {
          setIsLoading(false);
          return;
        }
        
        // Load members database
        let members: Member[] = [];
        const savedMembers = localStorage.getItem("loyalty_members");
        if (savedMembers) {
          try {
            members = JSON.parse(savedMembers);
          } catch (err) {
            members = DEFAULT_MEMBERS;
          }
        } else {
          members = DEFAULT_MEMBERS;
          localStorage.setItem("loyalty_members", JSON.stringify(DEFAULT_MEMBERS));
        }

        // Search for member or default to name
        const found = members.find(m => m.name.toLowerCase() === username.toLowerCase() || m.id === username);
        const sessionKey = found ? found.id : `usr_${username}`;
        sessionStorage.setItem("loyalty_api_key", sessionKey);
      }
      setIsLoading(false);
      router.push("/portal");
    }, 800);
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !email.trim() || !password.trim()) return;

    setIsLoading(true);
    setTimeout(() => {
      // 1. Load members database
      let members: Member[] = [];
      const savedMembers = localStorage.getItem("loyalty_members");
      if (savedMembers) {
        try {
          members = JSON.parse(savedMembers);
        } catch (err) {
          members = DEFAULT_MEMBERS;
        }
      } else {
        members = DEFAULT_MEMBERS;
      }

      // 2. Load referral settings
      const referralActive = localStorage.getItem("loyalty_referral_active") === "true";
      const referrerPoints = parseInt(localStorage.getItem("loyalty_referral_referrer_points") || "100");
      const refereePoints = parseInt(localStorage.getItem("loyalty_referral_referee_points") || "50");

      const newMemberId = `usr_${Math.floor(1000 + Math.random() * 9000)}`;
      const hasValidReferral = referralActive && refCode && members.some(m => m.id === refCode);
      const startBalance = hasValidReferral ? refereePoints : 0;

      // 3. Update Referrer points if applicable
      if (hasValidReferral) {
        members = members.map(m => {
          if (m.id === refCode) {
            return {
              ...m,
              balance: m.balance + referrerPoints,
              referrals: [...m.referrals, newMemberId]
            };
          }
          return m;
        });
      }

      // 4. Create new member
      const newMember: Member = {
        id: newMemberId,
        name: username,
        email: email,
        balance: startBalance,
        status: "ACTIVE",
        referrals: [],
        referredBy: hasValidReferral ? refCode : null
      };

      members.push(newMember);
      localStorage.setItem("loyalty_members", JSON.stringify(members));

      // 5. Authenticate and redirect
      sessionStorage.setItem("loyalty_api_key", newMemberId);
      setIsLoading(false);
      router.push("/portal");
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
        {/* Header */}
        <div className="space-y-3 text-center">
          <div className="mx-auto w-16 h-16 rounded-2xl bg-secondary flex items-center justify-center mb-6 shadow-sm border border-border">
            <Gift className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-3xl font-semibold tracking-tight text-foreground">
            {isRegistering
              ? t("registerTitle")
              : activeTab === "api"
              ? t("organizationAccess")
              : t("loginTitle")}
          </h1>
          <p className="text-sm text-muted-foreground">
            {!isRegistering && activeTab === "api" ? t("description") : ""}
          </p>
        </div>

        {/* Tab Selector (only shown when not registering) */}
        {!isRegistering && (
          <div className="flex bg-muted p-1 rounded-lg border border-border">
            <button
              type="button"
              onClick={() => setActiveTab("api")}
              className={`flex-1 py-2 text-xs font-semibold rounded-md transition-all ${
                activeTab === "api"
                  ? "bg-background text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {t("loginWithApiKey")}
            </button>
            <button
              type="button"
              onClick={() => setActiveTab("credentials")}
              className={`flex-1 py-2 text-xs font-semibold rounded-md transition-all ${
                activeTab === "credentials"
                  ? "bg-background text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {t("loginWithCredentials")}
            </button>
          </div>
        )}

        {isRegistering ? (
          /* Registration Form */
          <form onSubmit={handleRegister} className="space-y-6 pt-2">
            
            {/* Referral Info Alert */}
            {refCode && (
              <div className="bg-primary/10 border border-primary/20 text-primary px-4 py-3 rounded-lg text-xs font-medium flex items-center gap-2">
                <Info className="w-4 h-4 flex-shrink-0" />
                <span>{t("referredByAlert", { ref: refCode })}</span>
              </div>
            )}

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                  {t("nameLabel")} *
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                    <User className="w-5 h-5" />
                  </span>
                  <input
                    type="text"
                    placeholder={t("namePlaceholder")}
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                  {t("emailLabel")} *
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
                  {t("passwordLabel")} *
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
              disabled={isLoading || !username.trim() || !email.trim() || !password.trim()}
              className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
            >
              {isLoading ? t("authenticating") : t("registerButton")}
            </button>

            <div
              onClick={() => setIsRegistering(false)}
              className="text-sm text-center text-primary font-medium hover:underline cursor-pointer pt-2"
            >
              {t("alreadyHaveAccount")}
            </div>
          </form>
        ) : (
          /* Login Form */
          <form onSubmit={handleLogin} className="space-y-6 pt-2">
            {activeTab === "api" ? (
              /* API Key Form */
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
                    placeholder={t("apiKeyPlaceholder")}
                    value={apiKey}
                    onChange={(e) => setApiKey(e.target.value)}
                    required
                    className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
                  />
                </div>
              </div>
            ) : (
              /* Credentials Form */
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                    {t("usernameLabel")}
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-3.5 text-muted-foreground/60">
                      <User className="w-5 h-5" />
                    </span>
                    <input
                      type="text"
                      placeholder="Login"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
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
            )}

            <button
              type="submit"
              disabled={
                isLoading ||
                (activeTab === "api" ? !apiKey.trim() : !username.trim() || !password.trim())
              }
              className="inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 py-2 w-full disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
            >
              {isLoading ? t("authenticating") : t("accessDashboard")}
            </button>

            {activeTab === "credentials" && (
              <div
                onClick={() => setIsRegistering(true)}
                className="text-sm text-center text-primary font-medium hover:underline cursor-pointer pt-2"
              >
                {t("registerLink")}
              </div>
            )}
          </form>
        )}
      </div>

      <div className="absolute bottom-8 text-center w-full text-xs text-muted-foreground/60 tracking-wider">
        {t("footer")} &copy; {new Date().getFullYear()}
      </div>
    </main>
  );
}