"use client";

import { use, useState, useEffect } from "react";
import { Link } from "@/i18n/routing";
import {
  ArrowLeft,
  User,
  Activity,
  AlertTriangle,
  ShieldCheck,
  Coins,
  RefreshCcw,
  Copy,
  Check,
  Users,
  Award,
  TrendingUp,
  Clock
} from "lucide-react";
import { useTranslations } from "next-intl";
import {
  useMemberPoints,
  useMemberTier,
  useMemberPointsHistory
} from "@/hooks/useBackend";

interface Member {
  id: string;
  name: string;
  email: string;
  status: string;
  referrals: string[];
  referredBy?: string | null;
}

const DEFAULT_MEMBERS: Member[] = [
  { id: "usr_9921", name: "Amandine Dubois", email: "amandine@example.com", status: "ACTIVE", referrals: [] },
  { id: "usr_9922", name: "Jean Dupont", email: "jean.dupont@example.com", status: "PENDING_KYC", referrals: [] },
  { id: "usr_9923", name: "Alice Martin", email: "alice.martin@example.com", status: "ACTIVE", referrals: [] },
  { id: "usr_9924", name: "Paul Lambert", email: "paul.lambert@example.com", status: "FROZEN", referrals: [] }
];

export default function MemberDetailView({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const memberId = resolvedParams.id;
  const t = useTranslations("MemberDetail");

  // Backend Data Hooks
  const { data: pointsData, isLoading: pointsLoading, refetch: refetchPoints } = useMemberPoints(memberId);
  const { data: tierData, isLoading: tierLoading, refetch: refetchTier } = useMemberTier(memberId);
  const { data: history, isLoading: historyLoading, refetch: refetchHistory } = useMemberPointsHistory(memberId);

  // Identity state (simulated from a central member list like the dashboard)
  const [member, setMember] = useState<Member | null>(null);
  const [copied, setCopied] = useState(false);
  const [referralLink, setReferralLink] = useState("");

  useEffect(() => {
    // Search in default members first
    const found = DEFAULT_MEMBERS.find((m) => m.id === memberId);
    if (found) {
      setMember(found);
    } else {
      // Fallback for demo
      setMember({
        id: memberId,
        name: "Membre Inconnu",
        email: "unknown-member@loyalty.com",
        status: "ACTIVE",
        referrals: []
      });
    }

    if (typeof window !== "undefined") {
      const origin = window.location.origin;
      const pathSegments = window.location.pathname.split("/");
      const locale = pathSegments[1] || "fr";
      setReferralLink(`${origin}/${locale}?ref=${memberId}`);
    }
  }, [memberId]);

  const handleRefresh = () => {
    refetchPoints();
    refetchTier();
    refetchHistory();
  };

  const handleCopyLink = () => {
    navigator.clipboard.writeText(referralLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (!member) {
    return <div className="space-y-6 animate-pulse" />;
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header avec Navigation */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link
            href="/portal/members"
            className="p-2.5 text-muted-foreground hover:bg-secondary rounded-xl transition-all border border-border bg-card shadow-sm"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div className="space-y-1">
            <h1 className="text-3xl font-bold tracking-tight text-foreground flex items-center gap-2">
              <User className="w-8 h-8 text-primary/40" />
              Détails du Membre
            </h1>
            <p className="text-muted-foreground text-xs font-mono uppercase tracking-widest bg-secondary/50 px-2 py-0.5 rounded-md inline-block">
              UUID : {memberId}
            </p>
          </div>
        </div>

        <button
          onClick={handleRefresh}
          className="flex items-center gap-2 text-xs font-bold text-primary hover:text-primary/80 border border-primary/20 px-4 py-2 rounded-xl bg-primary/5 hover:bg-primary/10 transition-all active:scale-95"
        >
          <RefreshCcw className={`w-4 h-4 ${(pointsLoading || tierLoading || historyLoading) ? 'animate-spin' : ''}`} />
          Rafraîchir les données
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-8 mt-6">
        {/* Colonne GAUCHE: Profil & Tier */}
        <div className="md:col-span-4 space-y-8">

          {/* Identity Card */}
          <div className="border border-border bg-card rounded-2xl p-8 shadow-sm flex flex-col items-center text-center space-y-6 relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full -mr-16 -mt-16 transition-transform group-hover:scale-110" />

            <div className="w-24 h-24 rounded-full bg-secondary flex items-center justify-center border-4 border-background shadow-inner">
              <User className="w-12 h-12 text-primary" />
            </div>

            <div className="space-y-1.5 relative z-10">
              <h2 className="text-xl font-bold text-foreground">{member.name}</h2>
              <p className="text-sm text-muted-foreground italic font-sans">{member.email}</p>
            </div>

            <div className="w-full pt-6 border-t border-border flex flex-col items-center">
              <p className="text-[10px] uppercase font-bold text-muted-foreground tracking-widest mb-3">
                Solde de Points Fidélité
              </p>
              {pointsLoading ? (
                <div className="h-10 w-24 bg-muted animate-pulse rounded-lg" />
              ) : (
                <div className="flex items-baseline gap-2 text-primary">
                  <span className="text-5xl font-black font-mono tracking-tighter">
                    {pointsData?.totalPoints.toLocaleString() ?? "0"}
                  </span>
                  <span className="text-sm font-bold uppercase tracking-widest">CR</span>
                </div>
              )}
            </div>
          </div>

          {/* Tier Status Card */}
          <div className="border border-border bg-card rounded-2xl p-8 shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Programme Tier</h3>
              <Award className="w-5 h-5 text-primary" />
            </div>

            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-muted-foreground">Rang Actuel</span>
                {tierLoading ? (
                  <div className="h-6 w-20 bg-muted animate-pulse rounded" />
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-black uppercase tracking-widest bg-primary text-white shadow-sm ring-4 ring-primary/10">
                    {tierData?.tierLabel ?? "Bronze"}
                  </span>
                )}
              </div>

              {/* Progress Bar Tier */}
              <div className="space-y-2.5">
                <div className="flex justify-between text-[10px] font-black uppercase tracking-widest">
                  <span className="text-muted-foreground">Progression</span>
                  <span className="text-primary font-mono">{pointsData?.progressPercent ?? 0}%</span>
                </div>
                <div className="h-3 bg-secondary rounded-full overflow-hidden border border-border shadow-inner">
                  <div
                    className="h-full bg-primary transition-all duration-1000 shadow-[0_0_20px_rgba(141,110,99,0.3)]"
                    style={{ width: `${pointsData?.progressPercent ?? 0}%` }}
                  />
                </div>
                <p className="text-[10px] text-center text-muted-foreground italic">
                  +{pointsData?.nextTierPoints ?? 0} points avant le rang supérieur
                </p>
              </div>
            </div>
          </div>

          {/* Referral Link Card */}
          <div className="border border-border bg-card rounded-2xl p-6 shadow-sm space-y-4">
            <div className="flex items-center gap-2 font-bold text-xs uppercase tracking-widest border-b border-border pb-3">
              <Users className="w-4 h-4 text-primary" /> Parrainage
            </div>

            <div className="relative flex items-center group">
              <input
                type="text"
                readOnly
                value={referralLink}
                className="w-full bg-secondary border border-border rounded-xl pl-4 pr-12 py-3 text-[10px] font-mono text-muted-foreground focus:outline-none select-all transition-colors group-hover:bg-muted"
              />
              <button
                onClick={handleCopyLink}
                className="absolute right-2 p-2 bg-card border border-border rounded-lg text-muted-foreground hover:text-primary hover:shadow-sm transition-all"
                title="Copier le lien"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </div>

        {/* Colonne DROITE: Historique & Actions */}
        <div className="md:col-span-8 space-y-8">

          {/* Dashboard Stats Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex items-center gap-5 group hover:border-primary/30 transition-colors">
              <div className="p-3 bg-emerald-50 rounded-2xl text-emerald-600 group-hover:scale-110 transition-transform">
                <TrendingUp className="w-6 h-6" />
              </div>
              <div>
                <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Points Totaux Gagnés</p>
                <p className="text-xl font-bold text-foreground font-mono">{pointsData?.totalPoints.toLocaleString() ?? "0"} <span className="text-xs">CR</span></p>
              </div>
            </div>
            <div className="bg-card border border-border rounded-2xl p-6 shadow-sm flex items-center gap-5 group hover:border-primary/30 transition-colors">
              <div className="p-3 bg-primary/5 rounded-2xl text-primary group-hover:scale-110 transition-transform">
                <Activity className="w-6 h-6" />
              </div>
              <div>
                <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Transactions Loyalité</p>
                <p className="text-xl font-bold text-foreground font-mono">{history?.length ?? 0} <span className="text-xs">ops</span></p>
              </div>
            </div>
          </div>

          {/* Transaction History (Réel) */}
          <div className="border border-border bg-card rounded-2xl shadow-sm overflow-hidden flex flex-col group">
            <div className="bg-secondary/50 px-8 py-5 border-b border-border flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-card rounded-lg shadow-sm border border-border">
                  <Clock className="w-5 h-5 text-primary" />
                </div>
                <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Historique des points</h3>
              </div>
              <span className="text-[10px] uppercase font-black text-muted-foreground tracking-tighter">Flux en temps réel</span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-[10px] text-muted-foreground uppercase bg-muted/30 border-b border-border font-black tracking-widest">
                  <tr>
                    <th className="px-8 py-5">Date UTC</th>
                    <th className="px-8 py-5">Transaction ID</th>
                    <th className="px-8 py-5">Source / Événement</th>
                    <th className="px-8 py-5 text-right">Points</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {historyLoading ? (
                    [1, 2, 3].map((i) => (
                      <tr key={i} className="animate-pulse">
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-32" /></td>
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-24" /></td>
                        <td className="px-8 py-5"><div className="h-4 bg-muted rounded w-40" /></td>
                        <td className="px-8 py-5 text-right"><div className="h-4 bg-muted rounded w-16 ml-auto" /></td>
                      </tr>
                    ))
                  ) : history && history.length > 0 ? (
                    history.map((tx, index) => (
                      <tr
                        key={tx.id}
                        className={`border-b border-border/40 hover:bg-muted/10 transition-colors group/row ${index % 2 === 0 ? "bg-background" : "bg-muted/5"
                          }`}
                      >
                        <td className="px-8 py-5 text-[10px] font-mono text-muted-foreground">
                          {new Date(tx.createdAt).toLocaleString()}
                        </td>
                        <td className="px-8 py-5 font-mono text-[10px] text-muted-foreground group-hover/row:text-primary transition-colors">
                          {tx.id.substring(0, 8)}…
                        </td>
                        <td className="px-8 py-5">
                          <span className="text-[10px] font-black uppercase tracking-widest bg-secondary text-primary px-2 py-1 rounded border border-border shadow-sm">
                            {tx.type}
                          </span>
                          <p className="text-[10px] text-muted-foreground mt-1.5 italic font-sans">{tx.description}</p>
                        </td>
                        <td className={`px-8 py-5 font-black font-mono text-right text-base ${tx.points > 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                          {tx.points > 0 ? '+' : ''}{tx.points}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={4} className="px-8 py-10 text-center text-xs italic text-muted-foreground">
                        Aucun historique de points trouvé pour ce membre.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            {history && history.length > 10 && (
              <div className="p-4 bg-muted/20 border-t border-border text-center">
                <button className="text-[10px] uppercase font-black text-primary hover:underline">Charger plus de transactions</button>
              </div>
            )}
          </div>

          {/* Security Alert / Note */}
          <div className="bg-secondary/30 border border-border rounded-2xl p-6 flex items-start gap-4 shadow-inner">
            <div className="p-2.5 bg-card rounded-xl border border-border">
              <AlertTriangle className="w-5 h-5 text-orange-500" />
            </div>
            <div className="space-y-1">
              <p className="text-xs font-bold uppercase tracking-widest text-foreground">Gestion d&apos;identité externe</p>
              <p className="text-[11px] text-muted-foreground leading-relaxed">
                Ce compte est synchronisé avec Yowyob Kernel Core. Les modifications d&apos;identité doivent être effectuées depuis le panel d&apos;administration central.
              </p>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
