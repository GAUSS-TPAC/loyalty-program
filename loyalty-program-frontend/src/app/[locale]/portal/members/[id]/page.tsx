"use client";

import { use, useState } from "react";
import { Link } from "@/i18n/routing";
import { ArrowLeft, User, Activity, AlertTriangle, ShieldCheck, Coins, RefreshCcw } from "lucide-react";
import { useTranslations } from "next-intl";

export default function MemberDetailView({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const memberId = resolvedParams.id;

  const [walletStatus, setWalletStatus] = useState("ACTIVE");
  const [balance, setBalance] = useState(5400);

  const t = useTranslations("MemberDetail");

  const transactions = [
    { id: "tx_901", type: "CREDIT", source: "LOYALTY_REWARD", amount: 500, date: "2026-06-03T10:14:22Z" },
    { id: "tx_902", type: "DEBIT", source: "PURCHASE", amount: 150, date: "2026-06-01T14:20:00Z" },
    { id: "tx_903", type: "CREDIT", source: "TOPUP_STRIPE", amount: 1000, date: "2026-05-28T09:15:30Z" },
  ];

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex items-center gap-4">
        <Link href="/portal/members" className="p-2 text-muted-foreground hover:bg-secondary rounded-lg transition-colors border border-transparent hover:border-border">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground text-sm font-mono">
            {t("memberId")} {memberId}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 mt-6">
        
        {/* Left Column: Status & Balance */}
        <div className="md:col-span-4 space-y-6">
          <div className="border border-border bg-card rounded-xl p-6 shadow-sm flex flex-col items-center justify-center text-center space-y-4">
            <div className="w-16 h-16 rounded-full bg-secondary flex items-center justify-center border border-border">
              <User className="w-8 h-8 text-primary" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-foreground">Amandine Dubois</h2>
              <p className="text-sm text-muted-foreground">amandine@example.com</p>
            </div>
            
            <div className="w-full h-px bg-border my-2" />
            
            <div className="w-full">
              <p className="text-xs uppercase tracking-wider text-muted-foreground font-semibold mb-1">{t("currentBalance")}</p>
              <p className="text-4xl font-bold text-primary font-mono">{balance.toLocaleString()} <span className="text-xl">CR</span></p>
            </div>
          </div>

          <div className="border border-border bg-card rounded-xl p-6 shadow-sm space-y-6">
            <h3 className="font-medium text-foreground border-b border-border pb-3">{t("walletStatus")}</h3>
            
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">{t("currentState")}</span>
              {walletStatus === "ACTIVE" ? (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-green-100 text-green-700 border border-green-200">
                  <ShieldCheck className="w-4 h-4" /> Active
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-sm font-medium bg-destructive/10 text-destructive border border-destructive/20">
                  <AlertTriangle className="w-4 h-4" /> Frozen
                </span>
              )}
            </div>

            <div className="pt-2">
              {walletStatus === "ACTIVE" ? (
                <button 
                  onClick={() => setWalletStatus("FROZEN")}
                  className="w-full flex items-center justify-center gap-2 bg-background border border-destructive text-destructive hover:bg-destructive hover:text-white px-4 py-2 rounded-lg text-sm font-medium transition-all"
                >
                  <AlertTriangle className="w-4 h-4" />
                  {t("freezeWallet")}
                </button>
              ) : (
                <button 
                  onClick={() => setWalletStatus("ACTIVE")}
                  className="w-full flex items-center justify-center gap-2 bg-background border border-green-600 text-green-600 hover:bg-green-600 hover:text-white px-4 py-2 rounded-lg text-sm font-medium transition-all"
                >
                  <ShieldCheck className="w-4 h-4" />
                  {t("unfreezeWallet")}
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Actions & History */}
        <div className="md:col-span-8 space-y-6">
          
          {/* Manual Adjustment */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
              <Coins className="w-5 h-5 text-primary" />
              <h3 className="font-medium text-foreground">{t("manualAdjustment")}</h3>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("operation")}</label>
                  <select className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm">
                    <option value="CREDIT">{t("creditWallet")}</option>
                    <option value="DEBIT">{t("debitWallet")}</option>
                  </select>
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("amount")}</label>
                  <input type="number" min="1" placeholder={t("amountPlaceholder")} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm" />
                </div>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">{t("reason")}</label>
                <input type="text" placeholder={t("reasonPlaceholder")} className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm" />
              </div>
              <div className="flex justify-end pt-2">
                <button className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm active:scale-95">
                  {t("executeAdjustment")}
                </button>
              </div>
            </div>
          </div>

          {/* Transaction History */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
              <Activity className="w-5 h-5 text-primary" />
              <h3 className="font-medium text-foreground">{t("transactionHistory")}</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-xs text-muted-foreground uppercase bg-muted/50 border-b border-border">
                  <tr>
                    <th className="px-6 py-3 font-semibold tracking-wider">{t("date")}</th>
                    <th className="px-6 py-3 font-semibold tracking-wider">{t("txnId")}</th>
                    <th className="px-6 py-3 font-semibold tracking-wider">{t("source")}</th>
                    <th className="px-6 py-3 font-semibold tracking-wider text-right">{t("amount")}</th>
                    <th className="px-6 py-3 font-semibold tracking-wider text-center">{t("action")}</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx, index) => (
                    <tr key={tx.id} className={`border-b border-border/50 hover:bg-secondary/50 transition-colors ${index % 2 === 0 ? 'bg-background' : 'bg-muted/10'}`}>
                      <td className="px-6 py-3 text-xs text-muted-foreground whitespace-nowrap">{tx.date}</td>
                      <td className="px-6 py-3 font-mono text-xs">{tx.id}</td>
                      <td className="px-6 py-3">
                        <span className="text-xs font-medium text-primary bg-primary/10 px-2 py-1 rounded-md">{tx.source}</span>
                      </td>
                      <td className={`px-6 py-3 font-mono font-medium text-right ${tx.type === 'CREDIT' ? 'text-green-600' : 'text-foreground'}`}>
                        {tx.type === 'CREDIT' ? '+' : '-'}{tx.amount}
                      </td>
                      <td className="px-6 py-3 text-center">
                        <button className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-md transition-colors" title="Reverse Transaction">
                          <RefreshCcw className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
