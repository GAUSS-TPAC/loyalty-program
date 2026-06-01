"use client";

import Link from "next/link";
import {
    Star, Wallet, Gift, UserPlus, ArrowUp, ArrowDown,
    ChevronRight, Zap, Crown, Shield, Award
} from "lucide-react";

const MEMBER = {
    name: "Marie Ndjomo",
    avatar: "MN",
    tier: "Gold",
    points: 12450,
    nextTierPoints: 20000,
    walletBalance: 8750,
    currency: "YowCoins",
    referralsCount: 3,
};

const TIER_CONFIG: Record<string, { color: string; bg: string; border: string; icon: React.ElementType }> = {
    Bronze:   { color: "text-orange-700",  bg: "bg-orange-50",   border: "border-orange-200", icon: Shield },
    Silver:   { color: "text-slate-600",   bg: "bg-slate-100",   border: "border-slate-300",  icon: Award },
    Gold:     { color: "text-amber-700",   bg: "bg-amber-50",    border: "border-amber-200",  icon: Crown },
    Platinum: { color: "text-purple-700",  bg: "bg-purple-50",   border: "border-purple-200", icon: Crown },
};

const RECENT_ACTIVITY = [
    { id: 1, label: "Achat supermarché",         amount: "+450 pts",    positive: true,  date: "Aujourd'hui" },
    { id: 2, label: "Rechargement Orange Money",  amount: "+5 000 YC",   positive: true,  date: "Aujourd'hui" },
    { id: 3, label: "Échange bon d'achat",        amount: "-1 500 pts",  positive: false, date: "Il y a 5 j" },
    { id: 4, label: "Paiement Cinéma",            amount: "-1 200 YC",   positive: false, date: "Il y a 9 j" },
    { id: 5, label: "Bonus parrainage Sarah T.",  amount: "+500 pts",    positive: true,  date: "Il y a 14 j" },
];

const ACTIVE_REWARDS = [
    { id: "r1", name: "Café Offert — Station Total", expiresIn: "8 jours" },
];

