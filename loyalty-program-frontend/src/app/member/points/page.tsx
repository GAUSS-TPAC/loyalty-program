"use client";

import { useState } from "react";
import { Star, Gift, ArrowUp, ArrowDown, ShoppingBag, Check } from "lucide-react";
import { toast } from "sonner";

const AVAILABLE_POINTS = 12450;
const LIFETIME_EARNED = 24600;
const LIFETIME_SPENT = 12150;

const HISTORY = [
    { id: "p1", date: "2026-05-30", label: "Achat supermarché",          amount: +450,   positive: true  },
    { id: "p2", date: "2026-05-18", label: "Échange bon d'achat",        amount: -1500,  positive: false },
    { id: "p3", date: "2026-05-10", label: "Bonus parrainage Sarah T.",  amount: +500,   positive: true  },
    { id: "p4", date: "2026-04-30", label: "Geste commercial annivers.", amount: +1000,  positive: true  },
    { id: "p5", date: "2026-04-15", label: "Achat boutique en ligne",    amount: +320,   positive: true  },
];

const CATALOG = [
    { id: "c1", name: "Café Offert",              category: "Boisson",   cost: 500,   available: true,  emoji: "☕" },
    { id: "c2", name: "Bon d'achat 1 000 XAF",   category: "Shopping",  cost: 1500,  available: true,  emoji: "🛍️" },
    { id: "c3", name: "Réduction 20%",            category: "Mode",      cost: 2500,  available: true,  emoji: "👗" },
    { id: "c4", name: "Bon d'achat 5 000 XAF",   category: "Shopping",  cost: 7500,  available: true,  emoji: "🎁" },
    { id: "c5", name: "Pass Cinéma",              category: "Loisir",    cost: 3000,  available: true,  emoji: "🎬" },
    { id: "c6", name: "Accès Lounge VIP",         category: "Premium",   cost: 15000, available: false, emoji: "✈️" },
];

const MY_GRANTS = [
    { id: "g1", name: "Café Offert — Station Total",   status: "Disponible", grantedDate: "22 mai 2026" },
    { id: "g2", name: "Bon d'achat Boulangerie",       status: "Utilisé",    grantedDate: "18 mai 2026" },
    { id: "g3", name: "Réduction 10% Boutique Mode",  status: "Expiré",     grantedDate: "15 déc 2025" },
];

