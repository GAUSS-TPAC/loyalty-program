"use client";

import { use, useState, useEffect } from "react";
import { Link } from "@/i18n/routing";
import { ArrowLeft, User, Activity, AlertTriangle, ShieldCheck, Coins, RefreshCcw, Copy, Check, Users } from "lucide-react";
import { useTranslations } from "next-intl";

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

export default function MemberDetailView({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const memberId = resolvedParams.id;

  const t = useTranslations("MemberDetail");

  // LocalStorage Database states
  const [members, setMembers] = useState<Member[]>([]);
  const [member, setMember] = useState<Member | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);

  // Referral Link copy status
  const [copied, setCopied] = useState(false);
  const [referralLink, setReferralLink] = useState("");

  // Adjustment form states
  const [operation, setOperation] = useState("CREDIT");
  const [adjAmount, setAdjAmount] = useState("");
  const [adjReason, setAdjReason] = useState("");

  // Simulated transaction history
  const [transactions, setTransactions] = useState([
    { id: "tx_901", type: "CREDIT", source: "LOYALTY_REWARD", amount: 500, date: "2026-06-03T10:14:22Z" },
    { id: "tx_902", type: "DEBIT", source: "PURCHASE", amount: 150, date: "2026-06-01T14:20:00Z" },
    { id: "tx_903", type: "CREDIT", source: "TOPUP_STRIPE", amount: 1000, date: "2026-05-28T09:15:30Z" },
  ]);

  useEffect(() => {
    // 1. Load database
    const saved = localStorage.getItem("loyalty_members");
    let loadedMembers: Member[] = [];
    if (saved) {
      try {
        loadedMembers = JSON.parse(saved);
      } catch (e) {
        loadedMembers = DEFAULT_MEMBERS;
      }
    } else {
      loadedMembers = DEFAULT_MEMBERS;
      localStorage.setItem("loyalty_members", JSON.stringify(DEFAULT_MEMBERS));
    }
    setMembers(loadedMembers);

    // 2. Find specific member
    const found = loadedMembers.find((m) => m.id === memberId);
    if (found) {
      setMember(found);
    }

    // 3. Build referral link
    if (typeof window !== "undefined") {
      const origin = window.location.origin;
      // Fetch locale prefix (defaulting to en)
      const pathSegments = window.location.pathname.split("/");
      const locale = pathSegments[1] || "en";
      setReferralLink(`${origin}/${locale}?ref=${memberId}`);
    }

    setIsLoaded(true);
  }, [memberId]);

  // Adjust Wallet Status (Freeze / Unfreeze)
  const toggleStatus = () => {
    if (!member) return;
    const nextStatus = member.status === "ACTIVE" ? "FROZEN" : "ACTIVE";
    const updatedMember = { ...member, status: nextStatus };
    const updatedMembers = members.map((m) => (m.id === memberId ? updatedMember : m));

    setMember(updatedMember);
    setMembers(updatedMembers);
    localStorage.setItem("loyalty_members", JSON.stringify(updatedMembers));
  };

  // Perform manual adjustment
  const handleAdjustment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!member || !adjAmount) return;

    const amountNum = parseInt(adjAmount);
    if (isNaN(amountNum) || amountNum <= 0) return;

    const nextBalance = operation === "CREDIT" ? member.balance + amountNum : Math.max(0, member.balance - amountNum);
    const updatedMember = { ...member, balance: nextBalance };
    const updatedMembers = members.map((m) => (m.id === memberId ? updatedMember : m));

    setMember(updatedMember);
    setMembers(updatedMembers);
    localStorage.setItem("loyalty_members", JSON.stringify(updatedMembers));

    // Append to transactions history
    const newTx = {
      id: `tx_${Math.floor(1000 + Math.random() * 9000)}`,
      type: operation,
      source: "MANUAL_ADJUSTMENT",
      amount: amountNum,
      date: new Date().toISOString(),
    };
    setTransactions([newTx, ...transactions]);

    // Reset inputs
    setAdjAmount("");
    setAdjReason("");
  };

  // Copy referral link handler
  const handleCopyLink = () => {
    navigator.clipboard.writeText(referralLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (!isLoaded || !member) {
    return <div className="space-y-6 animate-pulse" />;
  }

  // Get list of sponsored members
  const sponsoredMembers = members.filter((m) => m.referredBy === memberId);

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Back navigation */}
      <div className="flex items-center gap-4">
        <Link
          href="/portal/members"
          className="p-2 text-muted-foreground hover:bg-secondary rounded-lg transition-colors border border-transparent hover:border-border"
        >
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
        {/* Left Column: Status, Balance, and Referral Link */}
        <div className="md:col-span-4 space-y-6">
          
          {/* Identity & Balance card */}
          <div className="border border-border bg-card rounded-xl p-6 shadow-sm flex flex-col items-center justify-center text-center space-y-4">
            <div className="w-16 h-16 rounded-full bg-secondary flex items-center justify-center border border-border">
              <User className="w-8 h-8 text-primary" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-foreground">{member.name}</h2>
              <p className="text-sm text-muted-foreground">{member.email}</p>
            </div>

            <div className="w-full h-px bg-border my-2" />

            <div className="w-full">
              <p className="text-xs uppercase tracking-wider text-muted-foreground font-semibold mb-1">
                {t("currentBalance")}
              </p>
              <p className="text-4xl font-bold text-primary font-mono">
                {member.balance.toLocaleString()} <span className="text-xl">CR</span>
              </p>
            </div>
          </div>

          {/* Wallet Status card */}
          <div className="border border-border bg-card rounded-xl p-6 shadow-sm space-y-6">
            <h3 className="font-medium text-foreground border-b border-border pb-3">{t("walletStatus")}</h3>

            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">{t("currentState")}</span>
              {member.status === "ACTIVE" ? (
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
              <button
                onClick={toggleStatus}
                className={`w-full flex items-center justify-center gap-2 bg-background border px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                  member.status === "ACTIVE"
                    ? "border-destructive text-destructive hover:bg-destructive hover:text-white"
                    : "border-green-600 text-green-600 hover:bg-green-600 hover:text-white"
                }`}
              >
                <AlertTriangle className="w-4 h-4" />
                {member.status === "ACTIVE" ? t("freezeWallet") : t("unfreezeWallet")}
              </button>
            </div>
          </div>

          {/* Referral Link Card */}
          <div className="border border-border bg-card rounded-xl p-6 shadow-sm space-y-4">
            <h3 className="font-medium text-foreground border-b border-border pb-2">{t("referralLinkTitle")}</h3>
            <p className="text-xs text-muted-foreground">{t("referralLinkDesc")}</p>

            <div className="relative flex items-center">
              <input
                type="text"
                readOnly
                value={referralLink}
                className="w-full bg-muted border border-border rounded-lg pl-3 pr-10 py-2 text-xs font-mono text-muted-foreground focus:outline-none select-all"
              />
              <button
                onClick={handleCopyLink}
                className="absolute right-2 text-muted-foreground hover:text-primary transition-colors p-1"
                title="Copy referral link"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Actions, History & Referrals list */}
        <div className="md:col-span-8 space-y-6">
          
          {/* Manual Adjustment */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
              <Coins className="w-5 h-5 text-primary" />
              <h3 className="font-medium text-foreground">{t("manualAdjustment")}</h3>
            </div>
            <form onSubmit={handleAdjustment} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                    {t("operation")}
                  </label>
                  <select
                    value={operation}
                    onChange={(e) => setOperation(e.target.value)}
                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm focus:outline-none"
                  >
                    <option value="CREDIT">{t("creditWallet")}</option>
                    <option value="DEBIT">{t("debitWallet")}</option>
                  </select>
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                    {t("amount")}
                  </label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={adjAmount}
                    onChange={(e) => setAdjAmount(e.target.value)}
                    placeholder={t("amountPlaceholder")}
                    className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm focus:outline-none"
                  />
                </div>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                  {t("reason")}
                </label>
                <input
                  type="text"
                  required
                  value={adjReason}
                  onChange={(e) => setAdjReason(e.target.value)}
                  placeholder={t("reasonPlaceholder")}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm focus:outline-none"
                />
              </div>
              <div className="flex justify-end pt-2">
                <button
                  type="submit"
                  className="flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-sm active:scale-95"
                >
                  {t("executeAdjustment")}
                </button>
              </div>
            </form>
          </div>

          {/* Sponsored Members (Filleuls) list */}
          <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
              <Users className="w-5 h-5 text-primary" />
              <h3 className="font-medium text-foreground">{t("referralsListTitle")}</h3>
            </div>
            
            <div className="divide-y divide-border">
              {sponsoredMembers.length === 0 ? (
                <div className="p-6 text-sm text-center text-muted-foreground">
                  {t("noReferralsYet")}
                </div>
              ) : (
                sponsoredMembers.map((sm) => (
                  <div key={sm.id} className="p-4 flex justify-between items-center hover:bg-muted/10 transition-colors">
                    <div>
                      <div className="font-medium text-sm text-foreground">{sm.name}</div>
                      <div className="text-xs text-muted-foreground font-mono">{sm.id}</div>
                    </div>
                    <div className="text-right">
                      <span className="text-xs bg-emerald-50 text-emerald-700 px-2 py-0.5 border border-emerald-100 rounded-md font-semibold">
                        ACTIVE
                      </span>
                    </div>
                  </div>
                ))
              )}
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
                    <tr
                      key={tx.id}
                      className={`border-b border-border/50 hover:bg-secondary/50 transition-colors ${
                        index % 2 === 0 ? "bg-background" : "bg-muted/10"
                      }`}
                    >
                      <td className="px-6 py-3 text-xs text-muted-foreground whitespace-nowrap">{tx.date}</td>
                      <td className="px-6 py-3 font-mono text-xs">{tx.id}</td>
                      <td className="px-6 py-3">
                        <span className="text-xs font-medium text-primary bg-primary/10 px-2 py-1 rounded-md">
                          {tx.source}
                        </span>
                      </td>
                      <td
                        className={`px-6 py-3 font-mono font-medium text-right ${
                          tx.type === "CREDIT" ? "text-green-600" : "text-foreground"
                        }`}
                      >
                        {tx.type === "CREDIT" ? "+" : "-"}
                        {tx.amount}
                      </td>
                      <td className="px-6 py-3 text-center">
                        <button
                          className="p-1.5 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded-md transition-colors"
                          title="Reverse Transaction"
                        >
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
