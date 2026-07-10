"use client";

import { useEffect, useState } from "react";
import { Link } from "@/i18n/routing";
import { Shield, LogOut, Building2 } from "lucide-react";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

const PLATFORM_KEY_STORAGE_KEY = "loyalty_platform_admin_key";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const [platformKey, setPlatformKey] = useState<string | null>(null);
  const [keyInput, setKeyInput] = useState("");
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- one-time session read on mount
    setPlatformKey(sessionStorage.getItem(PLATFORM_KEY_STORAGE_KEY));
    setChecked(true);
  }, []);

  const handleUnlock = (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyInput.trim()) return;
    sessionStorage.setItem(PLATFORM_KEY_STORAGE_KEY, keyInput.trim());
    setPlatformKey(keyInput.trim());
  };

  const handleLogout = () => {
    sessionStorage.removeItem(PLATFORM_KEY_STORAGE_KEY);
    setPlatformKey(null);
    setKeyInput("");
  };

  if (!checked) return <div className="min-h-screen bg-background" />;

  if (!platformKey) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-6">
        <form
          onSubmit={handleUnlock}
          className="w-full max-w-md bg-card border border-border rounded-xl shadow-sm p-8 space-y-5"
        >
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center border border-border">
              <Shield className="w-4.5 h-4.5 text-primary" />
            </div>
            <h1 className="font-semibold text-foreground">Console plateforme</h1>
          </div>
          <p className="text-sm text-muted-foreground">
            Accès réservé à l&apos;équipe Yowyob. Collez le secret plateforme.
          </p>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Secret plateforme</label>
            <input
              type="password"
              value={keyInput}
              onChange={(e) => setKeyInput(e.target.value)}
              placeholder="X-Platform-Admin-Key"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <button
            type="submit"
            className="w-full py-2.5 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity"
          >
            Déverrouiller
          </button>
          <p className="text-xs text-center text-muted-foreground">
            Vous êtes un tenant ?{" "}
            <Link href="/portal" className="text-primary hover:underline">
              Portail développeur
            </Link>
          </p>
        </form>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground font-sans">
      <header className="h-16 border-b border-border flex items-center justify-between px-6 md:px-8 bg-card/80 backdrop-blur sticky top-0 z-10 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-secondary flex items-center justify-center border border-border">
            <Shield className="w-4 h-4 text-primary" />
          </div>
          <span className="font-semibold tracking-wide text-foreground">Console Plateforme</span>
          <span className="hidden sm:flex items-center gap-1.5 text-xs text-muted-foreground border border-border rounded-full px-2.5 py-0.5 ml-2">
            <Building2 className="w-3 h-3" /> Organisations
          </span>
        </div>
        <div className="flex items-center gap-3">
          <LanguageSwitcher />
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-3 py-1.5 text-xs text-destructive hover:bg-destructive/10 rounded-md transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
            Déconnexion
          </button>
        </div>
      </header>
      <div className="p-4 sm:p-6 md:p-8 max-w-6xl mx-auto space-y-8 pb-12">{children}</div>
    </div>
  );
}
