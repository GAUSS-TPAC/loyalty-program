"use client";

import React, { useState, useEffect, use } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import {
    ArrowLeft, User, Mail, Phone, Calendar, Shield, Activity, 
    Award, TrendingUp, Wallet, Copy, Check, Lock, Unlock, 
    Trash2, Plus, Minus, Info, Gift, Search, Share2, History, AlertTriangle
} from "lucide-react";
import { getMembers, saveMembers, Member, Transaction, RewardGrant, AdminHistory } from "@/lib/membersData";
import { toast } from "sonner";

interface PageProps {
    params: Promise<{ id: string }>;
}

export default function MemberDetailPage({ params }: PageProps) {
    const { id } = use(params);
    
    const [membersList, setMembersList] = useState<Member[]>([]);
    const [member, setMember] = useState<Member | null>(null);
    const [activeTab, setActiveTab] = useState<"overview" | "transactions" | "rewards" | "referrals" | "admin">("overview");

    // Copy Referral Link State
    const [copied, setCopied] = useState(false);

    // Administrative Form States
    const [freezeReason, setFreezeReason] = useState("");
    const [adjustType, setAdjustType] = useState<"Points" | "Wallet">("Points");
    const [adjustAction, setAdjustAction] = useState<"Add" | "Remove">("Add");
    const [adjustAmount, setAdjustAmount] = useState("");
    const [adjustJustification, setAdjustJustification] = useState("");
    
    const [closeConfirmed, setCloseConfirmed] = useState(false);
    const [closeReason, setCloseReason] = useState("");

    // Transactions filter state
    const [txSearch, setTxSearch] = useState("");
    const [txLedgerFilter, setTxLedgerFilter] = useState<"all" | "Points" | "Wallet">("all");
    const [txTypeFilter, setTxTypeFilter] = useState("all");

    // Load data from memory/localStorage
    useEffect(() => {
        const loadedMembers = getMembers();
        setMembersList(loadedMembers);
        const found = loadedMembers.find(m => m.id === id);
        if (found) {
            setMember(found);
        }
    }, [id]);

    if (!member) {
        return (
            <div className="space-y-6">
                <Link href="/dashboard/members">
                    <Button variant="outline" className="h-8 text-xs gap-1 cursor-pointer">
                        <ArrowLeft size={14} />
                        Retour au registre
                    </Button>
                </Link>
                <Card className="border-slate-200 shadow-2xs p-8 text-center">
                    <p className="text-sm font-semibold text-slate-500">Membre introuvable (ID: {id})</p>
                </Card>
            </div>
        );
    }

    // Helper: update single member state and save globally
    const updateMember = (updated: Member) => {
        setMember(updated);
        const updatedList = membersList.map(m => m.id === updated.id ? updated : m);
        setMembersList(updatedList);
        saveMembers(updatedList);
    };

    // Color helpers
    const getTierColor = (tier: string) => {
        switch (tier) {
            case "Platinum": return "bg-purple-100 text-purple-700 border-purple-200";
            case "Gold": return "bg-amber-100 text-amber-700 border-amber-200";
            case "Silver": return "bg-slate-100 text-slate-700 border-slate-200";
            default: return "bg-orange-100 text-orange-700 border-orange-200"; // Bronze
        }
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case "ACTIVE": return "bg-emerald-100 text-emerald-700 border-emerald-200";
            case "FROZEN": return "bg-amber-100 text-amber-700 border-amber-200";
            default: return "bg-rose-100 text-rose-700 border-rose-200"; // CLOSED
        }
    };

    // Copy referral link utility
    const handleCopyLink = () => {
        const link = `https://yowyob.com/join?ref=${member.referralCode}`;
        navigator.clipboard.writeText(link);
        setCopied(true);
        toast.success("Lien de parrainage copié !");
        setTimeout(() => setCopied(false), 2000);
    };

    // ----------------------------------------------------
    // Admin Handlers
    // ----------------------------------------------------
    const handleToggleWalletStatus = (e: React.FormEvent) => {
        e.preventDefault();
        if (!freezeReason.trim()) {
            toast.error("Veuillez saisir le motif administratif");
            return;
        }

        const currentStatus = member.walletStatus;
        let newStatus: "ACTIVE" | "FROZEN" = "ACTIVE";
        let actionLabel = "Dégel du Wallet";
        
        if (currentStatus === "ACTIVE") {
            newStatus = "FROZEN";
            actionLabel = "Gel du Wallet";
        }

        const newAdminHistory: AdminHistory = {
            id: `adh-${Date.now()}`,
            date: new Date().toISOString().split("T")[0],
            action: actionLabel,
            reason: freezeReason,
            admin: "Administrateur Principal"
        };

        const updated: Member = {
            ...member,
            walletStatus: newStatus,
            adminHistory: [newAdminHistory, ...member.adminHistory]
        };

        updateMember(updated);
        setFreezeReason("");
        toast.success(`Le wallet du membre a été mis à jour: ${newStatus}`);
    };

    const handleAdjustBalances = (e: React.FormEvent) => {
        e.preventDefault();
        const amt = Number(adjustAmount);
        if (!adjustAmount || isNaN(amt) || amt <= 0) {
            toast.error("Veuillez saisir un montant valide supérieur à 0");
            return;
        }
        if (!adjustJustification.trim()) {
            toast.error("Veuillez saisir une justification");
            return;
        }

        const multiplier = adjustAction === "Add" ? 1 : -1;
        const delta = amt * multiplier;

        let updatedPoints = member.points;
        let updatedWallet = member.walletBalance;

        let description = "";

        if (adjustType === "Points") {
            updatedPoints = Math.max(0, member.points + delta);
            description = `Ajustement Admin Points (${adjustAction === "Add" ? "+" : "-"}${amt})`;
        } else {
            updatedWallet = Math.max(0, member.walletBalance + delta);
            description = `Ajustement Admin Wallet (${adjustAction === "Add" ? "+" : "-"}${amt} XAF)`;
        }

        const newTransaction: Transaction = {
            id: `tx-${Date.now()}`,
            date: new Date().toISOString().split("T")[0],
            type: "Ajustement Admin",
            ledger: adjustType,
            amount: delta,
            description: `${description} - ${adjustJustification}`
        };

        const newAdminHistory: AdminHistory = {
            id: `adh-${Date.now()}`,
            date: new Date().toISOString().split("T")[0],
            action: `Ajustement ${adjustType} (${adjustAction === "Add" ? "+" : "-"}${amt})`,
            reason: adjustJustification,
            admin: "Administrateur Principal"
        };

        const updated: Member = {
            ...member,
            points: updatedPoints,
            walletBalance: updatedWallet,
            transactions: [newTransaction, ...member.transactions],
            adminHistory: [newAdminHistory, ...member.adminHistory]
        };

        updateMember(updated);
        setAdjustAmount("");
        setAdjustJustification("");
        toast.success("Ajustement de solde appliqué !");
    };

    const handleCloseAccount = (e: React.FormEvent) => {
        e.preventDefault();
        if (!closeConfirmed) {
            toast.error("Veuillez confirmer la clôture en cochant la case.");
            return;
        }
        if (!closeReason.trim()) {
            toast.error("Veuillez fournir un motif de clôture.");
            return;
        }

        const newAdminHistory: AdminHistory = {
            id: `adh-${Date.now()}`,
            date: new Date().toISOString().split("T")[0],
            action: "Clôture Définitive du Compte",
            reason: closeReason,
            admin: "Administrateur Principal"
        };

        // Empty wallet balance upon closure
        const newTransaction: Transaction | null = member.walletBalance > 0 ? {
            id: `tx-${Date.now()}`,
            date: new Date().toISOString().split("T")[0],
            type: "Débit Wallet",
            ledger: "Wallet",
            amount: -member.walletBalance,
            description: "Solde vidé pour clôture administrative du compte"
        } : null;

        const updated: Member = {
            ...member,
            walletStatus: "CLOSED",
            walletBalance: 0,
            transactions: newTransaction ? [newTransaction, ...member.transactions] : member.transactions,
            adminHistory: [newAdminHistory, ...member.adminHistory]
        };

        updateMember(updated);
        setCloseReason("");
        setCloseConfirmed(false);
        toast.success("Compte client clôturé définitivement.");
    };

    // ----------------------------------------------------
    // Filtering Transactions
    // ----------------------------------------------------
    const filteredTx = member.transactions.filter(tx => {
        const query = txSearch.toLowerCase().trim();
        if (query && !tx.description.toLowerCase().includes(query) && !tx.type.toLowerCase().includes(query)) {
            return false;
        }
        if (txLedgerFilter !== "all" && tx.ledger !== txLedgerFilter) {
            return false;
        }
        if (txTypeFilter !== "all" && tx.type !== txTypeFilter) {
            return false;
        }
        return true;
    });

    // Tier Progress calculation
    const pointsProgress = member.points;
    const progressPercent = Math.min(100, Math.floor((pointsProgress / member.nextTierPoints) * 100));
    const pointsNeeded = Math.max(0, member.nextTierPoints - pointsProgress);

    return (
        <div className="space-y-6 pb-12">
            
            {/* Navigation back */}
            <div className="flex items-center justify-between">
                <Link href="/dashboard/members">
                    <Button variant="outline" className="h-8 text-xs gap-1 cursor-pointer">
                        <ArrowLeft size={14} />
                        Retour au registre
                    </Button>
                </Link>
                <span className="text-xs text-slate-400 font-bold">Fiche Membre &bull; ID: {member.id}</span>
            </div>

            {/* Profile Overview Card Header */}
            <div className="p-6 bg-white border border-slate-200/80 rounded-2xl shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-6">
                <div className="flex items-start md:items-center gap-4">
                    <div className="w-14 h-14 bg-indigo-50 text-indigo-700 font-black rounded-full flex items-center justify-center text-lg border border-indigo-100 shadow-2xs">
                        {member.avatar}
                    </div>
                    <div>
                        <div className="flex flex-wrap items-center gap-2">
                            <h1 className="text-lg font-black text-slate-850 tracking-tight">{member.name}</h1>
                            <Badge className={`text-[10px] font-bold border ${getTierColor(member.tier)} shadow-3xs`}>
                                Palier {member.tier}
                            </Badge>
                            <Badge className={`text-[10px] font-bold border ${getStatusColor(member.walletStatus)} shadow-3xs`}>
                                Wallet {member.walletStatus}
                            </Badge>
                        </div>
                        <p className="text-xs text-slate-500 font-semibold mt-1 flex flex-col sm:flex-row sm:items-center gap-2">
                            <span>{member.email}</span>
                            <span className="hidden sm:inline text-slate-300">|</span>
                            <span>{member.phone}</span>
                            <span className="hidden sm:inline text-slate-300">|</span>
                            <span className="font-mono text-indigo-600 bg-indigo-50 border border-indigo-100/50 rounded-md px-1 py-0.5">{member.externalId}</span>
                        </p>
                    </div>
                </div>
                <div className="flex items-center gap-4 text-xs font-bold border-t md:border-t-0 border-slate-100 pt-4 md:pt-0">
                    <div className="text-left md:text-right">
                        <p className="text-[10px] text-slate-400 uppercase tracking-wider">Date d'inscription</p>
                        <p className="text-slate-800 font-extrabold mt-0.5">{member.joined}</p>
                    </div>
                    <div className="h-8 w-px bg-slate-200" />
                    <div className="text-left md:text-right">
                        <p className="text-[10px] text-slate-400 uppercase tracking-wider">Dernière activité</p>
                        <p className="text-slate-800 font-extrabold mt-0.5">{member.lastActive}</p>
                    </div>
                </div>
            </div>

            {/* Tabs Bar */}
            <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200/80 overflow-x-auto gap-1">
                {[
                    { id: "overview", label: "Vue d'ensemble", icon: User },
                    { id: "transactions", label: "Historique Transactions", icon: History },
                    { id: "rewards", label: "Récompenses", icon: Gift },
                    { id: "referrals", label: "Parrainage", icon: Share2 },
                    { id: "admin", label: "Actions admin", icon: Shield }
                ].map((tab) => {
                    const isActive = activeTab === tab.id;
                    return (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id as any)}
                            className={`flex items-center gap-1.5 px-3 py-2 text-xs font-bold uppercase rounded-lg transition-all shrink-0 cursor-pointer ${
                                isActive
                                    ? "bg-white text-indigo-600 shadow-2xs font-black"
                                    : "text-slate-500 hover:text-slate-850 hover:bg-slate-50/50"
                            }`}
                        >
                            <tab.icon size={13} />
                            {tab.label}
                        </button>
                    );
                })}
            </div>

            {/* TAB CONTENT */}
            <div className="space-y-6">
                
                {/* 1. OVERVIEW TAB */}
                {activeTab === "overview" && (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Member card detail fields */}
                        <Card className="border-slate-200/80 shadow-2xs">
                            <CardHeader className="py-3.5 border-b border-slate-100">
                                <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5">
                                    <User size={14} className="text-indigo-600" /> Profil
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-4 space-y-3.5 text-xs">
                                <div>
                                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Nom complet</span>
                                    <span className="font-extrabold text-slate-800 block mt-0.5">{member.name}</span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Adresse email</span>
                                    <span className="font-extrabold text-slate-800 block mt-0.5">{member.email}</span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Numéro de téléphone</span>
                                    <span className="font-extrabold text-slate-800 block mt-0.5">{member.phone}</span>
                                </div>
                                <div>
                                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Segment d'audience</span>
                                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 border border-slate-200 text-slate-700 mt-1">
                                        {member.segment}
                                    </span>
                                </div>
                            </CardContent>
                        </Card>

                        {/* Wallet balances & Progression Card */}
                        <div className="md:col-span-2 space-y-6">
                            
                            {/* Balances details */}
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <Card className="border-slate-200/80 shadow-2xs bg-gradient-to-br from-indigo-50/30 to-white">
                                    <CardContent className="p-4 flex items-center gap-3.5">
                                        <div className="p-3 bg-indigo-50 text-indigo-700 rounded-2xl border border-indigo-100 shadow-3xs">
                                            <Award size={22} />
                                        </div>
                                        <div>
                                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Solde Points</p>
                                            <p className="text-xl font-black text-slate-800 tracking-tight mt-0.5">
                                                {member.points.toLocaleString()} <span className="text-xs font-normal text-slate-500">pts</span>
                                            </p>
                                        </div>
                                    </CardContent>
                                </Card>

                                <Card className="border-slate-200/80 shadow-2xs bg-gradient-to-br from-emerald-50/30 to-white">
                                    <CardContent className="p-4 flex items-center gap-3.5">
                                        <div className="p-3 bg-emerald-50 text-emerald-700 rounded-2xl border border-emerald-100 shadow-3xs">
                                            <Wallet size={22} />
                                        </div>
                                        <div>
                                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Crédit Wallet</p>
                                            <p className="text-xl font-black text-slate-800 tracking-tight mt-0.5">
                                                {member.walletBalance.toLocaleString()} <span className="text-xs font-normal text-slate-500">XAF</span>
                                            </p>
                                        </div>
                                    </CardContent>
                                </Card>
                            </div>

                            {/* Progression Bar to next Tier */}
                            {member.walletStatus !== "CLOSED" && (
                                <Card className="border-slate-200/80 shadow-2xs">
                                    <CardContent className="p-4 space-y-3">
                                        <div className="flex justify-between items-end">
                                            <div>
                                                <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Statut fidélité</p>
                                                <h4 className="text-xs font-extrabold text-slate-800 mt-0.5">
                                                    Progression vers le palier supérieur ({member.tier === "Platinum" ? "Maximum" : "Platinum"})
                                                </h4>
                                            </div>
                                            <span className="text-xs font-extrabold text-indigo-600 bg-indigo-50 border border-indigo-100 rounded-md px-1.5 py-0.5 leading-none">
                                                {progressPercent}%
                                            </span>
                                        </div>
                                        {/* Visual Progress Bar */}
                                        <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden border border-slate-150">
                                            <div 
                                                className="h-full bg-gradient-to-r from-indigo-500 to-indigo-600 rounded-full transition-all duration-500" 
                                                style={{ width: `${progressPercent}%` }}
                                            />
                                        </div>
                                        <div className="flex justify-between items-center text-[10px] font-bold text-slate-400">
                                            <span>{member.points.toLocaleString()} pts</span>
                                            <span>
                                                {pointsNeeded > 0 ? `Encore ${pointsNeeded.toLocaleString()} pts requis` : "Palier maximal atteint"}
                                            </span>
                                            <span>{member.nextTierPoints.toLocaleString()} pts</span>
                                        </div>
                                    </CardContent>
                                </Card>
                            )}

                            {/* Recent Transactions & Active Rewards lists (side-by-side inside content) */}
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                
                                {/* 5 Recent Transactions */}
                                <Card className="border-slate-200/80 shadow-2xs flex flex-col h-[280px]">
                                    <CardHeader className="py-3 border-b border-slate-100 space-y-0 flex flex-row items-center justify-between shrink-0">
                                        <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                            Dernières Transactions
                                        </CardTitle>
                                        <History size={12} className="text-slate-400" />
                                    </CardHeader>
                                    <CardContent className="p-0 overflow-y-auto flex-1 divide-y divide-slate-100">
                                        {member.transactions.slice(0, 5).length > 0 ? (
                                            member.transactions.slice(0, 5).map((tx) => (
                                                <div key={tx.id} className="p-2.5 flex justify-between items-center text-xs">
                                                    <div className="min-w-0">
                                                        <p className="font-bold text-slate-800 truncate max-w-[140px]">{tx.description}</p>
                                                        <p className="text-[10px] text-slate-400 font-semibold">{tx.date} &bull; {tx.type}</p>
                                                    </div>
                                                    <span className={`font-black text-right shrink-0 ${
                                                        tx.amount >= 0 ? "text-emerald-600" : "text-rose-600"
                                                    }`}>
                                                        {tx.amount >= 0 ? "+" : ""}{tx.amount.toLocaleString()} {tx.ledger === "Points" ? "Pts" : "XAF"}
                                                    </span>
                                                </div>
                                            ))
                                        ) : (
                                            <p className="p-4 text-center text-[10px] text-slate-400 font-medium">Aucune transaction.</p>
                                        )}
                                    </CardContent>
                                </Card>

                                {/* Active Rewards (status: Disponible) */}
                                <Card className="border-slate-200/80 shadow-2xs flex flex-col h-[280px]">
                                    <CardHeader className="py-3 border-b border-slate-100 space-y-0 flex flex-row items-center justify-between shrink-0">
                                        <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                            Récompenses Actives
                                        </CardTitle>
                                        <Gift size={12} className="text-slate-400" />
                                    </CardHeader>
                                    <CardContent className="p-0 overflow-y-auto flex-1 divide-y divide-slate-100">
                                        {member.rewards.filter(r => r.status === "Disponible").length > 0 ? (
                                            member.rewards.filter(r => r.status === "Disponible").map((r) => (
                                                <div key={r.id} className="p-2.5 flex justify-between items-center text-xs">
                                                    <div>
                                                        <p className="font-bold text-slate-800">{r.name}</p>
                                                        <p className="text-[10px] text-slate-400 font-semibold">Emis le {r.grantedDate}</p>
                                                    </div>
                                                    <Badge className="text-[9px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-100 shadow-3xs leading-none">
                                                        Disponible
                                                    </Badge>
                                                </div>
                                            ))
                                        ) : (
                                            <div className="p-4 text-center text-[10px] text-slate-400 font-medium h-full flex flex-col justify-center items-center">
                                                <Gift size={20} className="text-slate-300 mb-1" />
                                                Aucun cadeau disponible.
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            </div>

                        </div>
                    </div>
                )}

                {/* 2. TRANSACTIONS TAB */}
                {activeTab === "transactions" && (
                    <Card className="border-slate-200/80 shadow-2xs">
                        <CardHeader className="py-4 px-4 border-b border-slate-100 space-y-3 shrink-0">
                            <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                Historique Complet des Transactions
                            </CardTitle>
                            
                            {/* Filtering and search inputs */}
                            <div className="flex flex-col sm:flex-row gap-3">
                                <div className="flex-1 relative">
                                    <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-slate-400" />
                                    <Input
                                        placeholder="Filtrer par description ou type..."
                                        value={txSearch}
                                        onChange={(e) => setTxSearch(e.target.value)}
                                        className="pl-8 h-8 text-xs border-slate-200"
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <select
                                        value={txLedgerFilter}
                                        onChange={(e) => setTxLedgerFilter(e.target.value as any)}
                                        className="h-8 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                    >
                                        <option value="all">Tous les registres</option>
                                        <option value="Points">Points uniquement</option>
                                        <option value="Wallet">Wallet uniquement</option>
                                    </select>
                                    <select
                                        value={txTypeFilter}
                                        onChange={(e) => setTxTypeFilter(e.target.value)}
                                        className="h-8 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                    >
                                        <option value="all">Tous les types</option>
                                        <option value="Achat">Achat</option>
                                        <option value="Parrainage">Parrainage</option>
                                        <option value="Ajustement Admin">Ajustement Admin</option>
                                        <option value="Rachat Récompense">Rachat Récompense</option>
                                        <option value="Crédit Wallet">Crédit Wallet</option>
                                        <option value="Débit Wallet">Débit Wallet</option>
                                    </select>
                                </div>
                            </div>
                        </CardHeader>
                        <CardContent className="p-0 overflow-x-auto">
                            <table className="w-full text-slate-700">
                                <thead>
                                    <tr className="border-b border-slate-100 bg-slate-50/70 text-[9px] font-black uppercase tracking-wider text-slate-500">
                                        <th className="text-left p-3.5">ID</th>
                                        <th className="text-left p-3.5">Date</th>
                                        <th className="text-left p-3.5">Registre</th>
                                        <th className="text-left p-3.5">Type de transaction</th>
                                        <th className="text-left p-3.5">Description</th>
                                        <th className="text-right p-3.5">Montant</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {filteredTx.length > 0 ? (
                                        filteredTx.map((tx) => (
                                            <tr key={tx.id} className="text-xs hover:bg-slate-50/30 transition-colors">
                                                <td className="p-3.5 font-mono text-slate-400 text-[10px]">{tx.id}</td>
                                                <td className="p-3.5 text-slate-400 font-bold">{tx.date}</td>
                                                <td className="p-3.5">
                                                    <Badge className={`text-[9px] font-extrabold border leading-none ${
                                                        tx.ledger === "Points"
                                                            ? "bg-indigo-50 text-indigo-700 border-indigo-150"
                                                            : "bg-emerald-50 text-emerald-700 border-emerald-150"
                                                    }`}>
                                                        {tx.ledger}
                                                    </Badge>
                                                </td>
                                                <td className="p-3.5 font-bold text-slate-800">{tx.type}</td>
                                                <td className="p-3.5 text-slate-500 font-semibold">{tx.description}</td>
                                                <td className={`p-3.5 text-right font-black ${
                                                    tx.amount >= 0 ? "text-emerald-600" : "text-rose-600"
                                                }`}>
                                                    {tx.amount >= 0 ? "+" : ""}{tx.amount.toLocaleString()} {tx.ledger === "Points" ? "Pts" : "XAF"}
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={6} className="p-8 text-center text-xs text-slate-400 font-medium">
                                                Aucune transaction correspondante.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>
                )}

                {/* 3. REWARDS TAB */}
                {activeTab === "rewards" && (
                    <Card className="border-slate-200/80 shadow-2xs">
                        <CardHeader className="py-3.5 border-b border-slate-100 flex flex-row items-center justify-between shrink-0">
                            <div>
                                <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                    Historique des Récompenses Emises
                                </CardTitle>
                                <p className="text-[10px] text-slate-400 font-medium">Bon d'achats et privilèges attribués au membre</p>
                            </div>
                            <span className="text-[10px] font-bold bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">
                                {member.rewards.length} octrois
                            </span>
                        </CardHeader>
                        <CardContent className="p-0 overflow-x-auto">
                            <table className="w-full text-slate-700">
                                <thead>
                                    <tr className="border-b border-slate-100 bg-slate-50/70 text-[9px] font-black uppercase tracking-wider text-slate-500">
                                        <th className="text-left p-3.5">ID</th>
                                        <th className="text-left p-3.5">Nom de la Récompense</th>
                                        <th className="text-left p-3.5">Date d'émission</th>
                                        <th className="text-left p-3.5">Coût d'échange</th>
                                        <th className="text-left p-3.5">Statut actuel</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {member.rewards.length > 0 ? (
                                        member.rewards.map((rew) => {
                                            let badgeClass = "bg-emerald-50 text-emerald-700 border-emerald-100";
                                            if (rew.status === "Utilisé") {
                                                badgeClass = "bg-blue-50 text-blue-700 border-blue-100";
                                            } else if (rew.status === "Expiré") {
                                                badgeClass = "bg-slate-50 text-slate-500 border-slate-200";
                                            }

                                            return (
                                                <tr key={rew.id} className="text-xs hover:bg-slate-50/30 transition-colors">
                                                    <td className="p-3.5 font-mono text-slate-400 text-[10px]">{rew.id}</td>
                                                    <td className="p-3.5 font-bold text-slate-800">{rew.name}</td>
                                                    <td className="p-3.5 text-slate-400 font-bold">{rew.grantedDate}</td>
                                                    <td className="p-3.5 text-slate-750 font-bold">{rew.pointsCost.toLocaleString()} pts</td>
                                                    <td className="p-3.5">
                                                        <Badge className={`text-[9px] font-bold border ${badgeClass} shadow-3xs`}>
                                                            {rew.status}
                                                        </Badge>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    ) : (
                                        <tr>
                                            <td colSpan={5} className="p-8 text-center text-xs text-slate-400 font-medium">
                                                Aucune récompense accordée à ce jour.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </CardContent>
                    </Card>
                )}

                {/* 4. REFERRALS TAB */}
                {activeTab === "referrals" && (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        
                        {/* Referral configuration link & stats */}
                        <div className="space-y-6">
                            
                            {/* Referral Link Box */}
                            <Card className="border-slate-200/80 shadow-2xs bg-gradient-to-br from-indigo-50/20 to-white">
                                <CardHeader className="py-3.5 border-b border-slate-100">
                                    <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5">
                                        <Share2 size={14} className="text-indigo-600" /> Lien de Parrainage
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-4 space-y-4">
                                    <div>
                                        <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Code unique</span>
                                        <span className="font-black text-slate-850 text-base tracking-wider block mt-0.5">{member.referralCode}</span>
                                    </div>
                                    <div className="space-y-1.5">
                                        <Label className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Lien complet d'invitation</Label>
                                        <div className="flex gap-1.5 items-center">
                                            <Input
                                                readOnly
                                                value={`https://yowyob.com/join?ref=${member.referralCode}`}
                                                className="h-8 text-xs font-semibold select-all bg-slate-50 border-slate-200 pr-1.5"
                                            />
                                            <Button 
                                                variant="outline" 
                                                size="icon-xs" 
                                                onClick={handleCopyLink}
                                                className="cursor-pointer"
                                            >
                                                {copied ? <Check className="h-3 w-3 text-emerald-600" /> : <Copy className="h-3 w-3" />}
                                            </Button>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>

                            {/* Conversion Stats */}
                            <Card className="border-slate-200/80 shadow-2xs">
                                <CardHeader className="py-3 border-b border-slate-100">
                                    <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                        Statistiques Filleuls
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-4 space-y-4 text-xs font-semibold">
                                    <div className="flex justify-between items-center border-b border-slate-50 pb-2">
                                        <span className="text-slate-500">Recrues totales</span>
                                        <span className="font-black text-slate-800 text-sm">{member.referrals.length}</span>
                                    </div>
                                    <div className="flex justify-between items-center border-b border-slate-50 pb-2">
                                        <span className="text-slate-500">Filleuls Convertis</span>
                                        <span className="font-black text-emerald-600 text-sm">
                                            {member.referrals.filter(r => r.status === "Converti").length}
                                        </span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-slate-500">Points récoltés</span>
                                        <span className="font-black text-indigo-600 text-sm">
                                            {(member.referrals.filter(r => r.status === "Converti").length * 500).toLocaleString()} Pts
                                        </span>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        {/* List of invitees */}
                        <Card className="md:col-span-2 border-slate-200/80 shadow-2xs">
                            <CardHeader className="py-3.5 border-b border-slate-100">
                                <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                    Registre des Filleuls Recrutés
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-0 overflow-x-auto">
                                <table className="w-full text-slate-700">
                                    <thead>
                                        <tr className="border-b border-slate-100 bg-slate-50/70 text-[9px] font-black uppercase tracking-wider text-slate-500">
                                            <th className="text-left p-3.5">Filleul</th>
                                            <th className="text-left p-3.5">Date Recrutement</th>
                                            <th className="text-left p-3.5">Statut Conversion</th>
                                            <th className="text-right p-3.5">Gain Parrainage</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-100">
                                        {member.referrals.length > 0 ? (
                                            member.referrals.map((ref) => {
                                                let badgeClass = "bg-emerald-50 text-emerald-700 border-emerald-100";
                                                if (ref.status === "En attente") {
                                                    badgeClass = "bg-amber-50 text-amber-700 border-amber-100";
                                                } else if (ref.status === "Inactif") {
                                                    badgeClass = "bg-slate-50 text-slate-500 border-slate-200";
                                                }

                                                return (
                                                    <tr key={ref.id} className="text-xs hover:bg-slate-50/30 transition-colors">
                                                        <td className="p-3.5 font-bold text-slate-800">{ref.name}</td>
                                                        <td className="p-3.5 text-slate-400 font-bold">{ref.joined}</td>
                                                        <td className="p-3.5">
                                                            <Badge className={`text-[9px] font-bold border ${badgeClass} shadow-3xs`}>
                                                                {ref.status}
                                                            </Badge>
                                                        </td>
                                                        <td className="p-3.5 text-right font-black text-slate-800">
                                                            {ref.reward}
                                                        </td>
                                                    </tr>
                                                );
                                            })
                                        ) : (
                                            <tr>
                                                <td colSpan={4} className="p-8 text-center text-xs text-slate-400 font-medium">
                                                    Aucun filleul recruté pour l'instant.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </CardContent>
                        </Card>
                    </div>
                )}

                {/* 5. ADMIN ACTIONS TAB */}
                {activeTab === "admin" && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        
                        {/* Status update & Manual balances */}
                        <div className="space-y-6">
                            
                            {/* Block A: Freeze / Unfreeze wallet */}
                            <Card className="border-slate-200/80 shadow-2xs">
                                <CardHeader className="py-3.5 border-b border-slate-100">
                                    <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5">
                                        <Lock size={14} className="text-indigo-600" /> Geler / Dégeler le Wallet
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-4 space-y-4">
                                    <div className="bg-slate-50 border border-slate-150 rounded-xl p-3 text-xs text-slate-500 font-semibold space-y-1">
                                        <p className="flex items-center gap-1 text-slate-700 font-bold">
                                            <Info size={12} className="text-indigo-600 shrink-0" />
                                            Statut actuel du wallet : 
                                            <Badge className={`text-[9.5px] font-bold border ${getStatusColor(member.walletStatus)} leading-none`}>
                                                {member.walletStatus}
                                            </Badge>
                                        </p>
                                        <p>Geler un wallet suspend immédiatement toute possibilité d'utiliser le crédit Wallet ou d'obtenir/dépenser des points de fidélité pour le membre.</p>
                                    </div>
                                    
                                    {member.walletStatus === "CLOSED" ? (
                                        <div className="text-xs font-bold text-rose-600 bg-rose-50 border border-rose-100 rounded-xl p-3 flex items-center gap-1.5">
                                            <AlertTriangle size={16} />
                                            Ce compte est définitivement clos. Aucune opération de statut n'est possible.
                                        </div>
                                    ) : (
                                        <form onSubmit={handleToggleWalletStatus} className="space-y-3.5">
                                            <div className="space-y-1.5">
                                                <Label htmlFor="freezeReason" className="text-xs font-bold text-slate-700">
                                                    Motif de l'opération <span className="text-rose-500">*</span>
                                                </Label>
                                                <Input
                                                    id="freezeReason"
                                                    placeholder="Saisissez la justification obligatoire..."
                                                    value={freezeReason}
                                                    onChange={(e) => setFreezeReason(e.target.value)}
                                                    className="h-9 text-xs border-slate-200"
                                                    required
                                                />
                                            </div>
                                            <Button 
                                                type="submit" 
                                                variant={member.walletStatus === "ACTIVE" ? "destructive" : "default"}
                                                className={`h-8 w-full text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer ${
                                                    member.walletStatus === "FROZEN" ? "bg-emerald-600 hover:bg-emerald-700 text-white" : ""
                                                }`}
                                            >
                                                {member.walletStatus === "ACTIVE" ? (
                                                    <>
                                                        <Lock size={12} />
                                                        Geler le compte wallet
                                                    </>
                                                ) : (
                                                    <>
                                                        <Unlock size={12} />
                                                        Dégeler le compte wallet
                                                    </>
                                                )}
                                            </Button>
                                        </form>
                                    )}
                                </CardContent>
                            </Card>

                            {/* Block B: Manual points adjustment */}
                            <Card className="border-slate-200/80 shadow-2xs">
                                <CardHeader className="py-3.5 border-b border-slate-100">
                                    <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider flex items-center gap-1.5">
                                        <Plus size={14} className="text-indigo-600" /> Ajustement manuel des soldes
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-4">
                                    {member.walletStatus === "CLOSED" ? (
                                        <div className="text-xs font-bold text-rose-600 bg-rose-50 border border-rose-100 rounded-xl p-3 flex items-center gap-1.5">
                                            <AlertTriangle size={16} />
                                            Ce compte est fermé. Les ajustements manuels de crédit sont bloqués.
                                        </div>
                                    ) : (
                                        <form onSubmit={handleAdjustBalances} className="space-y-4">
                                            <div className="grid grid-cols-2 gap-4">
                                                <div className="space-y-1.5">
                                                    <Label htmlFor="adjustType" className="text-xs font-bold text-slate-700">Registre</Label>
                                                    <select
                                                        id="adjustType"
                                                        value={adjustType}
                                                        onChange={(e) => setAdjustType(e.target.value as any)}
                                                        className="w-full h-8 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                                    >
                                                        <option value="Points">Points de fidélité</option>
                                                        <option value="Wallet">Crédit Wallet (XAF)</option>
                                                    </select>
                                                </div>
                                                <div className="space-y-1.5">
                                                    <Label htmlFor="adjustAction" className="text-xs font-bold text-slate-700">Opération</Label>
                                                    <select
                                                        id="adjustAction"
                                                        value={adjustAction}
                                                        onChange={(e) => setAdjustAction(e.target.value as any)}
                                                        className="w-full h-8 rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                                    >
                                                        <option value="Add">Ajouter (Créditer)</option>
                                                        <option value="Remove">Soustraire (Débiter)</option>
                                                    </select>
                                                </div>
                                            </div>
                                            
                                            <div className="space-y-1.5">
                                                <Label htmlFor="adjustAmount" className="text-xs font-bold text-slate-700">Montant de l'ajustement</Label>
                                                <Input
                                                    id="adjustAmount"
                                                    type="number"
                                                    placeholder={adjustType === "Points" ? "ex: 1000" : "ex: 5000"}
                                                    value={adjustAmount}
                                                    onChange={(e) => setAdjustAmount(e.target.value)}
                                                    className="h-8 text-xs border-slate-200"
                                                    required
                                                />
                                            </div>

                                            <div className="space-y-1.5">
                                                <Label htmlFor="adjustJustification" className="text-xs font-bold text-slate-700">
                                                    Justification de l'ajustement <span className="text-rose-500">*</span>
                                                </Label>
                                                <textarea
                                                    id="adjustJustification"
                                                    placeholder="Indiquez la raison commerciale ou l'erreur rectifiée..."
                                                    value={adjustJustification}
                                                    onChange={(e) => setAdjustJustification(e.target.value)}
                                                    className="w-full h-20 rounded-lg border border-slate-200 bg-white p-2.5 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                                    required
                                                />
                                            </div>

                                            <Button type="submit" className="h-8 w-full text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white cursor-pointer">
                                                Appliquer l'ajustement
                                            </Button>
                                        </form>
                                    )}
                                </CardContent>
                            </Card>

                        </div>

                        {/* Logs of admin actions & Closure Warning */}
                        <div className="space-y-6">
                            
                            {/* Block C: Admin History Actions Logs */}
                            <Card className="border-slate-200/80 shadow-2xs flex flex-col h-[320px]">
                                <CardHeader className="py-3 border-b border-slate-100 flex flex-row items-center justify-between shrink-0 space-y-0">
                                    <CardTitle className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                                        Journal d'Audit Administratif
                                    </CardTitle>
                                    <Shield size={12} className="text-slate-400" />
                                </CardHeader>
                                <CardContent className="p-0 overflow-y-auto flex-1 divide-y divide-slate-100">
                                    {member.adminHistory.length > 0 ? (
                                        member.adminHistory.map((log) => (
                                            <div key={log.id} className="p-3 text-xs">
                                                <div className="flex justify-between items-center font-bold text-slate-800">
                                                    <span>{log.action}</span>
                                                    <span className="text-[10px] text-slate-400 font-semibold">{log.date}</span>
                                                </div>
                                                <p className="text-slate-500 font-semibold mt-1">Motif: {log.reason}</p>
                                                <p className="text-[10px] text-slate-400 mt-0.5">Par: {log.admin}</p>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="p-8 text-center text-[10px] text-slate-400 font-medium h-full flex flex-col justify-center items-center">
                                            <Shield size={20} className="text-slate-200 mb-1" />
                                            Aucune action administrative passée.
                                        </div>
                                    )}
                                </CardContent>
                            </Card>

                            {/* Block D: Account Closure Danger Zone */}
                            <Card className="border-rose-200/80 bg-rose-50/20 shadow-2xs">
                                <CardHeader className="py-3.5 border-b border-rose-100">
                                    <CardTitle className="text-xs font-bold text-rose-700 uppercase tracking-wider flex items-center gap-1.5">
                                        <Trash2 size={14} /> Zone de Danger : Clore le compte
                                    </CardTitle>
                                </CardHeader>
                                <CardContent className="p-4">
                                    {member.walletStatus === "CLOSED" ? (
                                        <p className="text-xs font-bold text-rose-700">Ce compte est déjà clôturé définitivement.</p>
                                    ) : (
                                        <form onSubmit={handleCloseAccount} className="space-y-4">
                                            <p className="text-xs text-rose-900/80 font-semibold leading-relaxed">
                                                La clôture du compte est une opération irréversible. Elle désactive le compte client, remet le solde wallet à zéro et gèle définitivement les transactions.
                                            </p>
                                            
                                            <div className="flex items-start gap-2 text-xs text-rose-900 font-bold select-none cursor-pointer">
                                                <input
                                                    id="closeConfirm"
                                                    type="checkbox"
                                                    checked={closeConfirmed}
                                                    onChange={(e) => setCloseConfirmed(e.target.checked)}
                                                    className="rounded border-rose-300 text-rose-600 focus:ring-rose-500 mt-0.5 shrink-0"
                                                />
                                                <label htmlFor="closeConfirm" className="cursor-pointer">
                                                    Je confirme la clôture irréversible de ce compte membre.
                                                </label>
                                            </div>

                                            <div className="space-y-1.5">
                                                <Label htmlFor="closeReason" className="text-xs font-bold text-rose-800">
                                                    Motif de clôture obligatoire <span className="text-rose-500">*</span>
                                                </Label>
                                                <Input
                                                    id="closeReason"
                                                    placeholder="Saisissez la raison justifiant la suppression..."
                                                    value={closeReason}
                                                    onChange={(e) => setCloseReason(e.target.value)}
                                                    className="h-8 text-xs border-rose-200 focus-visible:ring-rose-400 focus-visible:border-transparent bg-white text-rose-900"
                                                    required
                                                />
                                            </div>

                                            <Button 
                                                type="submit" 
                                                variant="destructive"
                                                className="h-8 w-full text-xs font-bold bg-rose-600 hover:bg-rose-700 text-white cursor-pointer flex items-center justify-center gap-1.5"
                                            >
                                                <Trash2 size={12} />
                                                Clore définitivement le compte
                                            </Button>
                                        </form>
                                    )}
                                </CardContent>
                            </Card>

                        </div>

                    </div>
                )}

            </div>

        </div>
    );
}