export default function MemberHomePage() {
    const tierCfg = TIER_CONFIG[MEMBER.tier];
    const TierIcon = tierCfg.icon;
    const progressPct = Math.min(100, Math.round((MEMBER.points / MEMBER.nextTierPoints) * 100));

    return (
        <div className="px-4 py-5 space-y-5 max-w-lg mx-auto">

            {/* Hero card */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-purple-600 to-indigo-800 p-5 text-white shadow-lg">
                <div className="absolute -top-6 -right-6 w-28 h-28 bg-white/10 rounded-full" />
                <div className="absolute -bottom-8 -left-4 w-20 h-20 bg-white/5 rounded-full" />
                <div className="relative">
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-2.5">
                            <div className="w-10 h-10 rounded-full bg-white/20 border-2 border-white/40 flex items-center justify-center font-extrabold text-sm">
                                {MEMBER.avatar}
                            </div>
                            <div>
                                <p className="font-extrabold text-sm leading-tight">{MEMBER.name}</p>
                                <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-extrabold border mt-0.5 ${tierCfg.bg} ${tierCfg.color} ${tierCfg.border}`}>
                                    <TierIcon size={10} />
                                    {MEMBER.tier}
                                </span>
                            </div>
                        </div>
                        <div className="text-right">
                            <p className="text-[10px] font-bold text-white/60 uppercase tracking-wider">Wallet</p>
                            <p className="text-lg font-black">{MEMBER.walletBalance.toLocaleString()}</p>
                            <p className="text-[10px] font-bold text-white/70">{MEMBER.currency}</p>
                        </div>
                    </div>

                    <div className="space-y-1.5">
                        <div className="flex items-center justify-between text-[11px] font-bold">
                            <span className="text-white/80">Points disponibles</span>
                            <span>{MEMBER.points.toLocaleString()} / {MEMBER.nextTierPoints.toLocaleString()}</span>
                        </div>
                        <div className="h-2 bg-white/20 rounded-full overflow-hidden">
                            <div
                                className="h-full bg-white rounded-full transition-all"
                                style={{ width: `${progressPct}%` }}
                            />
                        </div>
                        <p className="text-[10px] text-white/60 font-semibold">
                            {(MEMBER.nextTierPoints - MEMBER.points).toLocaleString()} pts pour atteindre Platinum
                        </p>
                    </div>
                </div>
            </div>

            {/* Quick links */}
            <div className="grid grid-cols-4 gap-3">
                {[
                    { href: "/member/wallet",   icon: Wallet,   label: "Wallet",    color: "text-emerald-600 bg-emerald-50" },
                    { href: "/member/points",   icon: Star,     label: "Points",    color: "text-amber-600  bg-amber-50"   },
                    { href: "/member/tier",     icon: Crown,    label: "Palier",    color: "text-purple-600 bg-purple-50"  },
                    { href: "/member/referral", icon: UserPlus, label: "Parrain",   color: "text-indigo-600 bg-indigo-50"  },
                ].map((item) => (
                    <Link
                        key={item.href}
                        href={item.href}
                        className="flex flex-col items-center gap-1.5 p-3 bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow"
                    >
                        <div className={`p-2 rounded-xl ${item.color}`}>
                            <item.icon size={18} />
                        </div>
                        <span className="text-[10px] font-bold text-slate-600">{item.label}</span>
                    </Link>
                ))}
            </div>

            {/* Récompenses actives */}
            {ACTIVE_REWARDS.length > 0 && (
                <div>
                    <div className="flex items-center justify-between mb-2">
                        <h2 className="text-xs font-black text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
                            <Gift size={14} className="text-fuchsia-500" />
                            Récompenses disponibles
                        </h2>
                        <Link href="/member/points" className="text-[10px] font-bold text-indigo-600 flex items-center gap-0.5">
                            Voir tout <ChevronRight size={12} />
                        </Link>
                    </div>
                    <div className="space-y-2">
                        {ACTIVE_REWARDS.map((r) => (
                            <div key={r.id} className="bg-white border border-fuchsia-100 rounded-xl p-3.5 flex items-center justify-between shadow-sm">
                                <div className="flex items-center gap-2.5">
                                    <div className="p-2 bg-fuchsia-50 text-fuchsia-600 rounded-lg">
                                        <Gift size={16} />
                                    </div>
                                    <div>
                                        <p className="text-xs font-bold text-slate-800">{r.name}</p>
                                        <p className="text-[10px] text-amber-600 font-semibold">Expire dans {r.expiresIn}</p>
                                    </div>
                                </div>
                                <button className="px-3 py-1.5 bg-fuchsia-600 text-white text-[10px] font-extrabold rounded-lg hover:bg-fuchsia-700 transition-colors">
                                    Utiliser
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Activité récente */}
            <div>
                <div className="flex items-center justify-between mb-2">
                    <h2 className="text-xs font-black text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
                        <Zap size={14} className="text-amber-500" />
                        Dernières activités
                    </h2>
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm divide-y divide-slate-100">
                    {RECENT_ACTIVITY.map((item) => (
                        <div key={item.id} className="flex items-center justify-between px-4 py-3 hover:bg-slate-50 transition-colors">
                            <div className="flex items-center gap-3">
                                <div className={`p-1.5 rounded-lg ${item.positive ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-500"}`}>
                                    {item.positive ? <ArrowUp size={14} /> : <ArrowDown size={14} />}
                                </div>
                                <div>
                                    <p className="text-xs font-semibold text-slate-800">{item.label}</p>
                                    <p className="text-[10px] text-slate-400 font-medium">{item.date}</p>
                                </div>
                            </div>
                            <span className={`text-xs font-extrabold ${item.positive ? "text-emerald-600" : "text-rose-500"}`}>
                                {item.amount}
                            </span>
                        </div>
                    ))}
                </div>
            </div>

        </div>
    );
}
