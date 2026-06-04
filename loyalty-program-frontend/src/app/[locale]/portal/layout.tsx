"use client";

import { useEffect, useState } from "react";
import { useRouter, Link } from "@/i18n/routing";
import { usePathname } from "next/navigation";
import { Terminal, Settings, LogOut, Code2, Cpu, Wallet, Users } from "lucide-react";
import { useTranslations } from "next-intl";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

export default function PortalLayout({ children }: { children: React.ReactNode }) {
  const [apiKey, setApiKey] = useState<string | null>(null);
  const router = useRouter();
  const pathname = usePathname();
  const tNav = useTranslations("Navigation");
  const tSide = useTranslations("Sidebar");
  const tHeader = useTranslations("Header");

  useEffect(() => {
    const key = sessionStorage.getItem("loyalty_api_key");
    if (!key) {
      router.push("/");
    } else {
      // eslint-disable-next-line
      setTimeout(() => setApiKey(key), 0);
    }
  }, [router]);

  const handleLogout = () => {
    sessionStorage.removeItem("loyalty_api_key");
    router.push("/");
  };

  if (!apiKey) return <div className="min-h-screen bg-background" />;

  // The pathname from next/navigation might have /en/ or /fr/ in it.
  // Using next-intl's usePathname removes the locale prefix, making it easier to match!
  // Oh wait, next/navigation usePathname includes the locale. Let's fix that below by importing from @/i18n/routing if needed.
  // Actually, I can just use next-intl's usePathname.
  // But wait! next-intl's usePathname requires a different import. I'll stick to next/navigation for now and do a simple string match.

  const navItems = [
    { name: tNav("rulesConfig"), href: "/portal/rules", icon: Code2 },
    { name: tNav("establishment"), href: "/portal/establishment", icon: Settings },
    { name: tNav("walletPolicy"), href: "/portal/wallet/config", icon: Wallet },
    { name: tNav("membersDirectory"), href: "/portal/members", icon: Users },
    { name: tNav("eventLogs"), href: "/portal/logs", icon: Terminal },
  ];

  return (
    <div className="flex min-h-screen bg-background text-foreground font-sans selection:bg-primary selection:text-white">
      {/* Sidebar */}
      <aside className="w-64 border-r border-border bg-card flex flex-col shadow-sm z-20">
        <div className="p-6 border-b border-border">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-8 h-8 rounded-lg bg-secondary flex items-center justify-center border border-border">
              <Cpu className="w-4 h-4 text-primary" />
            </div>
            <span className="font-semibold tracking-wide text-foreground">{tSide("loyaltyCore")}</span>
          </div>
          <div className="text-xs text-muted-foreground truncate bg-muted px-2 py-1 rounded-md border border-border" title={apiKey}>
            {tSide("key")}{apiKey.substring(0, 14)}...
          </div>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => {
            const isActive = pathname.includes(item.href);
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
            {tSide("disconnect")}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col overflow-hidden relative">
        <header className="h-16 border-b border-border flex items-center justify-between px-8 bg-card/80 backdrop-blur sticky top-0 z-10 shadow-sm">
          <h2 className="text-sm font-medium text-muted-foreground uppercase tracking-wider">
            {tHeader("dashboard")}
          </h2>
          <div>
            <LanguageSwitcher />
          </div>
        </header>
        <div className="flex-1 overflow-auto p-8 bg-background">
          <div className="max-w-6xl mx-auto space-y-8 pb-12">
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}