export default function PointsPage() {
    const [activeTab, setActiveTab] = useState<"history" | "catalog" | "grants">("history");
    const [redeemingId, setRedeemingId] = useState<string | null>(null);

    const handleRedeem = (item: typeof CATALOG[0]) => {
        if (!item.available) return;
        if (AVAILABLE_POINTS < item.cost) {
            toast.error(`Points insuffisants. Il vous faut ${(item.cost - AVAILABLE_POINTS).toLocaleString()} pts de plus.`);
            return;
        }
        setRedeemingId(item.id);
        setTimeout(() => {
            setRedeemingId(null);
            toast.success(`"${item.name}" ajouté à vos récompenses !`);
        }, 1200);
    };

    const grantStatusStyle = (status: string) => {
        switch (status) {
            case "Disponible": return "bg-emerald-50 text-emerald-700 border-emerald-200";
            case "Utilisé":    return "bg-slate-100 text-slate-500 border-slate-200";
            default:           return "bg-red-50 text-red-500 border-red-100";
        }
    };

    return (
        <div className="px-4 py-5 space-y-5 max-w-lg mx-auto">

            {/* Balance card */}
            <div className="bg-gradient-to-br from-amber-500 to-orange-600 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
                <div className="absolute -bottom-6 -right-6 w-24 h-24 bg-white/10 rounded-full" />
                <div className="relative">
                    <div className="flex items-center gap-2 mb-2">
                        <Star size={15} className="text-white/70" />
                        <span className="text-xs font-bold text-white/70 uppercase tracking-wider">Mes Points</span>
                    </div>
                    <p className="text-4xl font-black">{AVAILABLE_POINTS.toLocaleString()}</p>
                    <p className="text-sm font-semibold text-white/70 mt-0.5">points disponibles</p>
                    <div className="flex gap-6 mt-4 pt-4 border-t border-white/20">
                        <div>
                            <p className="text-[10px] text-white/60 font-bold uppercase">Gagnés total</p>
                            <p className="text-base font-extrabold">{LIFETIME_EARNED.toLocaleString()}</p>
                        </div>
                        <div>
                            <p className="text-[10px] text-white/60 font-bold uppercase">Dépensés total</p>
                            <p className="text-base font-extrabold">{LIFETIME_SPENT.toLocaleString()}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Tabs */}
            <div className="flex bg-slate-100 rounded-xl p-1 gap-1">
                {([
                    { id: "history", label: "Historique" },
                    { id: "catalog", label: "Catalogue" },
                    { id: "grants",  label: "Mes récompenses" },
                ] as const).map((tab) => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id)}
                        className={`flex-1 py-2 text-[11px] font-bold rounded-lg transition-all ${
                            activeTab === tab.id
                                ? "bg-white text-slateigo-700 shadow-sm text-indigo-700"
                                : "text-slate-500"
                        }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Tab: Historique */}
            {activeTab === "history" && (
                <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm divide-y divide-slate-100">
                    {HISTORY.map((tx) => (
                        <div key={tx.id} className="flex items-center justify-between px-4 py-3.5">
                            <div className="flex items-center gap-3">
                                <div className={`p-2 rounded-xl ${tx.positive ? "bg-amber-50 text-amber-600" : "bg-slate-100 text-slate-500"}`}>
                                    {tx.positive ? <ArrowUp size={14} /> : <ArrowDown size={14} />}
                                </div>
                                <div>
                                    <p className="text-xs font-semibold text-slate-800">{tx.label}</p>
                                    <p className="text-[10px] text-slate-400">{tx.date}</p>
                                </div>
                            </div>
                            <span className={`text-sm font-extrabold ${tx.positive ? "text-amber-600" : "text-slate-500"}`}>
                                {tx.amount > 0 ? "+" : ""}{tx.amount.toLocaleString()} pts
                            </span>
                        </div>
                    ))}
                </div>
            )}

            {/* Tab: Catalogue */}
            {activeTab === "catalog" && (
                <div className="grid grid-cols-1 gap-3">
                    {CATALOG.map((item) => {
                        const canAfford = AVAILABLE_POINTS >= item.cost;
                        const isRedeeming = redeemingId === item.id;
                        return (
                            <div
                                key={item.id}
                                className={`bg-white border rounded-2xl p-4 flex items-center justify-between shadow-sm transition-all ${
                                    item.available ? "border-slate-200" : "opacity-50 border-slate-100"
                                }`}
                            >
                                <div className="flex items-center gap-3">
                                    <div className="w-10 h-10 bg-amber-50 rounded-xl flex items-center justify-center text-xl">
                                        {item.emoji}
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-slate-800">{item.name}</p>
                                        <div className="flex items-center gap-2 mt-0.5">
                                            <span className="text-[10px] font-bold text-amber-600 bg-amber-50 px-1.5 py-0.5 rounded-md">
                                                {item.cost.toLocaleString()} pts
                                            </span>
                                            <span className="text-[10px] text-slate-400">{item.category}</span>
                                        </div>
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleRedeem(item)}
                                    disabled={!item.available || !canAfford || isRedeeming}
                                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-extrabold transition-all ${
                                        isRedeeming
                                            ? "bg-emerald-100 text-emerald-600"
                                            : item.available && canAfford
                                                ? "bg-indigo-600 text-white hover:bg-indigo-700"
                                                : "bg-slate-100 text-slate-400 cursor-not-allowed"
                                    }`}
                                >
                                    {isRedeeming ? <Check size={12} /> : <ShoppingBag size={12} />}
                                    {isRedeeming ? "OK" : canAfford ? "Échanger" : "Insuffisant"}
                                </button>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* Tab: Mes récompenses */}
            {activeTab === "grants" && (
                <div className="space-y-3">
                    {MY_GRANTS.map((g) => (
                        <div key={g.id} className="bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between shadow-sm">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-fuchsia-50 text-fuchsia-600 rounded-xl">
                                    <Gift size={16} />
                                </div>
                                <div>
                                    <p className="text-xs font-bold text-slate-800">{g.name}</p>
                                    <p className="text-[10px] text-slate-400 mt-0.5">Obtenu le {g.grantedDate}</p>
                                </div>
                            </div>
                            <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold border ${grantStatusStyle(g.status)}`}>
                                {g.status}
                            </span>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
