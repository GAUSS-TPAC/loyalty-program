"use client";

import { useState } from "react";
import {
    UserPlus, Copy, Share2, CheckCircle2, Clock,
    XCircle, Gift, Users, TrendingUp
} from "lucide-react";
import { toast } from "sonner";

const REFERRAL_CODE = "MARIE998";
const REFERRAL_LINK = `https://app.yowyob.cm/join?ref=${REFERRAL_CODE}`;

const PROGRAM = {
    referrerReward: "500 pts",
    refereeReward:  "200 pts",
    condition:      "1er achat ≥ 5 000 XAF",
};

const REFERRALS = [
    { id: "r1", name: "Alice Koffi",  joined: "10 avr. 2025", status: "Converti",   reward: "500 pts" },
    { id: "r2", name: "Marc Kamga",   joined: "15 août 2025", status: "En attente", reward: "—"       },
    { id: "r3", name: "Sarah Touré",  joined: "20 jan. 2026", status: "Converti",   reward: "500 pts" },
];

const STATS = {
    total:     3,
    converted: 2,
    pending:   1,
    earned:    1000,
};

const statusConfig = {
    "Converti":    { icon: CheckCircle2, color: "text-emerald-600", bg: "bg-emerald-50",  border: "border-emerald-200" },
    "En attente":  { icon: Clock,        color: "text-amber-600",   bg: "bg-amber-50",    border: "border-amber-200"   },
    "Inactif":     { icon: XCircle,      color: "text-slate-400",   bg: "bg-slate-100",   border: "border-slate-200"   },
} as const;

type ReferralStatus = keyof typeof statusConfig;

