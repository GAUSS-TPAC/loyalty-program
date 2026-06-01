"use client";

import { Award, Crown, Shield, Lock, CheckCircle2, TrendingUp, Star } from "lucide-react";

const MEMBER_TIER = "Gold";
const MEMBER_POINTS = 12450;
const MEMBER_TIER_REACHED = "15 mars 2026";
const PERIOD_POINTS = 1820;    // pts gagnés ce trimestre
const PERIOD_THRESHOLD = 2000; // pts requis pour maintien

const TIERS = [
    {
        name: "Bronze",
        threshold: 0,
        multiplier: "×1.0",
        color: "from-orange-400 to-amber-500",
        textColor: "text-orange-700",
        bg: "bg-orange-50",
        border: "border-orange-200",
        icon: Shield,
        perks: ["Points × 1.0 sur tous les achats", "Accès au catalogue standard"],
    },
    {
        name: "Silver",
        threshold: 1000,
        multiplier: "×1.5",
        color: "from-slate-400 to-slate-500",
        textColor: "text-slate-600",
        bg: "bg-slate-100",
        border: "border-slate-300",
        icon: Award,
        perks: ["Points × 1.5 sur tous les achats", "Accès récompenses Silver", "Notifications prioritaires"],
    },
    {
        name: "Gold",
        threshold: 5000,
        multiplier: "×2.0",
        color: "from-amber-400 to-yellow-500",
        textColor: "text-amber-700",
        bg: "bg-amber-50",
        border: "border-amber-200",
        icon: Crown,
        perks: ["Points × 2.0 sur tous les achats", "Accès récompenses Gold exclusives", "Support prioritaire", "Bonus anniversaire x3"],
    },
    {
        name: "Platinum",
        threshold: 20000,
        multiplier: "×3.0",
        color: "from-purple-500 to-indigo-600",
        textColor: "text-purple-700",
        bg: "bg-purple-50",
        border: "border-purple-200",
        icon: Crown,
        perks: ["Points × 3.0 sur tous les achats", "Récompenses VIP exclusives", "Accès lounge aéroport", "Conseiller dédié", "Bonus anniversaire x5"],
    },
];

const currentIndex = TIERS.findIndex((t) => t.name === MEMBER_TIER);
const currentTier = TIERS[currentIndex];
const nextTier = TIERS[currentIndex + 1];
const progressPct = nextTier
    ? Math.min(100, Math.round(((MEMBER_POINTS - currentTier.threshold) / (nextTier.threshold - currentTier.threshold)) * 100))
    : 100;
const maintainPct = Math.min(100, Math.round((PERIOD_POINTS / PERIOD_THRESHOLD) * 100));

