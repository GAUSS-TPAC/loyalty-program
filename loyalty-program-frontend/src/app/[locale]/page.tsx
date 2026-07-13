"use client";

import { useTranslations } from "next-intl";
import { LandingHeader } from "@/components/LandingHeader";
import { LandingFooter } from "@/components/LandingFooter";
import { Code2, Gift, Wallet, CheckCircle2, Settings, Users, Trophy } from "lucide-react";
import { Link } from "@/i18n/routing";

export default function LandingPage() {
  const t = useTranslations("Landing");

  return (
    <div className="min-h-screen flex flex-col bg-background selection:bg-primary/20 selection:text-primary relative">
      <LandingHeader />

      <main className="flex-grow">
        {/* Hero Section */}
        <section className="relative pt-32 pb-20 md:pt-48 md:pb-32 overflow-hidden">
          {/* Background decorations */}
          <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-[0.4]">
            <div className="absolute -top-64 -left-64 w-[800px] h-[800px] bg-secondary rounded-full blur-3xl mix-blend-multiply" />
            <div className="absolute -bottom-64 -right-64 w-[600px] h-[600px] bg-[#d7ccc8] rounded-full blur-3xl mix-blend-multiply opacity-50 animate-pulse" />
          </div>

          <div className="container relative z-10 mx-auto px-4 md:px-6">
            <div className="max-w-4xl mx-auto text-center space-y-8">
              <h1 className="text-4xl md:text-6xl font-bold tracking-tight text-foreground leading-tight text-balance">
                {t("heroTitle")}
              </h1>
              <p className="text-lg md:text-xl text-muted-foreground leading-relaxed max-w-3xl mx-auto text-balance">
                {t("heroDescription")}
              </p>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
                <a
                  href="#solutions"
                  className="w-full sm:w-auto px-8 py-4 text-base font-semibold text-primary-foreground bg-primary rounded-xl shadow-lg shadow-primary/20 hover:bg-primary/90 transition-all hover:-translate-y-1"
                >
                  {t("discoverSolutions")}
                </a>
              </div>
            </div>
          </div>
        </section>

        {/* Our Solutions Section */}
        <section id="solutions" className="py-24 bg-secondary/10 relative">
          <div className="container mx-auto px-4 md:px-6">
            <div className="text-center mb-16">
              <h2 className="text-3xl md:text-4xl font-bold tracking-tight text-foreground">
                {t("solutionsTitle")}
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* API Card */}
              <div className="group bg-card border border-border p-8 rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
                <div className="w-14 h-14 bg-primary/10 rounded-xl flex items-center justify-center mb-6 group-hover:bg-primary/20 transition-colors">
                  <Code2 className="w-7 h-7 text-primary" />
                </div>
                <h3 className="text-xl font-bold text-foreground mb-4">
                  {t("solutionApiTitle")}
                </h3>
                <p className="text-muted-foreground leading-relaxed text-balance">
                  {t("solutionApiDesc")}
                </p>
              </div>

              {/* Rewards Card */}
              <div className="group bg-card border border-border p-8 rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
                <div className="w-14 h-14 bg-primary/10 rounded-xl flex items-center justify-center mb-6 group-hover:bg-primary/20 transition-colors">
                  <Gift className="w-7 h-7 text-primary" />
                </div>
                <h3 className="text-xl font-bold text-foreground mb-4">
                  {t("solutionRewardsTitle")}
                </h3>
                <p className="text-muted-foreground leading-relaxed text-balance">
                  {t("solutionRewardsDesc")}
                </p>
              </div>

              {/* Wallet Card */}
              <div className="group bg-card border border-border p-8 rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-2">
                <div className="w-14 h-14 bg-primary/10 rounded-xl flex items-center justify-center mb-6 group-hover:bg-primary/20 transition-colors">
                  <Wallet className="w-7 h-7 text-primary" />
                </div>
                <h3 className="text-xl font-bold text-foreground mb-4">
                  {t("solutionWalletTitle")}
                </h3>
                <p className="text-muted-foreground leading-relaxed text-balance">
                  {t("solutionWalletDesc")}
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Feature: Wallet Section */}
        <section id="wallet" className="py-24">
          <div className="container mx-auto px-4 md:px-6">
            <div className="flex flex-col md:flex-row items-center gap-16">
              <div className="w-full md:w-1/2 space-y-6">
                <h2 className="text-3xl md:text-4xl font-bold tracking-tight text-foreground leading-tight">
                  {t("walletSectionTitle")}
                </h2>
                <p className="text-lg text-muted-foreground leading-relaxed">
                  {t("walletSectionDesc")}
                </p>
                <ul className="space-y-4 pt-4">
                  {[
                    t("walletFeature1"),
                    t("walletFeature2"),
                    t("walletFeature3"),
                    t("walletFeature4"),
                    t("walletFeature5"),
                  ].map((feature, idx) => (
                    <li key={idx} className="flex items-start gap-3">
                      <CheckCircle2 className="w-6 h-6 text-primary shrink-0" />
                      <span className="font-medium text-foreground">{feature}</span>
                    </li>
                  ))}
                </ul>
              </div>
              <div className="w-full md:w-1/2">
                {/* Decorative mock element */}
                <div className="relative w-full max-w-md mx-auto aspect-square rounded-full bg-gradient-to-tr from-secondary to-background border-4 border-white shadow-2xl flex items-center justify-center">
                   <div className="absolute inset-4 rounded-full border border-primary/20 border-dashed animate-[spin_20s_linear_infinite]" />
                   <Wallet className="w-32 h-32 text-primary opacity-80" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* 3 Steps Section */}
        <section id="rewards" className="py-24 bg-primary/5">
          <div className="container mx-auto px-4 md:px-6">
            <div className="text-center mb-16 max-w-3xl mx-auto">
              <h2 className="text-3xl md:text-4xl font-bold tracking-tight text-foreground leading-tight">
                {t("stepsTitle")}
              </h2>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <div className="bg-card p-8 rounded-3xl text-center space-y-6 shadow-sm border border-border/50 hover:border-primary/30 transition-colors">
                <div className="mx-auto w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center">
                  <Settings className="w-10 h-10 text-primary" />
                </div>
                <h3 className="text-2xl font-bold text-foreground">{t("step1Title")}</h3>
                <p className="text-muted-foreground">{t("step1Desc")}</p>
              </div>
              <div className="bg-card p-8 rounded-3xl text-center space-y-6 shadow-sm border border-border/50 hover:border-primary/30 transition-colors">
                <div className="mx-auto w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center">
                  <Users className="w-10 h-10 text-primary" />
                </div>
                <h3 className="text-2xl font-bold text-foreground">{t("step2Title")}</h3>
                <p className="text-muted-foreground">{t("step2Desc")}</p>
              </div>
              <div className="bg-card p-8 rounded-3xl text-center space-y-6 shadow-sm border border-border/50 hover:border-primary/30 transition-colors">
                <div className="mx-auto w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center">
                  <Trophy className="w-10 h-10 text-primary" />
                </div>
                <h3 className="text-2xl font-bold text-foreground">{t("step3Title")}</h3>
                <p className="text-muted-foreground">{t("step3Desc")}</p>
              </div>
            </div>
          </div>
        </section>

        {/* API Section */}
        <section id="api" className="py-24">
          <div className="container mx-auto px-4 md:px-6">
            <div className="flex flex-col-reverse md:flex-row items-center gap-16">
              <div className="w-full md:w-1/2">
                <div className="relative w-full aspect-[4/3] rounded-2xl bg-slate-900 shadow-2xl p-6 border border-slate-800 overflow-hidden group">
                  <div className="absolute top-0 left-0 w-full h-8 bg-slate-800 flex items-center px-4 gap-2">
                    <div className="w-3 h-3 rounded-full bg-rose-500" />
                    <div className="w-3 h-3 rounded-full bg-amber-500" />
                    <div className="w-3 h-3 rounded-full bg-emerald-500" />
                  </div>
                  <pre className="mt-8 text-sm text-emerald-400 font-mono">
                    <code>
                      {`POST /v1/rewards HTTP/1.1
Host: api.loyaltyengine.com
Authorization: Bearer sk_live_...

{
  "event": "purchase.completed",
  "customer_id": "cus_9x12ab",
  "amount": 1500,
  "currency": "FCFA"
}`}
                    </code>
                  </pre>
                  <div className="absolute bottom-6 right-6 opacity-0 group-hover:opacity-100 transition-opacity">
                     <Code2 className="w-8 h-8 text-slate-500" />
                  </div>
                </div>
              </div>
              <div className="w-full md:w-1/2 space-y-6">
                <h2 className="text-3xl md:text-4xl font-bold tracking-tight text-foreground leading-tight">
                  {t("apiSectionTitle")}
                </h2>
                <p className="text-lg text-muted-foreground leading-relaxed">
                  {t("apiSectionDesc")}
                </p>
                <p className="font-semibold text-foreground pt-2">
                  {t("apiSectionSub")}
                </p>
                <ul className="space-y-4">
                  {[
                    t("apiFeature1"),
                    t("apiFeature2"),
                    t("apiFeature3"),
                    t("apiFeature4"),
                  ].map((feature, idx) => (
                    <li key={idx} className="flex items-start gap-3">
                      <CheckCircle2 className="w-6 h-6 text-primary shrink-0" />
                      <span className="font-medium text-foreground">{feature}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-32 bg-background relative overflow-hidden">
          <div className="absolute inset-0 bg-primary/5" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-4xl aspect-video bg-primary/10 rounded-full blur-[120px] pointer-events-none" />
          
          <div className="container relative z-10 mx-auto px-4 text-center space-y-8 max-w-3xl">
            <h2 className="text-4xl md:text-5xl font-bold text-foreground">
              {t("readyTitle")}
            </h2>
            <p className="text-lg md:text-xl text-muted-foreground text-balance">
              {t("readyDesc")}
            </p>
            <div className="pt-8">
              <Link
                href="/login"
                className="inline-block px-10 py-4 text-lg font-bold text-primary-foreground bg-primary rounded-xl shadow-xl shadow-primary/25 hover:bg-primary/90 hover:scale-105 active:scale-95 transition-all"
              >
                {t("openMyAccount")}
              </Link>
            </div>
          </div>
        </section>
      </main>

      <LandingFooter />
    </div>
  );
}
