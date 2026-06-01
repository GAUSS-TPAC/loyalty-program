"use client";

import { useState } from "react";
import {
    Wallet, ArrowUp, ArrowDown, Plus, Send,
    CreditCard, Smartphone, ChevronDown
} from "lucide-react";
import { toast } from "sonner";

const BALANCE = 8750;
const CURRENCY = "YowCoins";

const TRANSACTIONS = [
    { id: "t1", date: "2026-05-30", label: "Rechargement Orange Money",    amount: +5000,  type: "credit", provider: "Orange" },
    { id: "t2", date: "2026-05-24", label: "Paiement Cinéma Club",         amount: -1200,  type: "debit",  provider: null },
    { id: "t3", date: "2026-05-18", label: "Rechargement MTN MoMo",        amount: +10000, type: "credit", provider: "MTN" },
    { id: "t4", date: "2026-05-10", label: "Paiement Supermarché Score",   amount: -3500,  type: "debit",  provider: null },
    { id: "t5", date: "2026-04-30", label: "Cashback programme fidélité",  amount: +1200,  type: "credit", provider: "Loyalty" },
    { id: "t6", date: "2026-04-22", label: "Paiement Station Total",       amount: -500,   type: "debit",  provider: null },
];

type TopUpMethod = "MTN" | "Orange" | "Stripe";

