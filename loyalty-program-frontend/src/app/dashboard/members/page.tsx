"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import {
    Search, Filter, Download, ArrowRight, X, RotateCcw, 
    Calendar, Users, ChevronLeft, ChevronRight
} from "lucide-react";
import { getMembers, Member } from "@/lib/membersData";
import { toast } from "sonner";

export default function MembersPage() {
    const [membersList, setMembersList] = useState<Member[]>([]);
    
    // Search & Filter state
    const [searchTerm, setSearchTerm] = useState("");
    const [showFilters, setShowFilters] = useState(false);
    
    // Sidebar Filter inputs
    const [selectedTiers, setSelectedTiers] = useState<string[]>([]);
    const [selectedStatus, setSelectedStatus] = useState<string[]>([]);
    const [selectedSegments, setSelectedSegments] = useState<string[]>([]);
    const [joinStart, setJoinStart] = useState("");
    const [joinEnd, setJoinEnd] = useState("");
    const [minPoints, setMinPoints] = useState("");
    const [maxPoints, setMaxPoints] = useState("");
    const [activityFilter, setActivityFilter] = useState<"all" | "7d" | "30d" | "inactive">("all");

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5;

    // Load members from shared data source (safely handles localStorage)
    useEffect(() => {
        setMembersList(getMembers());
    }, []);

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

    // Filter Logic
    const filteredMembers = membersList.filter(member => {
        // Search Term: Name, email, phone, externalId
        const query = searchTerm.toLowerCase().trim();
        if (query) {
            const matchesSearch = 
                member.name.toLowerCase().includes(query) ||
                member.email.toLowerCase().includes(query) ||
                member.phone.toLowerCase().includes(query) ||
                member.externalId.toLowerCase().includes(query);
            if (!matchesSearch) return false;
        }

        // Tiers Filter
        if (selectedTiers.length > 0 && !selectedTiers.includes(member.tier)) {
            return false;
        }

        // Status Filter
        if (selectedStatus.length > 0 && !selectedStatus.includes(member.walletStatus)) {
            return false;
        }

        // Segment Filter
        if (selectedSegments.length > 0 && !selectedSegments.includes(member.segment)) {
            return false;
        }

        // Date Period Filter
        if (joinStart && new Date(member.joined) < new Date(joinStart)) return false;
        if (joinEnd && new Date(member.joined) > new Date(joinEnd)) return false;

        // Points Range Filter
        if (minPoints !== "" && member.points < Number(minPoints)) return false;
        if (maxPoints !== "" && member.points > Number(maxPoints)) return false;

        // Activity Period Filter
        if (activityFilter !== "all") {
            const lastActiveDate = new Date(member.lastActive);
            const now = new Date();
            const diffDays = Math.ceil((now.getTime() - lastActiveDate.getTime()) / (1000 * 60 * 60 * 24));
            
            if (activityFilter === "7d" && diffDays > 7) return false;
            if (activityFilter === "30d" && diffDays > 30) return false;
            if (activityFilter === "inactive" && diffDays <= 30) return false;
        }

        return true;
    });

    // Reset All Filters
    const handleResetFilters = () => {
        setSelectedTiers([]);
        setSelectedStatus([]);
        setSelectedSegments([]);
        setJoinStart("");
        setJoinEnd("");
        setMinPoints("");
        setMaxPoints("");
        setActivityFilter("all");
        toast.info("Filtres réinitialisés");
    };

    // Toggle Multi-select Arrays
    const toggleTier = (tier: string) => {
        setSelectedTiers(prev => prev.includes(tier) ? prev.filter(t => t !== tier) : [...prev, tier]);
    };

    const toggleStatus = (status: string) => {
        setSelectedStatus(prev => prev.includes(status) ? prev.filter(s => s !== status) : [...prev, status]);
    };

    const toggleSegment = (segment: "VIP" | "Régulier" | "Nouveau" | "Inactif") => {
        setSelectedSegments(prev => prev.includes(segment) ? prev.filter(s => s !== segment) : [...prev, segment]);
    };

    // CSV Exporter (filtered results only)
    const handleExportCSV = () => {
        if (filteredMembers.length === 0) {
            toast.error("Aucun membre à exporter");
            return;
        }

        const headers = ["ID", "Nom", "Email", "Téléphone", "ID Externe", "Palier", "Points", "Wallet Balance", "Statut", "Inscription", "Dernière Activité"];
        const rows = filteredMembers.map(m => [
            m.id,
            m.name,
            m.email,
            m.phone,
            m.externalId,
            m.tier,
            m.points,
            m.walletBalance,
            m.walletStatus,
            m.joined,
            m.lastActive
        ]);

        const csvContent = [
            headers,
            ...rows
        ].map(row => row.map(val => `"${String(val).replace(/"/g, '""')}"`).join(",")).join("\n");

        const blob = new Blob(["\uFEFF" + csvContent], { type: "text/csv;charset=utf-8;" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", `membres_loyalty_${new Date().toISOString().split('T')[0]}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        toast.success(`${filteredMembers.length} membres exportés avec succès !`);
    };

    // Pagination Calculation
    const totalPages = Math.max(1, Math.ceil(filteredMembers.length / itemsPerPage));
    const paginatedMembers = filteredMembers.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

    // Reset to page 1 on search or filter change
    useEffect(() => {
        setCurrentPage(1);
    }, [searchTerm, selectedTiers, selectedStatus, selectedSegments, joinStart, joinEnd, minPoints, maxPoints, activityFilter]);

    return (
        <div className="space-y-6 relative pb-10">
            
            {/* Header Area */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
                        <Users className="h-6 w-6 text-indigo-600" />
                        Registre des Membres
                    </h1>
                    <p className="text-xs text-slate-500 font-semibold mt-0.5">
                        Consultez, filtrez et gérez les comptes fidélité de vos clients.
                    </p>
                </div>
                <Button 
                    onClick={handleExportCSV}
                    className="flex items-center gap-2 h-9 px-4 bg-slate-900 hover:bg-black text-white rounded-xl text-xs font-bold shrink-0 self-start sm:self-auto cursor-pointer"
                >
                    <Download size={14} />
                    Exporter CSV
                </Button>
            </div>

            {/* Quick Summary Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                <Card className="border-slate-200/80 shadow-2xs">
                    <CardContent className="p-4">
                        <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Total Membres</p>
                        <p className="text-xl font-black text-slate-800 mt-1">{membersList.length}</p>
                    </CardContent>
                </Card>
                <Card className="border-slate-200/80 shadow-2xs">
                    <CardContent className="p-4">
                        <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Membres Filtrés</p>
                        <p className="text-xl font-black text-indigo-600 mt-1">{filteredMembers.length}</p>
                    </CardContent>
                </Card>
                <Card className="border-slate-200/80 shadow-2xs">
                    <CardContent className="p-4">
                        <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Comptes Actifs</p>
                        <p className="text-xl font-black text-emerald-600 mt-1">
                            {membersList.filter(m => m.walletStatus === "ACTIVE").length}
                        </p>
                    </CardContent>
                </Card>
                <Card className="border-slate-200/80 shadow-2xs">
                    <CardContent className="p-4">
                        <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Comptes Gelés</p>
                        <p className="text-xl font-black text-amber-600 mt-1">
                            {membersList.filter(m => m.walletStatus === "FROZEN").length}
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Search and Filters Trigger Bar */}
            <Card className="border-slate-200/80 shadow-2xs">
                <CardContent className="p-3 flex gap-3 items-center">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
                        <Input
                            placeholder="Rechercher par nom, email, téléphone ou ID externe (ex: EMP-9982)..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-9 h-9 text-xs border-slate-200/80 focus-visible:ring-indigo-500"
                        />
                    </div>
                    <Button 
                        variant="outline" 
                        onClick={() => setShowFilters(true)}
                        className={`h-9 px-4 rounded-xl text-xs font-bold flex items-center gap-1.5 cursor-pointer transition-all ${
                            (selectedTiers.length > 0 || selectedStatus.length > 0 || selectedSegments.length > 0 || joinStart || joinEnd || minPoints || maxPoints || activityFilter !== "all")
                                ? "border-indigo-200 bg-indigo-50/50 text-indigo-700 hover:bg-indigo-50"
                                : "border-slate-200 hover:bg-slate-50"
                        }`}
                    >
                        <Filter size={14} />
                        Filtres
                        {(selectedTiers.length + selectedStatus.length + selectedSegments.length + (joinStart ? 1 : 0) + (joinEnd ? 1 : 0) + (minPoints ? 1 : 0) + (maxPoints ? 1 : 0) + (activityFilter !== "all" ? 1 : 0)) > 0 && (
                            <span className="h-4.5 min-w-4.5 px-1 bg-indigo-600 text-white rounded-full flex items-center justify-center text-[9px] font-black">
                                {selectedTiers.length + selectedStatus.length + selectedSegments.length + (joinStart ? 1 : 0) + (joinEnd ? 1 : 0) + (minPoints ? 1 : 0) + (maxPoints ? 1 : 0) + (activityFilter !== "all" ? 1 : 0)}
                            </span>
                        )}
                    </Button>
                </CardContent>
            </Card>

            {/* Members Table */}
            <Card className="border-slate-200/80 shadow-xs overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-slate-700 border-collapse">
                        <thead>
                            <tr className="border-b border-slate-100 bg-slate-50/70 text-[10px] font-black uppercase tracking-wider text-slate-500">
                                <th className="text-left p-4">Membre</th>
                                <th className="text-left p-4">ID Externe</th>
                                <th className="text-left p-4">Palier</th>
                                <th className="text-left p-4">Points</th>
                                <th className="text-left p-4">Wallet</th>
                                <th className="text-left p-4">Statut Wallet</th>
                                <th className="text-left p-4">Inscription</th>
                                <th className="text-left p-4">Dernière Act.</th>
                                <th className="w-10"></th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {paginatedMembers.length > 0 ? (
                                paginatedMembers.map((member) => (
                                    <tr key={member.id} className="hover:bg-slate-50/50 transition-colors">
                                        <td className="p-4">
                                            <Link href={`/dashboard/members/${member.id}`} className="flex items-center gap-3">
                                                <div className="w-9 h-9 bg-indigo-50 text-indigo-700 font-extrabold rounded-full flex items-center justify-center text-xs border border-indigo-100">
                                                    {member.avatar}
                                                </div>
                                                <div className="min-w-0">
                                                    <p className="text-xs font-bold text-slate-800 hover:text-indigo-600 hover:underline transition-all">
                                                        {member.name}
                                                    </p>
                                                    <p className="text-[10px] text-slate-400 font-medium">
                                                        {member.email}
                                                    </p>
                                                </div>
                                            </Link>
                                        </td>
                                        <td className="p-4 text-xs font-mono text-slate-500">
                                            {member.externalId}
                                        </td>
                                        <td className="p-4">
                                            <Badge className={`text-[10px] font-bold border ${getTierColor(member.tier)} shadow-3xs`}>
                                                {member.tier}
                                            </Badge>
                                        </td>
                                        <td className="p-4 text-xs font-extrabold text-slate-800">
                                            {member.points.toLocaleString()} pts
                                        </td>
                                        <td className="p-4 text-xs font-bold text-slate-800">
                                            {member.walletBalance.toLocaleString()} XAF
                                        </td>
                                        <td className="p-4">
                                            <Badge className={`text-[10px] font-bold border ${getStatusColor(member.walletStatus)} shadow-3xs`}>
                                                {member.walletStatus}
                                            </Badge>
                                        </td>
                                        <td className="p-4 text-[10px] text-slate-400 font-bold">
                                            {member.joined}
                                        </td>
                                        <td className="p-4 text-[10px] text-slate-400 font-bold">
                                            {member.lastActive}
                                        </td>
                                        <td className="p-4">
                                            <Link href={`/dashboard/members/${member.id}`}>
                                                <Button variant="ghost" size="icon-xs" className="text-slate-400 hover:text-indigo-600 cursor-pointer">
                                                    <ArrowRight size={14} />
                                                </Button>
                                            </Link>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={9} className="p-8 text-center text-xs text-slate-400 font-medium">
                                        Aucun membre ne correspond à vos critères de recherche ou filtres.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination Controls */}
                {totalPages > 1 && (
                    <div className="p-4 border-t border-slate-100 bg-slate-50/30 flex items-center justify-between text-xs font-semibold text-slate-500">
                        <p>
                            Affichage de <span className="font-extrabold text-slate-800">{Math.min(filteredMembers.length, (currentPage - 1) * itemsPerPage + 1)}-{Math.min(filteredMembers.length, currentPage * itemsPerPage)}</span> sur <span className="font-extrabold text-slate-800">{filteredMembers.length}</span> membres
                        </p>
                        <div className="flex items-center gap-1.5">
                            <Button
                                variant="outline"
                                size="sm"
                                disabled={currentPage === 1}
                                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                className="h-7 px-2 border-slate-200 cursor-pointer"
                            >
                                <ChevronLeft size={14} />
                                Précédent
                            </Button>
                            <span className="px-2 text-slate-800 font-bold">
                                Page {currentPage} sur {totalPages}
                            </span>
                            <Button
                                variant="outline"
                                size="sm"
                                disabled={currentPage === totalPages}
                                onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                                className="h-7 px-2 border-slate-200 cursor-pointer"
                            >
                                Suivant
                                <ChevronRight size={14} />
                            </Button>
                        </div>
                    </div>
                )}
            </Card>

            {/* ==================================================== */}
            {/* RETRACTABLE FILTER DRAWER (SLIDING SIDEBAR) */}
            {/* ==================================================== */}
            {showFilters && (
                <>
                    {/* Backdrop */}
                    <div 
                        className="fixed inset-0 z-40 bg-slate-900/30 backdrop-blur-xs transition-opacity duration-200" 
                        onClick={() => setShowFilters(false)}
                    />
                    
                    {/* Drawer container */}
                    <div className="fixed right-0 top-0 bottom-0 z-50 w-80 bg-white border-l border-slate-200 p-6 shadow-2xl flex flex-col justify-between overflow-y-auto animate-in slide-in-from-right duration-200">
                        <div className="space-y-6">
                            {/* Drawer Header */}
                            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                                <div>
                                    <h3 className="text-sm font-extrabold uppercase text-slate-900 tracking-wider">Filtres Avancés</h3>
                                    <p className="text-[10px] text-slate-400 font-medium">Combinez les critères de ciblage</p>
                                </div>
                                <Button 
                                    variant="ghost" 
                                    size="icon-sm" 
                                    onClick={() => setShowFilters(false)}
                                    className="text-slate-400 hover:text-slate-600 cursor-pointer"
                                >
                                    <X size={16} />
                                </Button>
                            </div>

                            {/* Section 1: Palier / Tiers */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Paliers (Tiers)</Label>
                                <div className="grid grid-cols-2 gap-2">
                                    {["Bronze", "Silver", "Gold", "Platinum"].map((tier) => {
                                        const isSelected = selectedTiers.includes(tier);
                                        return (
                                            <button
                                                key={tier}
                                                onClick={() => toggleTier(tier)}
                                                className={`py-1.5 px-2 rounded-lg text-[10px] font-bold border text-center transition-all cursor-pointer ${
                                                    isSelected
                                                        ? "bg-indigo-600 text-white border-indigo-600 shadow-2xs font-extrabold"
                                                        : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"
                                                }`}
                                            >
                                                {tier}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Section 2: Statut Wallet */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Statut Wallet</Label>
                                <div className="grid grid-cols-3 gap-1.5">
                                    {["ACTIVE", "FROZEN", "CLOSED"].map((status) => {
                                        const isSelected = selectedStatus.includes(status);
                                        return (
                                            <button
                                                key={status}
                                                onClick={() => toggleStatus(status)}
                                                className={`py-1 px-1.5 rounded-lg text-[9px] font-extrabold border text-center transition-all cursor-pointer ${
                                                    isSelected
                                                        ? "bg-indigo-600 text-white border-indigo-600 shadow-2xs"
                                                        : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"
                                                }`}
                                            >
                                                {status}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Section 3: Segments */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Segments</Label>
                                <div className="grid grid-cols-2 gap-2">
                                    {(["VIP", "Régulier", "Nouveau", "Inactif"] as const).map((segment) => {
                                        const isSelected = selectedSegments.includes(segment);
                                        return (
                                            <button
                                                key={segment}
                                                onClick={() => toggleSegment(segment)}
                                                className={`py-1.5 px-2 rounded-lg text-[10px] font-bold border text-center transition-all cursor-pointer ${
                                                    isSelected
                                                        ? "bg-indigo-600 text-white border-indigo-600 shadow-2xs"
                                                        : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"
                                                }`}
                                            >
                                                {segment}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Section 4: Points Range */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Tranche de solde Points</Label>
                                <div className="flex gap-2 items-center">
                                    <Input
                                        type="number"
                                        placeholder="Min"
                                        value={minPoints}
                                        onChange={(e) => setMinPoints(e.target.value)}
                                        className="h-8 text-xs border-slate-200"
                                    />
                                    <span className="text-slate-400 text-xs">-</span>
                                    <Input
                                        type="number"
                                        placeholder="Max"
                                        value={maxPoints}
                                        onChange={(e) => setMaxPoints(e.target.value)}
                                        className="h-8 text-xs border-slate-200"
                                    />
                                </div>
                            </div>

                            {/* Section 5: Period of Registration */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Inscription</Label>
                                <div className="space-y-1.5">
                                    <div className="relative">
                                        <Calendar className="absolute left-2.5 top-2 h-3.5 w-3.5 text-slate-400" />
                                        <Input
                                            type="date"
                                            value={joinStart}
                                            onChange={(e) => setJoinStart(e.target.value)}
                                            className="pl-8 h-8 text-xs border-slate-200"
                                        />
                                    </div>
                                    <div className="relative">
                                        <Calendar className="absolute left-2.5 top-2 h-3.5 w-3.5 text-slate-400" />
                                        <Input
                                            type="date"
                                            value={joinEnd}
                                            onChange={(e) => setJoinEnd(e.target.value)}
                                            className="pl-8 h-8 text-xs border-slate-200"
                                        />
                                    </div>
                                </div>
                            </div>

                            {/* Section 6: Recent Activity */}
                            <div className="space-y-2">
                                <Label className="text-xs font-bold text-slate-700 uppercase tracking-wide">Activité récente</Label>
                                <div className="space-y-1.5 text-xs font-semibold text-slate-600">
                                    {[
                                        { id: "all", label: "Toute activité" },
                                        { id: "7d", label: "Actif ces 7 derniers jours" },
                                        { id: "30d", label: "Actif ces 30 derniers jours" },
                                        { id: "inactive", label: "Inactif depuis plus de 30 jours" }
                                    ].map((opt) => (
                                        <label key={opt.id} className="flex items-center gap-2 cursor-pointer py-0.5">
                                            <input
                                                type="radio"
                                                name="activity"
                                                checked={activityFilter === opt.id}
                                                onChange={() => setActivityFilter(opt.id as any)}
                                                className="text-indigo-600 focus:ring-indigo-500"
                                            />
                                            <span>{opt.label}</span>
                                        </label>
                                    ))}
                                </div>
                            </div>

                        </div>

                        {/* Drawer Actions at Bottom */}
                        <div className="border-t border-slate-100 pt-4 flex gap-3 mt-6">
                            <Button
                                variant="outline"
                                onClick={handleResetFilters}
                                className="flex-1 h-9 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 border-slate-200 text-slate-600 cursor-pointer"
                            >
                                <RotateCcw size={12} />
                                Reset
                            </Button>
                            <Button
                                onClick={() => setShowFilters(false)}
                                className="flex-1 h-9 rounded-xl text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white cursor-pointer"
                            >
                                Appliquer
                            </Button>
                        </div>
                    </div>
                </>
            )}

        </div>
    );
}