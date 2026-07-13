"use client";

import { useTranslations } from "next-intl";
import { Gift } from "lucide-react";
import { Link } from "@/i18n/routing";

export function LandingFooter() {
  const t = useTranslations("Landing");
  
  return (
    <footer className="bg-secondary/30 pt-20 pb-10 mt-auto border-t border-border">
      <div className="container mx-auto px-4 md:px-6">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-12 lg:gap-8">
          
          {/* Brand Info */}
          <div className="md:col-span-5 lg:col-span-4">
            <Link href="/" className="flex items-center gap-2 mb-6 opacity-90 hover:opacity-100 transition-opacity">
              <div className="p-1.5 bg-primary/10 rounded-lg">
                <Gift className="w-6 h-6 text-primary" />
              </div>
              <span className="font-bold text-xl tracking-tight text-foreground">
                Loyalty Engine
              </span>
            </Link>
            
            <p className="text-sm text-muted-foreground leading-relaxed mb-6">
              {t("footerDesc")}
            </p>
            
            <ul className="space-y-3 text-sm">
              <li>
                <span className="font-semibold text-primary">{t("footerSol1Title")}</span>
                <span className="text-muted-foreground">{t("footerSol1Desc")}</span>
              </li>
              <li>
                <span className="font-semibold text-primary">{t("footerSol2Title")}</span>
                <span className="text-muted-foreground">{t("footerSol2Desc")}</span>
              </li>
              <li>
                <span className="font-semibold text-primary">{t("footerSol3Title")}</span>
                <span className="text-muted-foreground">{t("footerSol3Desc")}</span>
              </li>
            </ul>
          </div>
          
          {/* Spacer */}
          <div className="hidden lg:block lg:col-span-1"></div>
          
          {/* Links: Solutions */}
          <div className="md:col-span-3 lg:col-span-3">
            <h4 className="font-semibold text-foreground mb-6 uppercase tracking-wider text-xs">
              {t("navSolutions")}
            </h4>
            <ul className="space-y-4">
              <li>
                <a href="#wallet" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("solutionWalletTitle")}
                </a>
              </li>
              <li>
                <a href="#api" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("solutionApiTitle")}
                </a>
              </li>
              <li>
                <a href="#rewards" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("solutionRewardsTitle")}
                </a>
              </li>
            </ul>
          </div>
          
          {/* Links: Resources */}
          <div className="md:col-span-4 lg:col-span-3">
            <h4 className="font-semibold text-foreground mb-6 uppercase tracking-wider text-xs">
              {t("footerResources")}
            </h4>
            <ul className="space-y-4">
              <li>
                <a href="#" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("footerHowItWorks")}
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("footerTerms")}
                </a>
              </li>
              <li>
                <a href="#" className="text-sm text-muted-foreground hover:text-primary transition-colors">
                  {t("footerPrivacy")}
                </a>
              </li>
            </ul>
          </div>
          
        </div>
        
        <div className="border-t border-border mt-16 pt-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <p className="text-xs text-muted-foreground">
            &copy; {new Date().getFullYear()} Loyalty Engine. All rights reserved.
          </p>
          <div className="flex gap-4">
            {/* Social placeholder icons if any */}
          </div>
        </div>
      </div>
    </footer>
  );
}