export default function TierPage() {
    const TierIcon = currentTier.icon;

    return (
        <div className="px-4 py-5 space-y-5 max-w-lg mx-auto">

            {/* Current tier hero */}
            <div className={`rounded-2xl p-5 bg-gradient-to-br ${currentTier.color} text-white shadow-lg relative overflow-hidden`}>
                <div className="absolute -top-5 -right-5 w-24 h-24 bg-white/10 rounded-full" />
                <div className="relative">
                    <div className="flex items-center gap-3 mb-3">
                        <div className="p-2.5 bg-white/20 rounded-xl">
                            <TierIcon size={22} />
                        </div>
                        <div>
                            <p className="text-xs font-bold text-white/70 uppercase tracking-wider">Palier actuel</p>
                            <p className="text-2xl font-black">{MEMBER_TIER}</p>
                        </div>
                    </div>
                    <p className="text-xs text-white/70 font-semibold mb-3">
                        Atteint le {MEMBER_TIER_REACHED} · Multiplicateur {currentTier.multiplier}
                    </p>
                    {nextTier && (
                        <div className="space-y-1.5">
                            <div className="flex justify-between text-[11px] font-bold">
                                <span className="text-white/80">Progression vers {nextTier.name}</span>
                                <span>{MEMBER_POINTS.toLocaleString()} / {nextTier.threshold.toLocaleString()} pts</span>
                            </div>
                            <div className="h-2 bg-white/20 rounded-full overflow-hidden">
                                <div className="h-full bg-white rounded-full" style={{ width: `${progressPct}%` }} />
                            </div>
                            <p className="text-[10px] text-white/60 font-semibold">
                                Encore {(nextTier.threshold - MEMBER_POINTS).toLocaleString()} pts pour atteindre {nextTier.name}
                            </p>
                        </div>
                    )}
                </div>
            </div>

            {/* Maintien du palier */}
            <div className="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
                <div className="flex items-center gap-2 mb-3">
                    <TrendingUp size={15} className="text-indigo-600" />
                    <h3 className="text-xs font-extrabold text-slate-800 uppercase tracking-wide">Maintien du palier</h3>
                </div>
                <p className="text-[11px] text-slate-500 mb-3">
                    Gagnez <strong className="text-slate-700">{PERIOD_THRESHOLD.toLocaleString()} pts</strong> ce trimestre pour rester Gold.
                </p>
                <div className="space-y-1.5">
                    <div className="flex justify-between text-[11px] font-bold text-slate-600">
                        <span>{PERIOD_POINTS.toLocaleString()} pts ce trimestre</span>
                        <span className={maintainPct >= 100 ? "text-emerald-600" : "text-amber-600"}>
                            {maintainPct}%
                        </span>
                    </div>
                    <div className="h-2.5 bg-slate-100 rounded-full overflow-hidden">
                        <div
                            className={`h-full rounded-full transition-all ${maintainPct >= 100 ? "bg-emerald-500" : "bg-amber-500"}`}
                            style={{ width: `${maintainPct}%` }}
                        />
                    </div>
                    {maintainPct < 100 && (
                        <p className="text-[10px] text-amber-600 font-semibold">
                            Il vous manque {(PERIOD_THRESHOLD - PERIOD_POINTS).toLocaleString()} pts pour ce trimestre
                        </p>
                    )}
                </div>
            </div>

            {/* All tiers */}
            <div>
                <h2 className="text-xs font-black text-slate-800 uppercase tracking-wider mb-3">
                    Tous les paliers
                </h2>
                <div className="space-y-3">
                    {TIERS.map((tier, idx) => {
                        const isCurrent = tier.name === MEMBER_TIER;
                        const isUnlocked = idx <= currentIndex;
                        const TIcon = tier.icon;

                        return (
                            <div
                                key={tier.name}
                                className={`rounded-2xl border p-4 transition-all ${
                                    isCurrent
                                        ? `${tier.bg} ${tier.border} shadow-md`
                                        : isUnlocked
                                            ? "bg-white border-slate-200"
                                            : "bg-slate-50 border-slate-100 opacity-60"
                                }`}
                            >
                                <div className="flex items-center justify-between mb-2">
                                    <div className="flex items-center gap-2.5">
                                        <div className={`p-2 rounded-lg ${tier.bg} ${tier.border} border`}>
                                            <TIcon size={16} className={tier.textColor} />
                                        </div>
                                        <div>
                                            <p className={`text-sm font-extrabold ${isCurrent ? tier.textColor : "text-slate-700"}`}>
                                                {tier.name}
                                                {isCurrent && (
                                                    <span className={`ml-2 text-[9px] font-black uppercase px-1.5 py-0.5 rounded-full ${tier.bg} ${tier.textColor}`}>
                                                        Actuel
                                                    </span>
                                                )}
                                            </p>
                                            <p className="text-[10px] text-slate-400 font-semibold">
                                                À partir de {tier.threshold.toLocaleString()} pts · {tier.multiplier} points
                                            </p>
                                        </div>
                                    </div>
                                    {isUnlocked
                                        ? <CheckCircle2 size={18} className="text-emerald-500 shrink-0" />
                                        : <Lock size={16} className="text-slate-300 shrink-0" />
                                    }
                                </div>
                                <ul className="space-y-1 mt-2">
                                    {tier.perks.map((perk) => (
                                        <li key={perk} className="flex items-center gap-1.5 text-[10px] font-semibold text-slate-600">
                                            <Star size={9} className={isUnlocked ? "text-amber-400" : "text-slate-300"} fill={isUnlocked ? "currentColor" : "none"} />
                                            {perk}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