export default function ReferralPage() {
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(REFERRAL_LINK).then(() => {
            setCopied(true);
            toast.success("Lien copié !");
            setTimeout(() => setCopied(false), 2000);
        });
    };

    const handleShare = () => {
        const text = `Rejoins le programme de fidélité Yowyob et gagne ${PROGRAM.refereeReward} dès ton premier achat ! Mon code : ${REFERRAL_CODE}\n${REFERRAL_LINK}`;
        if (navigator.share) {
            navigator.share({ title: "Yowyob Loyalty", text, url: REFERRAL_LINK });
        } else {
            navigator.clipboard.writeText(text);
            toast.success("Message copié pour partage !");
        }
    };

    return (
        <div className="px-4 py-5 space-y-5 max-w-lg mx-auto">

            {/* Hero */}
            <div className="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-2xl p-5 text-white shadow-lg relative overflow-hidden">
                <div className="absolute -top-4 -right-4 w-24 h-24 bg-white/10 rounded-full" />
                <div className="absolute -bottom-8 left-8 w-16 h-16 bg-white/5 rounded-full" />
                <div className="relative">
                    <div className="flex items-center gap-2 mb-2">
                        <UserPlus size={15} className="text-white/70" />
                        <span className="text-xs font-bold text-white/70 uppercase tracking-wider">Mon Parrainage</span>
                    </div>
                    <p className="text-sm font-semibold text-white/80 mb-1">
                        Tu parraines → <strong className="text-white">{PROGRAM.referrerReward}</strong> pour toi
                    </p>
                    <p className="text-sm font-semibold text-white/80">
                        Ton filleul → <strong className="text-white">{PROGRAM.refereeReward}</strong> à son 1er achat
                    </p>
                    <p className="text-[10px] text-white/50 font-semibold mt-1">Condition : {PROGRAM.condition}</p>
                </div>
            </div>

            {/* Code & lien */}
            <div className="bg-white border border-indigo-100 rounded-2xl p-4 shadow-sm space-y-3">
                <h3 className="text-xs font-extrabold text-slate-800 uppercase tracking-wide">Mon lien de parrainage</h3>

                {/* Code */}
                <div className="flex items-center justify-between bg-indigo-50 border border-indigo-200 rounded-xl px-4 py-3">
                    <div>
                        <p className="text-[10px] text-indigo-500 font-bold uppercase tracking-wider">Code</p>
                        <p className="text-xl font-black text-indigo-700 tracking-widest">{REFERRAL_CODE}</p>
                    </div>
                    <button
                        onClick={handleCopy}
                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-extrabold transition-all ${
                            copied
                                ? "bg-emerald-100 text-emerald-700"
                                : "bg-indigo-600 text-white hover:bg-indigo-700"
                        }`}
                    >
                        {copied ? <CheckCircle2 size={13} /> : <Copy size={13} />}
                        {copied ? "Copié !" : "Copier"}
                    </button>
                </div>

                {/* Link */}
                <div className="bg-slate-50 border border-slate-200 rounded-xl px-3 py-2">
                    <p className="text-[10px] text-slate-400 truncate font-mono">{REFERRAL_LINK}</p>
                </div>

                {/* Share buttons */}
                <div className="grid grid-cols-2 gap-2">
                    <button
                        onClick={handleCopy}
                        className="flex items-center justify-center gap-2 py-2.5 bg-white border border-slate-200 text-slate-700 rounded-xl text-xs font-bold hover:bg-slate-50 transition-colors"
                    >
                        <Copy size={14} /> Copier lien
                    </button>
                    <button
                        onClick={handleShare}
                        className="flex items-center justify-center gap-2 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold hover:bg-indigo-700 transition-colors"
                    >
                        <Share2 size={14} /> Partager
                    </button>
                </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 gap-3">
                <div className="bg-white border border-slate-200 rounded-2xl p-3.5 shadow-sm text-center">
                    <Users size={18} className="text-indigo-500 mx-auto mb-1" />
                    <p className="text-2xl font-black text-slate-900">{STATS.total}</p>
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Filleuls parrainés</p>
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl p-3.5 shadow-sm text-center">
                    <TrendingUp size={18} className="text-emerald-500 mx-auto mb-1" />
                    <p className="text-2xl font-black text-slate-900">{STATS.converted}</p>
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Convertis</p>
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl p-3.5 shadow-sm text-center">
                    <Clock size={18} className="text-amber-500 mx-auto mb-1" />
                    <p className="text-2xl font-black text-slate-900">{STATS.pending}</p>
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">En attente</p>
                </div>
                <div className="bg-white border border-slate-200 rounded-2xl p-3.5 shadow-sm text-center">
                    <Gift size={18} className="text-fuchsia-500 mx-auto mb-1" />
                    <p className="text-2xl font-black text-slate-900">{STATS.earned}</p>
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide">Pts gagnés</p>
                </div>
            </div>

            {/* Referral list */}
            <div>
                <h2 className="text-xs font-black text-slate-800 uppercase tracking-wider mb-3">Mes filleuls</h2>
                <div className="space-y-2.5">
                    {REFERRALS.map((ref) => {
                        const cfg = statusConfig[ref.status as ReferralStatus] ?? statusConfig["Inactif"];
                        const StatusIcon = cfg.icon;
                        return (
                            <div key={ref.id} className="bg-white border border-slate-200 rounded-2xl p-4 flex items-center justify-between shadow-sm">
                                <div className="flex items-center gap-3">
                                    <div className="w-9 h-9 bg-indigo-50 text-indigo-700 font-extrabold rounded-full flex items-center justify-center text-xs border border-indigo-100">
                                        {ref.name.split(" ").map(n => n[0]).join("")}
                                    </div>
                                    <div>
                                        <p className="text-xs font-bold text-slate-800">{ref.name}</p>
                                        <p className="text-[10px] text-slate-400 font-medium">Inscrit le {ref.joined}</p>
                                        {ref.reward !== "—" && (
                                            <p className="text-[10px] text-emerald-600 font-semibold mt-0.5">🎁 {ref.reward} gagnés</p>
                                        )}
                                    </div>
                                </div>
                                <span className={`flex items-center gap-1 px-2 py-1 rounded-full text-[10px] font-extrabold border ${cfg.bg} ${cfg.color} ${cfg.border}`}>
                                    <StatusIcon size={10} />
                                    {ref.status}
                                </span>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}
