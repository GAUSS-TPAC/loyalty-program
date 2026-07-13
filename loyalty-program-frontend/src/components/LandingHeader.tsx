"use client";

import { useTranslations } from "next-intl";
import { Link } from "@/i18n/routing";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { useState, useEffect } from "react";
import { Menu, X, Gift } from "lucide-react";

export function LandingHeader() {
  const t = useTranslations("Landing");
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        isScrolled
          ? "bg-background/90 backdrop-blur-md shadow-sm py-3"
          : "bg-transparent py-5"
      }`}
    >
      <div className="container mx-auto px-4 md:px-6">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <Link
            href="/"
            className="flex items-center gap-2 group transition-opacity hover:opacity-80"
          >
            <div className="p-1.5 bg-primary/10 rounded-lg group-hover:bg-primary/20 transition-colors">
              <Gift className="w-6 h-6 text-primary" />
            </div>
            <span className="font-bold text-xl tracking-tight text-foreground">
              Loyalty Engine
            </span>
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-6">
            <a
              href="#solutions"
              className="text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
            >
              {t("navSolutions")}
            </a>
            <a
              href="#rewards"
              className="text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
            >
              {t("navRewards")}
            </a>
            <a
              href="#api"
              className="text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
            >
              {t("navApiPricing")}
            </a>
            <a
              href="#developers"
              className="text-sm font-medium text-muted-foreground hover:text-primary transition-colors"
            >
              {t("navDevelopers")}
            </a>
          </nav>

          {/* Actions */}
          <div className="hidden md:flex items-center gap-4">
            <LanguageSwitcher />
            <Link
              href="/login"
              className="text-sm font-semibold text-primary hover:text-primary/80 transition-colors"
            >
              {t("login")}
            </Link>
            <Link
              href="/register"
              className="px-4 py-2 text-sm font-semibold text-primary-foreground bg-primary rounded-lg shadow-sm hover:bg-primary/90 transition-all hover:scale-105 active:scale-95"
            >
              {t("openAccount")}
            </Link>
          </div>

          {/* Mobile Actions */}
          <div className="flex items-center gap-2 md:hidden">
            <LanguageSwitcher />
            {/* Mobile Menu Toggle */}
            <button
              className="p-2 text-foreground"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden absolute top-full left-0 right-0 bg-background border-t border-border shadow-lg py-4 px-4 flex flex-col gap-4">
          <a
            href="#solutions"
            className="block text-sm font-medium text-foreground py-2 border-b border-border/50"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            {t("navSolutions")}
          </a>
          <a
            href="#rewards"
            className="block text-sm font-medium text-foreground py-2 border-b border-border/50"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            {t("navRewards")}
          </a>
          <a
            href="#api"
            className="block text-sm font-medium text-foreground py-2 border-b border-border/50"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            {t("navApiPricing")}
          </a>
          <a
            href="#developers"
            className="block text-sm font-medium text-foreground py-2 border-b border-border/50"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            {t("navDevelopers")}
          </a>
          
          <div className="flex flex-col gap-3 pt-2">
            <Link
              href="/login"
              className="block w-full text-center py-2 text-sm font-semibold text-primary border border-primary rounded-lg"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              {t("login")}
            </Link>
            <Link
              href="/register"
              className="block w-full text-center py-2 text-sm font-semibold text-primary-foreground bg-primary rounded-lg shadow-sm"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              {t("openAccount")}
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
