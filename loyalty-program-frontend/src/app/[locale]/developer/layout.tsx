"use client";

import { useEffect, useState } from "react";
import { Link, usePathname } from "@/i18n/routing";
import {
  Cpu,
  LayoutDashboard,
  Key,
  Webhook,
  Terminal,
  BookOpen,
  FlaskConical,
  LogOut,
  Menu,
  X,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

const TOKEN_KEY = "loyalty_jwt_token";

export default function DeveloperLayout({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [tokenInput, setTokenInput] = useState("");
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [checked, setChecked] = useState(false);
  const pathname = usePathname();
  const t = useTranslations("Developer");

  useEffect(() => {
    setToken(sessionStorage.getItem(TOKEN_KEY));
    setChecked(true);
  }, []);

  useEffect(() => {
    setIsMobileOpen(false);
  }, [pathname]);

  const handleUnlock = (e: React.FormEvent) => {
    e.preventDefault();
    if (!tokenInput.trim()) return;
    sessionStorage.setItem(TOKEN_KEY, tokenInput.trim());
    setToken(tokenInput.trim());
  };

  const handleLogout = () => {
    sessionStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setTokenInput("");
  };

  if (!checked) {
    return <div className="min-h-screen bg-background" />;
  }

  if (!token) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-6">
        <form
          onSubmit={handleUnlock}
          className="w-full max-w-md bg-card border border-border rounded-xl shadow-sm p-8 space-y-5"
        >
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center border border-border">
              <Cpu className="w-4.5 h-4.5 text-primary" />
            </div>
            <h1 className="font-semibold text-foreground">{t("tokenGateTitle")}</h1>
          </div>
          <p className="text-sm text-muted-foreground">{t("tokenGateDescription")}</p>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">{t("tokenLabel")}</label>
            <input
              type="password"
              value={tokenInput}
              onChange={(e) => setTokenInput(e.target.value)}
              placeholder={t("tokenPlaceholder")}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <button
            type="submit"
            className="w-full py-2.5 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity"
          >
            {t("unlock")}
          </button>
        </form>
      </div>
    );
  }

  const navItems = [
    { name: t("navDashboard"), href: "/developer", exact: true, icon: LayoutDashboard },
    { name: t("navApiKeys"), href: "/developer/api-keys", icon: Key },
    { name: t("navWebhooks"), href: "/developer/webhooks", icon: Webhook },
    { name: t("navLogs"), href: "/developer/logs", icon: Terminal },
    { name: t("navSandbox"), href: "/developer/sandbox", icon: FlaskConical },
    { name: t("navDocs"), href: "/developer/docs", icon: BookOpen },
  ];

  return (
    <div className="flex min-h-screen bg-background text-foreground font-sans selection:bg-primary selection:text-white">
      {isMobileOpen && (
        <div
          className="fixed inset-0 bg-background/60 backdrop-blur-sm z-20 md:hidden"
          onClick={() => setIsMobileOpen(false)}
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 w-64 border-r border-border bg-card flex flex-col shadow-sm z-35 transform transition-transform duration-300 md:translate-x-0 md:static ${
          isMobileOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="p-6 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-secondary flex items-center justify-center border border-border">
              <Cpu className="w-4 h-4 text-primary" />
            </div>
            <span className="font-semibold tracking-wide text-foreground">{t("loyaltyCore")}</span>
          </div>
          <button
            onClick={() => setIsMobileOpen(false)}
            className="md:hidden p-1.5 rounded-lg hover:bg-secondary text-muted-foreground active:scale-95 transition-all"
          >
            <X className="w-4.5 h-4.5" />
          </button>
        </div>

        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const cleanPath = pathname || "/";
            const isActive = item.exact ? cleanPath === item.href : cleanPath.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2.5 text-sm transition-all rounded-md ${
                  isActive
                    ? "bg-primary text-primary-foreground font-medium shadow-sm"
                    : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                }`}
              >
                <item.icon className={`w-4 h-4 ${isActive ? "text-primary-foreground" : ""}`} />
                {item.name}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-border">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3 px-3 py-2.5 text-sm text-destructive hover:bg-destructive/10 rounded-md transition-colors"
          >
            <LogOut className="w-4 h-4" />
            {t("disconnect")}
          </button>
        </div>
      </aside>

      <main className="flex-1 flex flex-col overflow-hidden relative">
        <header className="h-16 border-b border-border flex items-center justify-between px-6 md:px-8 bg-card/80 backdrop-blur sticky top-0 z-10 shadow-sm">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsMobileOpen(true)}
              className="md:hidden p-2 -ml-2 rounded-lg hover:bg-secondary text-muted-foreground active:scale-95 transition-all"
            >
              <Menu className="w-5 h-5" />
            </button>
            <h2 className="text-sm font-medium text-muted-foreground uppercase tracking-wider">
              {t("loyaltyCore")}
            </h2>
          </div>
          <LanguageSwitcher />
        </header>
        <div className="flex-1 overflow-auto p-4 sm:p-6 md:p-8 bg-background">
          <div className="max-w-6xl mx-auto space-y-8 pb-12">{children}</div>
        </div>
      </main>
    </div>
  );
}