export default function WalletPage() {
    const [showTopUp, setShowTopUp] = useState(false);
    const [method, setMethod] = useState<TopUpMethod>("MTN");
    const [amount, setAmount] = useState("5000");
    const [filter, setFilter] = useState<"all" | "credit" | "debit">("all");

    const filtered = TRANSACTIONS.filter(t => filter === "all" || t.type === filter);

    const handleTopUp = (e: React.FormEvent) => {
        e.preventDefault();
        const val = Number(amount);
        if (!val || val < 500) { toast.error("Montant minimum : 500 XAF"); return; }
        toast.success(`Recharge ${val.toLocaleString()} XAF via ${method} initiée. Confirmez sur votre téléphone.`);
        setShowTopUp(false);
        setAmount("5000");
    };

    return (
        <div className="px-4 py-5 space-y-5 max-w-lg mx-auto">

            {/* Balance card */}
            <div className="rounded-2xl bg-gradient-to-br from-emerald-600 to-teal-700 p-5 text-white shadow-lg relative overflow-hidden">
                <div className="absolute -top-4 -right-4 w-24 h-24 bg-white/10 rounded-full" />
                <div className="relative">
                    <div className="flex items-center gap-2 mb-3">
                        <Wallet size={16} className="text-white/70" />
                        <span className="text-xs font-bold text-white/70 uppercase tracking-wider">Mon Wallet</span>
                    </div>
                    <p className="text-4xl font-black tracking-tight mb-0.5">
                        {BALANCE.toLocaleString()}
                    </p>
                    <p className="text-sm font-bold text-white/70">{CURRENCY} ≈ {BALANCE.toLocaleString()} XAF</p>
                </div>
            </div>

            {/* Actions */}
            <div className="grid grid-cols-2 gap-3">
                <button
                    onClick={() => setShowTopUp(true)}
                    className="flex items-center justify-center gap-2 py-3 bg-indigo-600 text-white rounded-xl font-bold text-sm hover:bg-indigo-700 transition-colors shadow-sm"
                >
                    <Plus size={16} />
                    Recharger
                </button>
                <button
                    onClick={() => toast.info("Fonctionnalité de retrait disponible bientôt")}
                    className="flex items-center justify-center gap-2 py-3 bg-white border border-slate-200 text-slate-700 rounded-xl font-bold text-sm hover:bg-slate-50 transition-colors shadow-sm"
                >
                    <Send size={16} />
                    Retirer
                </button>
            </div>

            {/* Top-up form */}
            {showTopUp && (
                <div className="bg-white border border-indigo-100 rounded-2xl p-4 shadow-md space-y-4">
                    <h3 className="text-sm font-extrabold text-slate-900 uppercase tracking-wide">
                        Recharger mon wallet
                    </h3>
                    <form onSubmit={handleTopUp} className="space-y-3">
                        <div>
                            <label className="text-[11px] font-bold text-slate-600 uppercase tracking-wide block mb-1.5">
                                Méthode de paiement
                            </label>
                            <div className="grid grid-cols-3 gap-2">
                                {(["MTN", "Orange", "Stripe"] as TopUpMethod[]).map((m) => (
                                    <button
                                        key={m}
                                        type="button"
                                        onClick={() => setMethod(m)}
                                        className={`py-2 rounded-xl text-xs font-extrabold border transition-all ${
                                            method === m
                                                ? "bg-indigo-600 text-white border-indigo-600"
                                                : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"
                                        }`}
                                    >
                                        {m === "MTN" ? "MTN MoMo" : m === "Orange" ? "Orange Money" : "Carte Stripe"}
                                    </button>
                                ))}
                            </div>
                        </div>
                        <div>
                            <label className="text-[11px] font-bold text-slate-600 uppercase tracking-wide block mb-1.5">
                                Montant (XAF)
                            </label>
                            <div className="relative">
                                {method === "Stripe" ? (
                                    <CreditCard size={14} className="absolute left-3 top-2.5 text-slate-400" />
                                ) : (
                                    <Smartphone size={14} className="absolute left-3 top-2.5 text-slate-400" />
                                )}
                                <input
                                    type="number"
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    min="500"
                                    className="w-full pl-8 pr-3 py-2 border border-slate-200 rounded-xl text-sm font-bold focus:outline-none focus:ring-2 focus:ring-indigo-500"
                                    required
                                />
                            </div>
                            <div className="flex gap-2 mt-2">
                                {[1000, 2000, 5000, 10000].map((v) => (
                                    <button
                                        key={v}
                                        type="button"
                                        onClick={() => setAmount(String(v))}
                                        className="flex-1 py-1 text-[10px] font-bold border border-slate-200 rounded-lg hover:bg-indigo-50 hover:border-indigo-200 transition-colors"
                                    >
                                        {v.toLocaleString()}
                                    </button>
                                ))}
                            </div>
                        </div>
                        <div className="flex gap-2 pt-1">
                            <button
                                type="button"
                                onClick={() => setShowTopUp(false)}
                                className="flex-1 py-2.5 border border-slate-200 text-slate-600 rounded-xl text-xs font-bold hover:bg-slate-50"
                            >
                                Annuler
                            </button>
                            <button
                                type="submit"
                                className="flex-1 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-extrabold hover:bg-indigo-700 transition-colors"
                            >
                                Confirmer
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Transactions */}
            <div>
                <div className="flex items-center justify-between mb-3">
                    <h2 className="text-xs font-black text-slate-800 uppercase tracking-wider">
                        Historique
                    </h2>
                    <div className="flex gap-1.5">
                        {(["all", "credit", "debit"] as const).map((f) => (
                            <button
                                key={f}
                                onClick={() => setFilter(f)}
                                className={`px-2.5 py-1 text-[10px] font-bold rounded-lg border transition-all ${
                                    filter === f
                                        ? "bg-indigo-600 text-white border-indigo-600"
                                        : "bg-white text-slate-500 border-slate-200"
                                }`}
                            >
                                {f === "all" ? "Tout" : f === "credit" ? "Crédits" : "Débits"}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm divide-y divide-slate-100">
                    {filtered.map((tx) => (
                        <div key={tx.id} className="flex items-center justify-between px-4 py-3.5">
                            <div className="flex items-center gap-3">
                                <div className={`p-2 rounded-xl ${tx.type === "credit" ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-500"}`}>
                                    {tx.type === "credit" ? <ArrowUp size={14} /> : <ArrowDown size={14} />}
                                </div>
                                <div>
                                    <p className="text-xs font-semibold text-slate-800">{tx.label}</p>
                                    <p className="text-[10px] text-slate-400 font-medium">{tx.date}</p>
                                </div>
                            </div>
                            <span className={`text-sm font-extrabold ${tx.amount > 0 ? "text-emerald-600" : "text-rose-500"}`}>
                                {tx.amount > 0 ? "+" : ""}{tx.amount.toLocaleString()} YC
                            </span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
