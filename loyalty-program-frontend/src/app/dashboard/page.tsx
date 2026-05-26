"use client";

import React, { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { 
    Users, Award, TrendingUp, Wallet, UserPlus, Gift,
    ArrowUp, ArrowDown, Sparkles, AlertTriangle, RefreshCw,
    Star, ShieldAlert, Rocket, Tag, Clock, Plus, ChevronRight
} from "lucide-react";
import { 
    AreaChart, Area, LineChart, Line, XAxis, YAxis, 
    CartesianGrid, Tooltip, ResponsiveContainer 
} from "recharts";
import {
    Dialog, DialogContent, DialogDescription, DialogHeader, 
    DialogTitle, DialogFooter, DialogClose
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";

export default function HomeDashboard() {
    // ----------------------------------------------------
    // State & Interactive Data
    // ----------------------------------------------------
    const [timeframe, setTimeframe] = useState<"daily" | "weekly" | "monthly">("daily");
    
    // Alerts State
    const [alerts, setAlerts] = useState([
        { id: 1, text: "Wallet gelé : Le wallet de @alice.koffi (ID #8843) est suspendu en attente de validation administrative.", type: "wallet" },
        { id: 2, text: "Campagne expirante : La campagne 'Double Points Ramadan' se termine dans 48 heures.", type: "campaign" },
        { id: 3, text: "Quota promo : Le code promo 'SPRING30' a atteint 92% de sa limite de rédemption.", type: "promo" }
    ]);

    // Dialog Modal States
    const [isRuleOpen, setIsRuleOpen] = useState(false);
    const [isCampaignOpen, setIsCampaignOpen] = useState(false);
    const [isRewardOpen, setIsRewardOpen] = useState(false);
    const [isPromoOpen, setIsPromoOpen] = useState(false);

    // Form Fields State
    const [ruleName, setRuleName] = useState("");
    const [ruleTrigger, setRuleTrigger] = useState("purchase");
    const [rulePoints, setRulePoints] = useState("100");
    const [ruleMinPurchase, setRuleMinPurchase] = useState("5000");

    const [campName, setCampName] = useState("");
    const [campSegment, setCampSegment] = useState("all");
    const [campMultiplier, setCampMultiplier] = useState("2");
    const [campStart, setCampStart] = useState("");
    const [campEnd, setCampEnd] = useState("");

    const [rewName, setRewName] = useState("");
    const [rewPoints, setRewPoints] = useState("500");
    const [rewCategory, setRewCategory] = useState("voucher");
    const [rewStock, setRewStock] = useState("100");

    const [promoCode, setPromoCode] = useState("");
    const [promoDiscountType, setPromoDiscountType] = useState("percent");
    const [promoDiscountVal, setPromoDiscountVal] = useState("15");
    const [promoLimit, setPromoLimit] = useState("500");
    const [promoCost, setPromoCost] = useState("150");

    // Important Events log state
    const [eventsList, setEventsList] = useState([
        { id: 1, type: "gold", title: "Membre passé Gold", detail: "Alice Dupont a atteint le palier Or après ses achats.", time: "Il y a 5 min" },
        { id: 2, type: "fraud", title: "Fraude détectée", detail: "Double encaissement suspect sur le membre #9983.", time: "Il y a 25 min" },
        { id: 3, type: "campaign", title: "Campagne activée", detail: "Lancement de la campagne 'Double points printemps'.", time: "Il y a 1 h" },
        { id: 4, type: "promo", title: "Code promo épuisé", detail: "Le code promo 'SPRING30' a atteint sa limite d'utilisation.", time: "Il y a 2 h" },
        { id: 5, type: "gold", title: "Membre passé Gold", detail: "Marc Koffi a atteint le palier Or.", time: "Il y a 3 h" },
        { id: 6, type: "campaign", title: "Campagne activée", detail: "Lancement de la campagne 'Parrainage Mai'.", time: "Il y a 5 h" },
        { id: 7, type: "fraud", title: "Fraude détectée", detail: "Transaction anormale de 50 000 points bloquée.", time: "Il y a 12 h" },
        { id: 8, type: "gold", title: "Membre passé Gold", detail: "Sarah Touré a atteint le palier Or.", time: "Hier, 15:45" },
        { id: 9, type: "promo", title: "Code promo épuisé", detail: "Le code promo 'BIENVENUE10' est épuisé.", time: "Hier, 09:12" },
        { id: 10, type: "campaign", title: "Campagne activée", detail: "Lancement de la campagne 'Bonus Week-end'.", time: "Il y a 2 jours" },
    ]);

    // ----------------------------------------------------
    // Mock Sparklines & Main Chart Data
    // ----------------------------------------------------
    const activeMembersSparkline = [
        { val: 1100 }, { val: 1120 }, { val: 1150 }, { val: 1130 },
        { val: 1160 }, { val: 1180 }, { val: 1200 }, { val: 1190 },
        { val: 1220 }, { val: 1235 }, { val: 1248 }
    ];

    const pointsSparkline = [
        { val: 260000 }, { val: 258000 }, { val: 254000 }, { val: 255000 },
        { val: 252000 }, { val: 249000 }, { val: 247000 }, { val: 248500 },
        { val: 246000 }, { val: 245890 }
    ];

    // Main Chart Data Sets
    const dailyData = [
        { name: 'J-29', distrib: 4500, spent: 3100, tx: 85 },
        { name: 'J-26', distrib: 5800, spent: 4200, tx: 105 },
        { name: 'J-23', distrib: 6200, spent: 4500, tx: 115 },
        { name: 'J-20', distrib: 7500, spent: 5500, tx: 140 },
        { name: 'J-17', distrib: 8500, spent: 6200, tx: 160 },
        { name: 'J-14', distrib: 9200, spent: 7100, tx: 178 },
        { name: 'J-11', distrib: 10200, spent: 7900, tx: 202 },
        { name: 'J-8', distrib: 11200, spent: 8800, tx: 228 },
        { name: 'J-5', distrib: 12200, spent: 9600, tx: 252 },
        { name: 'J-2', distrib: 13200, spent: 10500, tx: 278 },
        { name: 'J-0', distrib: 14500, spent: 11800, tx: 312 },
    ];

    const weeklyData = [
        { name: 'Sem 1', distrib: 35000, spent: 24000, tx: 680 },
        { name: 'Sem 2', distrib: 42000, spent: 30000, tx: 810 },
        { name: 'Sem 3', distrib: 39000, spent: 28000, tx: 760 },
        { name: 'Sem 4', distrib: 48000, spent: 35000, tx: 920 },
        { name: 'Sem 5', distrib: 52000, spent: 39000, tx: 1050 },
        { name: 'Sem 6', distrib: 49000, spent: 36000, tx: 980 },
        { name: 'Sem 7', distrib: 58000, spent: 44000, tx: 1180 },
        { name: 'Sem 8', distrib: 62000, spent: 48000, tx: 1250 },
    ];

    const monthlyData = [
        { name: 'Déc', distrib: 160000, spent: 110000, tx: 3100 },
        { name: 'Jan', distrib: 185000, spent: 130000, tx: 3600 },
        { name: 'Fév', distrib: 172000, spent: 125000, tx: 3400 },
        { name: 'Mar', distrib: 210000, spent: 155000, tx: 4200 },
        { name: 'Avr', distrib: 235000, spent: 178000, tx: 4700 },
        { name: 'Mai', distrib: 245890, spent: 189000, tx: 4980 },
    ];

    const activeChartData = timeframe === "daily" 
        ? dailyData 
        : timeframe === "weekly" 
            ? weeklyData 
            : monthlyData;

    // ----------------------------------------------------
    // Form Submit Handlers
    // ----------------------------------------------------
    const handleCreateRule = (e: React.FormEvent) => {
        e.preventDefault();
        if (!ruleName.trim()) {
            toast.error("Veuillez saisir un nom pour la règle");
            return;
        }
        
        const newEvent = {
            id: Date.now(),
            type: "campaign",
            title: "Nouvelle règle créée",
            detail: `La règle de fidélité '${ruleName}' (${ruleTrigger === "purchase" ? "Achat" : "Parrainage"}) a été configurée.`,
            time: "À l'instant"
        };
        setEventsList(prev => [newEvent, ...prev.slice(0, 9)]);
        toast.success(`Règle '${ruleName}' créée avec succès !`);
        setIsRuleOpen(false);
        setRuleName("");
    };

    const handleLaunchCampaign = (e: React.FormEvent) => {
        e.preventDefault();
        if (!campName.trim()) {
            toast.error("Veuillez saisir un nom pour la campagne");
            return;
        }

        const newEvent = {
            id: Date.now(),
            type: "campaign",
            title: "Campagne lancée",
            detail: `Campagne '${campName}' activée (segment: ${campSegment === "all" ? "Tous" : campSegment.toUpperCase()}).`,
            time: "À l'instant"
        };
        setEventsList(prev => [newEvent, ...prev.slice(0, 9)]);
        toast.success(`Campagne '${campName}' lancée avec succès !`);
        setIsCampaignOpen(false);
        setCampName("");
    };

    const handleAddReward = (e: React.FormEvent) => {
        e.preventDefault();
        if (!rewName.trim()) {
            toast.error("Veuillez saisir le nom de la récompense");
            return;
        }

        const newEvent = {
            id: Date.now(),
            type: "gold",
            title: "Récompense ajoutée",
            detail: `Récompense '${rewName}' ajoutée pour ${rewPoints} points. Stock initial: ${rewStock}.`,
            time: "À l'instant"
        };
        setEventsList(prev => [newEvent, ...prev.slice(0, 9)]);
        toast.success(`Récompense '${rewName}' ajoutée au catalogue !`);
        setIsRewardOpen(false);
        setRewName("");
    };

    const handleGeneratePromo = (e: React.FormEvent) => {
        e.preventDefault();
        if (!promoCode.trim()) {
            toast.error("Veuillez saisir un code promo");
            return;
        }

        const newEvent = {
            id: Date.now(),
            type: "promo",
            title: "Code Promo généré",
            detail: `Code '${promoCode}' généré avec ${promoDiscountVal}${promoDiscountType === "percent" ? "%" : " XAF"} de remise.`,
            time: "À l'instant"
        };
        setEventsList(prev => [newEvent, ...prev.slice(0, 9)]);
        toast.success(`Code promo '${promoCode}' généré avec succès !`);
        setIsPromoOpen(false);
        setPromoCode("");
    };

    // ----------------------------------------------------
    // Redemption Rate Gauge Helpers
    // ----------------------------------------------------
    const redemptionValue = 28.4;
    let gaugeColor = "text-amber-500";
    let labelColor = "text-amber-800 bg-amber-50 border-amber-200";
    let statusText = "Moyen (10-30%)";

    if (redemptionValue < 10) {
        gaugeColor = "text-red-500";
        labelColor = "text-red-800 bg-red-50 border-red-200";
        statusText = "Faible (<10%)";
    } else if (redemptionValue > 30) {
        gaugeColor = "text-emerald-500";
        labelColor = "text-emerald-800 bg-emerald-50 border-emerald-200";
        statusText = "Excellent (>30%)";
    }

    return (
        <div className="space-y-6 pb-12">
            
            {/* 1. TOP WELCOME BAR */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 p-5 bg-white rounded-2xl border border-slate-200/80 shadow-xs">
                <div className="flex items-center gap-3">
                    <div className="p-2.5 bg-indigo-50 text-indigo-600 rounded-xl border border-indigo-100 shadow-xs">
                        <Sparkles className="h-5 w-5" />
                    </div>
                    <div>
                        <h1 className="text-xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
                            Yowyob Store
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-100 uppercase tracking-wider">
                                Plan Pro
                            </span>
                        </h1>
                        <p className="text-xs text-slate-500 font-medium">
                            Tableau de bord administrateur &bull; Renouvellement : <span className="font-bold text-slate-700">30 Juin 2026</span>
                        </p>
                    </div>
                </div>
                <div className="flex items-center gap-3">
                    <button 
                        onClick={() => {
                            toast.success("Données actualisées");
                        }}
                        className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-slate-600 hover:text-slate-900 bg-slate-50 hover:bg-slate-100 border border-slate-200 transition-all font-semibold text-xs"
                    >
                        <RefreshCw className="h-3.5 w-3.5" />
                        Actualiser
                    </button>
                </div>
            </div>

            {/* 2. ORANGE ALERT BANNER */}
            {alerts.length > 0 && (
                <div className="relative overflow-hidden bg-amber-50/70 border border-amber-200 rounded-2xl p-4 flex items-start gap-3 text-amber-900 shadow-2xs">
                    <div className="p-1.5 bg-amber-100 text-amber-800 rounded-lg shrink-0 mt-0.5">
                        <AlertTriangle className="h-5 w-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1.5">
                            <h4 className="text-xs font-bold text-amber-950 uppercase tracking-wider">Alertes nécessitant votre attention ({alerts.length})</h4>
                            <button 
                                onClick={() => setAlerts([])} 
                                className="text-xs font-semibold text-amber-800 hover:text-amber-950 underline underline-offset-2 transition-colors cursor-pointer"
                            >
                                Tout masquer
                            </button>
                        </div>
                        <ul className="space-y-2">
                            {alerts.map((alert) => (
                                <li key={alert.id} className="text-xs text-amber-900/90 flex items-center justify-between gap-4 font-semibold">
                                    <span className="flex items-center gap-2">
                                        <span className="w-1.5 h-1.5 rounded-full bg-amber-500 shrink-0" />
                                        {alert.text}
                                    </span>
                                    <button 
                                        onClick={() => {
                                            toast.info(`Traitement de l'alerte: ${alert.text.split(":")[0]}`);
                                            setAlerts(prev => prev.filter(a => a.id !== alert.id));
                                        }}
                                        className="text-[10px] font-extrabold uppercase tracking-wider text-amber-900 hover:text-amber-950 bg-amber-100/50 hover:bg-amber-100 border border-amber-200/50 rounded-md px-2 py-0.5 shrink-0 transition-colors"
                                    >
                                        Résoudre
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}

            {/* 3. SIX METRIC CARDS GRID */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-5">
                
                {/* Metric 1: Membres Actifs */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Membres Actifs</span>
                            <div className="p-1.5 bg-blue-50 text-blue-600 rounded-lg">
                                <Users size={16} />
                            </div>
                        </div>
                        <div>
                            <span className="text-2xl font-black text-slate-900 tracking-tight">1,248</span>
                            <div className="flex items-center gap-1 text-[10px] font-bold text-emerald-600 mt-0.5">
                                <ArrowUp size={10} />
                                <span>+12.5% vs mois dern.</span>
                            </div>
                        </div>
                        {/* Sparkline Area Chart */}
                        <div className="h-[30px] w-full mt-1">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={activeMembersSparkline} margin={{ top: 2, bottom: 2, left: 2, right: 2 }}>
                                    <defs>
                                        <linearGradient id="colorMembers" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#10b981" stopOpacity={0.3}/>
                                            <stop offset="95%" stopColor="#10b981" stopOpacity={0.0}/>
                                        </linearGradient>
                                    </defs>
                                    <Area type="monotone" dataKey="val" stroke="#10b981" strokeWidth={1.5} fillOpacity={1} fill="url(#colorMembers)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>

                {/* Metric 2: Points Distribués */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Pts Distribués</span>
                            <div className="p-1.5 bg-purple-50 text-purple-600 rounded-lg">
                                <Award size={16} />
                            </div>
                        </div>
                        <div>
                            <span className="text-2xl font-black text-slate-900 tracking-tight">245,890</span>
                            <div className="flex items-center gap-1 text-[10px] font-bold text-rose-600 mt-0.5">
                                <ArrowDown size={10} />
                                <span>-3.2% vs mois dern.</span>
                            </div>
                        </div>
                        {/* Sparkline Area Chart */}
                        <div className="h-[30px] w-full mt-1">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={pointsSparkline} margin={{ top: 2, bottom: 2, left: 2, right: 2 }}>
                                    <defs>
                                        <linearGradient id="colorPoints" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3}/>
                                            <stop offset="95%" stopColor="#ef4444" stopOpacity={0.0}/>
                                        </linearGradient>
                                    </defs>
                                    <Area type="monotone" dataKey="val" stroke="#ef4444" strokeWidth={1.5} fillOpacity={1} fill="url(#colorPoints)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>

                {/* Metric 3: Taux de Rédemption (with Gauge) */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-1">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Rédemption</span>
                            <div className="p-1.5 bg-amber-50 text-amber-600 rounded-lg">
                                <TrendingUp size={16} />
                            </div>
                        </div>
                        {/* Custom Radial Gauge */}
                        <div className="flex flex-col items-center justify-center py-1">
                            <div className="relative flex items-center justify-center h-14 w-14">
                                <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                                    <path
                                        className="text-slate-100"
                                        strokeWidth="3.5"
                                        stroke="currentColor"
                                        fill="none"
                                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                    />
                                    <path
                                        className={gaugeColor}
                                        strokeDasharray={`${redemptionValue}, 100`}
                                        strokeWidth="3.5"
                                        strokeLinecap="round"
                                        stroke="currentColor"
                                        fill="none"
                                        d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                    />
                                </svg>
                                <div className="absolute flex flex-col items-center justify-center">
                                    <span className="text-[11px] font-extrabold text-slate-800 leading-none">{redemptionValue}%</span>
                                </div>
                            </div>
                            <span className={`mt-1.5 px-2 py-0.5 rounded-full text-[9px] font-extrabold border ${labelColor} leading-none`}>
                                {statusText}
                            </span>
                        </div>
                    </CardContent>
                </Card>

                {/* Metric 4: Volume Wallet */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Volume Wallet</span>
                            <div className="p-1.5 bg-emerald-50 text-emerald-600 rounded-lg">
                                <Wallet size={16} />
                            </div>
                        </div>
                        <div className="space-y-1">
                            <span className="text-xl font-black text-slate-900 tracking-tight">8.4M Pts</span>
                            <p className="text-[10px] font-bold text-slate-500 bg-slate-50 border border-slate-100 rounded-md px-1.5 py-0.5 w-fit">
                                ≈ 8 400 000 XAF
                            </p>
                        </div>
                        <div className="flex items-center gap-1 text-[10px] font-bold text-emerald-600 mt-1">
                            <ArrowUp size={10} />
                            <span>+18.2% vs mois dern.</span>
                        </div>
                    </CardContent>
                </Card>

                {/* Metric 5: Parrainages */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Parrainages</span>
                            <div className="p-1.5 bg-cyan-50 text-cyan-600 rounded-lg">
                                <UserPlus size={16} />
                            </div>
                        </div>
                        <div>
                            <span className="text-2xl font-black text-slate-900 tracking-tight">87</span>
                            <p className="text-[10px] text-slate-400 font-semibold mt-0.5">nouveaux ce mois</p>
                        </div>
                        <div className="flex items-center gap-1 text-[10px] font-bold text-emerald-600 mt-1">
                            <ArrowUp size={10} />
                            <span>+24.0% vs mois dern.</span>
                        </div>
                    </CardContent>
                </Card>

                {/* Metric 6: Codes Promo Actifs */}
                <Card className="border-slate-200/80 shadow-2xs hover:shadow-xs transition-shadow">
                    <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
                        <div className="flex items-center justify-between">
                            <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Codes Promo</span>
                            <div className="p-1.5 bg-rose-50 text-rose-600 rounded-lg">
                                <Tag size={16} />
                            </div>
                        </div>
                        <div className="space-y-1">
                            <span className="text-2xl font-black text-slate-900 tracking-tight">12</span>
                            <p className="text-[10px] font-bold text-slate-500 bg-slate-50 border border-slate-100 rounded-md px-1.5 py-0.5 w-fit">
                                64% util. moyenne
                            </p>
                        </div>
                        <span className="text-[9px] text-slate-400 font-semibold block mt-1">
                            Taux d'utilisation stable
                        </span>
                    </CardContent>
                </Card>
            </div>

            {/* 4. SIDE-BY-SIDE PANELS */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                
                {/* Left Panel: Events List (10 elements) */}
                <Card className="border-slate-200/80 shadow-xs flex flex-col h-[460px]">
                    <CardHeader className="py-3 px-4 border-b border-slate-100 flex flex-row items-center justify-between space-y-0">
                        <div>
                            <CardTitle className="text-sm font-extrabold text-slate-900 uppercase tracking-wider">
                                Événements Récents
                            </CardTitle>
                            <p className="text-[11px] text-slate-400 font-medium">
                                Les 10 derniers événements notables
                            </p>
                        </div>
                        <span className="text-[10px] font-bold bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">
                            Flux en direct
                        </span>
                    </CardHeader>
                    <CardContent className="p-0 overflow-y-auto flex-1 divide-y divide-slate-100">
                        {eventsList.map((event) => {
                            let iconColor = "text-blue-600 bg-blue-50";
                            let IconComponent = Rocket;

                            if (event.type === "gold") {
                                iconColor = "text-amber-600 bg-amber-50";
                                IconComponent = Star;
                            } else if (event.type === "fraud") {
                                iconColor = "text-rose-600 bg-rose-50";
                                IconComponent = ShieldAlert;
                            } else if (event.type === "promo") {
                                iconColor = "text-indigo-600 bg-indigo-50";
                                IconComponent = Tag;
                            }

                            return (
                                <div key={event.id} className="p-3.5 flex items-start justify-between gap-3 hover:bg-slate-50/50 transition-colors">
                                    <div className="flex items-start gap-2.5 min-w-0">
                                        <div className={`p-2 rounded-xl shrink-0 ${iconColor}`}>
                                            <IconComponent className="h-4 w-4" />
                                        </div>
                                        <div className="min-w-0">
                                            <p className="text-xs font-bold text-slate-800 leading-tight">
                                                {event.title}
                                            </p>
                                            <p className="text-[11px] text-slate-500 font-medium mt-0.5 truncate max-w-[280px] sm:max-w-md">
                                                {event.detail}
                                            </p>
                                        </div>
                                    </div>
                                    <div className="flex flex-col items-end gap-1.5 shrink-0">
                                        <span className="text-[9px] text-slate-400 font-semibold flex items-center gap-1">
                                            <Clock className="h-2.5 w-2.5" />
                                            {event.time}
                                        </span>
                                        <button 
                                            onClick={() => {
                                                toast.info(`Détail de l'événement: ${event.title}\n${event.detail}`);
                                            }}
                                            className="text-[9px] font-extrabold text-indigo-600 hover:text-indigo-800 flex items-center gap-0.5 cursor-pointer uppercase tracking-wider"
                                        >
                                            Détail
                                            <ChevronRight className="h-2.5 w-2.5" />
                                        </button>
                                    </div>
                                </div>
                            );
                        })}
                    </CardContent>
                </Card>

                {/* Right Panel: Interactive Chart */}
                <Card className="border-slate-200/80 shadow-xs flex flex-col h-[460px]">
                    <CardHeader className="py-3 px-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-2 space-y-0">
                        <div>
                            <CardTitle className="text-sm font-extrabold text-slate-900 uppercase tracking-wider">
                                Analyse du Programme
                            </CardTitle>
                            <p className="text-[11px] text-slate-400 font-medium">
                                Évolution des points et des transactions
                            </p>
                        </div>
                        
                        {/* Timeframe Toggle */}
                        <div className="flex bg-slate-100 p-0.5 rounded-lg border border-slate-200 shrink-0 self-start sm:self-auto">
                            {(["daily", "weekly", "monthly"] as const).map((mode) => (
                                <button
                                    key={mode}
                                    onClick={() => setTimeframe(mode)}
                                    className={`px-2.5 py-1 text-[10px] font-bold uppercase rounded-md transition-all ${
                                        timeframe === mode
                                            ? "bg-white text-indigo-600 shadow-2xs font-extrabold"
                                            : "text-slate-500 hover:text-slate-800"
                                    }`}
                                >
                                    {mode === "daily" ? "Jour" : mode === "weekly" ? "Semaine" : "Mois"}
                                </button>
                            ))}
                        </div>
                    </CardHeader>
                    <CardContent className="p-4 flex-1 flex flex-col min-h-0">
                        {/* Legend */}
                        <div className="flex flex-wrap items-center gap-4 mb-4 text-[10px] font-bold text-slate-500">
                            <div className="flex items-center gap-1.5">
                                <span className="w-2.5 h-1 bg-indigo-500 rounded-full" />
                                <span>Points distribués</span>
                            </div>
                            <div className="flex items-center gap-1.5">
                                <span className="w-2.5 h-1 bg-emerald-500 rounded-full" />
                                <span>Points dépensés</span>
                            </div>
                            <div className="flex items-center gap-1.5">
                                <span className="w-2.5 h-1 bg-fuchsia-500 rounded-full" />
                                <span>Transactions wallet</span>
                            </div>
                        </div>

                        {/* Chart Render */}
                        <div className="flex-1 w-full min-h-0">
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={activeChartData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                    <XAxis 
                                        dataKey="name" 
                                        axisLine={false} 
                                        tickLine={false} 
                                        tick={{ fill: '#64748b', fontSize: 10, fontWeight: 600 }}
                                    />
                                    <YAxis 
                                        axisLine={false} 
                                        tickLine={false} 
                                        tick={{ fill: '#64748b', fontSize: 10, fontWeight: 600 }}
                                    />
                                    <Tooltip 
                                        contentStyle={{ 
                                            borderRadius: '12px', 
                                            border: '1px solid #e2e8f0', 
                                            boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                                            fontSize: '11px',
                                            fontWeight: '500'
                                        }}
                                    />
                                    <Line 
                                        type="monotone" 
                                        dataKey="distrib" 
                                        stroke="#6366f1" 
                                        strokeWidth={3} 
                                        dot={{ r: 3, fill: '#6366f1', strokeWidth: 1.5, stroke: '#fff' }}
                                        activeDot={{ r: 5, strokeWidth: 0 }}
                                    />
                                    <Line 
                                        type="monotone" 
                                        dataKey="spent" 
                                        stroke="#10b981" 
                                        strokeWidth={3} 
                                        dot={{ r: 3, fill: '#10b981', strokeWidth: 1.5, stroke: '#fff' }}
                                        activeDot={{ r: 5, strokeWidth: 0 }}
                                    />
                                    <Line 
                                        type="monotone" 
                                        dataKey="tx" 
                                        stroke="#d946ef" 
                                        strokeWidth={2} 
                                        strokeDasharray="4 4"
                                        dot={{ r: 2.5, fill: '#d946ef', strokeWidth: 1, stroke: '#fff' }}
                                        activeDot={{ r: 4, strokeWidth: 0 }}
                                    />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* 5. QUICK ACTIONS BOTTOM PANEL */}
            <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs space-y-4">
                <div>
                    <h3 className="text-xs font-black text-slate-800 uppercase tracking-widest">
                        Actions Rapides
                    </h3>
                    <p className="text-xs text-slate-400 font-semibold mt-0.5">
                        Exécutez des opérations administratives instantanées.
                    </p>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    
                    {/* Action 1: Créer une règle */}
                    <button 
                        onClick={() => setIsRuleOpen(true)}
                        className="flex items-center justify-between p-4 bg-slate-50 hover:bg-indigo-50/50 hover:border-indigo-200 border border-slate-200 rounded-xl transition-all text-left group cursor-pointer"
                    >
                        <div className="flex items-center gap-3">
                            <div className="p-2.5 bg-white text-indigo-600 rounded-lg shadow-2xs group-hover:bg-indigo-100 transition-colors">
                                <Plus className="h-4.5 w-4.5" />
                            </div>
                            <div>
                                <p className="text-xs font-black text-slate-900 uppercase tracking-wider">Créer une Règle</p>
                                <p className="text-[10px] text-slate-400 font-semibold mt-0.5">Règles de gain de points</p>
                            </div>
                        </div>
                        <ChevronRight className="h-4 w-4 text-slate-400 group-hover:translate-x-1 transition-transform" />
                    </button>

                    {/* Action 2: Lancer une campagne */}
                    <button 
                        onClick={() => setIsCampaignOpen(true)}
                        className="flex items-center justify-between p-4 bg-slate-50 hover:bg-indigo-50/50 hover:border-indigo-200 border border-slate-200 rounded-xl transition-all text-left group cursor-pointer"
                    >
                        <div className="flex items-center gap-3">
                            <div className="p-2.5 bg-white text-indigo-600 rounded-lg shadow-2xs group-hover:bg-indigo-100 transition-colors">
                                <Rocket className="h-4.5 w-4.5" />
                            </div>
                            <div>
                                <p className="text-xs font-black text-slate-900 uppercase tracking-wider">Lancer Campagne</p>
                                <p className="text-[10px] text-slate-400 font-semibold mt-0.5">Campagnes temporaires</p>
                            </div>
                        </div>
                        <ChevronRight className="h-4 w-4 text-slate-400 group-hover:translate-x-1 transition-transform" />
                    </button>

                    {/* Action 3: Ajouter une récompense */}
                    <button 
                        onClick={() => setIsRewardOpen(true)}
                        className="flex items-center justify-between p-4 bg-slate-50 hover:bg-indigo-50/50 hover:border-indigo-200 border border-slate-200 rounded-xl transition-all text-left group cursor-pointer"
                    >
                        <div className="flex items-center gap-3">
                            <div className="p-2.5 bg-white text-indigo-600 rounded-lg shadow-2xs group-hover:bg-indigo-100 transition-colors">
                                <Gift className="h-4.5 w-4.5" />
                            </div>
                            <div>
                                <p className="text-xs font-black text-slate-900 uppercase tracking-wider">Ajouter Récompense</p>
                                <p className="text-[10px] text-slate-400 font-semibold mt-0.5">Nouveaux cadeaux boutique</p>
                            </div>
                        </div>
                        <ChevronRight className="h-4 w-4 text-slate-400 group-hover:translate-x-1 transition-transform" />
                    </button>

                    {/* Action 4: Générer un code promo */}
                    <button 
                        onClick={() => setIsPromoOpen(true)}
                        className="flex items-center justify-between p-4 bg-slate-50 hover:bg-indigo-50/50 hover:border-indigo-200 border border-slate-200 rounded-xl transition-all text-left group cursor-pointer"
                    >
                        <div className="flex items-center gap-3">
                            <div className="p-2.5 bg-white text-indigo-600 rounded-lg shadow-2xs group-hover:bg-indigo-100 transition-colors">
                                <Tag className="h-4.5 w-4.5" />
                            </div>
                            <div>
                                <p className="text-xs font-black text-slate-900 uppercase tracking-wider">Générer Code Promo</p>
                                <p className="text-[10px] text-slate-400 font-semibold mt-0.5">Codes de réduction</p>
                            </div>
                        </div>
                        <ChevronRight className="h-4 w-4 text-slate-400 group-hover:translate-x-1 transition-transform" />
                    </button>

                </div>
            </div>

            {/* ==================================================== */}
            {/* DIALOG FORMS (INLINE MODALS) */}
            {/* ==================================================== */}

            {/* Dialog 1: Créer une règle */}
            <Dialog open={isRuleOpen} onOpenChange={setIsRuleOpen}>
                <DialogContent className="sm:max-w-[450px]">
                    <DialogHeader>
                        <DialogTitle className="text-base font-extrabold uppercase tracking-wide text-slate-900">
                            Créer une règle de fidélité
                        </DialogTitle>
                        <DialogDescription className="text-xs">
                            Configurez les critères d'attribution automatique de points.
                        </DialogDescription>
                    </DialogHeader>
                    <form onSubmit={handleCreateRule} className="space-y-4 py-2">
                        <div className="space-y-1.5">
                            <Label htmlFor="ruleName" className="text-xs font-bold text-slate-700">Nom de la règle</Label>
                            <Input 
                                id="ruleName" 
                                placeholder="ex: Points achat standard, Bonus premier achat" 
                                value={ruleName}
                                onChange={(e) => setRuleName(e.target.value)}
                                className="h-9 text-xs"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="ruleTrigger" className="text-xs font-bold text-slate-700">Déclencheur</Label>
                                <select 
                                    id="ruleTrigger"
                                    value={ruleTrigger}
                                    onChange={(e) => setRuleTrigger(e.target.value)}
                                    className="w-full h-9 rounded-lg border border-input bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                >
                                    <option value="purchase">Achat effectué</option>
                                    <option value="referral">Parrainage validé</option>
                                    <option value="birthday">Anniversaire membre</option>
                                    <option value="signup">Inscription</option>
                                </select>
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="rulePoints" className="text-xs font-bold text-slate-700">Points attribués</Label>
                                <Input 
                                    id="rulePoints" 
                                    type="number" 
                                    value={rulePoints}
                                    onChange={(e) => setRulePoints(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="ruleMinPurchase" className="text-xs font-bold text-slate-700">Montant d'achat minimal (XAF)</Label>
                            <Input 
                                id="ruleMinPurchase" 
                                type="number" 
                                value={ruleMinPurchase}
                                onChange={(e) => setRuleMinPurchase(e.target.value)}
                                className="h-9 text-xs"
                            />
                        </div>
                        <DialogFooter className="mt-6">
                            <DialogClose asChild>
                                <Button type="button" variant="outline" className="h-8 text-xs px-4">Annuler</Button>
                            </DialogClose>
                            <Button type="submit" className="h-8 bg-indigo-600 hover:bg-indigo-700 text-white text-xs px-4 font-bold">
                                Enregistrer la règle
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

            {/* Dialog 2: Lancer une campagne */}
            <Dialog open={isCampaignOpen} onOpenChange={setIsCampaignOpen}>
                <DialogContent className="sm:max-w-[450px]">
                    <DialogHeader>
                        <DialogTitle className="text-base font-extrabold uppercase tracking-wide text-slate-900">
                            Lancer une campagne de fidélité
                        </DialogTitle>
                        <DialogDescription className="text-xs">
                            Activez un multiplicateur temporaire de points pour booster l'engagement.
                        </DialogDescription>
                    </DialogHeader>
                    <form onSubmit={handleLaunchCampaign} className="space-y-4 py-2">
                        <div className="space-y-1.5">
                            <Label htmlFor="campName" className="text-xs font-bold text-slate-700">Nom de la campagne</Label>
                            <Input 
                                id="campName" 
                                placeholder="ex: Double Points Black Friday" 
                                value={campName}
                                onChange={(e) => setCampName(e.target.value)}
                                className="h-9 text-xs"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="campSegment" className="text-xs font-bold text-slate-700">Segment cible</Label>
                                <select 
                                    id="campSegment"
                                    value={campSegment}
                                    onChange={(e) => setCampSegment(e.target.value)}
                                    className="w-full h-9 rounded-lg border border-input bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                >
                                    <option value="all">Tous les membres</option>
                                    <option value="gold">Membres Gold uniquement</option>
                                    <option value="silver">Membres Silver uniquement</option>
                                    <option value="new">Nouveaux membres</option>
                                </select>
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="campMultiplier" className="text-xs font-bold text-slate-700">Multiplicateur de points</Label>
                                <Input 
                                    id="campMultiplier" 
                                    type="number" 
                                    step="0.1" 
                                    value={campMultiplier}
                                    onChange={(e) => setCampMultiplier(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="campStart" className="text-xs font-bold text-slate-700">Date de début</Label>
                                <Input 
                                    id="campStart" 
                                    type="date" 
                                    value={campStart}
                                    onChange={(e) => setCampStart(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="campEnd" className="text-xs font-bold text-slate-700">Date de fin</Label>
                                <Input 
                                    id="campEnd" 
                                    type="date" 
                                    value={campEnd}
                                    onChange={(e) => setCampEnd(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                        </div>
                        <DialogFooter className="mt-6">
                            <DialogClose asChild>
                                <Button type="button" variant="outline" className="h-8 text-xs px-4">Annuler</Button>
                            </DialogClose>
                            <Button type="submit" className="h-8 bg-indigo-600 hover:bg-indigo-700 text-white text-xs px-4 font-bold">
                                Activer la campagne
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

            {/* Dialog 3: Ajouter une récompense */}
            <Dialog open={isRewardOpen} onOpenChange={setIsRewardOpen}>
                <DialogContent className="sm:max-w-[450px]">
                    <DialogHeader>
                        <DialogTitle className="text-base font-extrabold uppercase tracking-wide text-slate-900">
                            Ajouter une récompense
                        </DialogTitle>
                        <DialogDescription className="text-xs">
                            Créez un nouvel article ou bon d'achat échangeable contre des points.
                        </DialogDescription>
                    </DialogHeader>
                    <form onSubmit={handleAddReward} className="space-y-4 py-2">
                        <div className="space-y-1.5">
                            <Label htmlFor="rewName" className="text-xs font-bold text-slate-700">Nom de la récompense / cadeau</Label>
                            <Input 
                                id="rewName" 
                                placeholder="ex: Bon d'achat 5000 XAF, T-Shirt Premium" 
                                value={rewName}
                                onChange={(e) => setRewName(e.target.value)}
                                className="h-9 text-xs"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="rewPoints" className="text-xs font-bold text-slate-700">Points requis</Label>
                                <Input 
                                    id="rewPoints" 
                                    type="number" 
                                    value={rewPoints}
                                    onChange={(e) => setRewPoints(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="rewCategory" className="text-xs font-bold text-slate-700">Catégorie</Label>
                                <select 
                                    id="rewCategory"
                                    value={rewCategory}
                                    onChange={(e) => setRewCategory(e.target.value)}
                                    className="w-full h-9 rounded-lg border border-input bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                >
                                    <option value="voucher">Bon d'achat numérique</option>
                                    <option value="product">Produit physique</option>
                                    <option value="experience">Expérience VIP</option>
                                </select>
                            </div>
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="rewStock" className="text-xs font-bold text-slate-700">Stock initial disponible</Label>
                            <Input 
                                id="rewStock" 
                                type="number" 
                                value={rewStock}
                                onChange={(e) => setRewStock(e.target.value)}
                                className="h-9 text-xs"
                                required
                            />
                        </div>
                        <DialogFooter className="mt-6">
                            <DialogClose asChild>
                                <Button type="button" variant="outline" className="h-8 text-xs px-4">Annuler</Button>
                            </DialogClose>
                            <Button type="submit" className="h-8 bg-indigo-600 hover:bg-indigo-700 text-white text-xs px-4 font-bold">
                                Créer la récompense
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

            {/* Dialog 4: Générer un code promo */}
            <Dialog open={isPromoOpen} onOpenChange={setIsPromoOpen}>
                <DialogContent className="sm:max-w-[450px]">
                    <DialogHeader>
                        <DialogTitle className="text-base font-extrabold uppercase tracking-wide text-slate-900">
                            Générer un code promo
                        </DialogTitle>
                        <DialogDescription className="text-xs">
                            Créez un code promo échangeable ou directement utilisable.
                        </DialogDescription>
                    </DialogHeader>
                    <form onSubmit={handleGeneratePromo} className="space-y-4 py-2">
                        <div className="space-y-1.5">
                            <Label htmlFor="promoCode" className="text-xs font-bold text-slate-700">Code promo (Texte)</Label>
                            <Input 
                                id="promoCode" 
                                placeholder="ex: SPRING50, GOLDLOYALTY" 
                                value={promoCode}
                                onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
                                className="h-9 text-xs font-black uppercase tracking-wider"
                                required
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="promoDiscountType" className="text-xs font-bold text-slate-700">Type de remise</Label>
                                <select 
                                    id="promoDiscountType"
                                    value={promoDiscountType}
                                    onChange={(e) => setPromoDiscountType(e.target.value)}
                                    className="w-full h-9 rounded-lg border border-input bg-white px-2.5 py-1 text-xs focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:border-transparent transition-all"
                                >
                                    <option value="percent">Pourcentage (%)</option>
                                    <option value="fixed">Montant fixe (XAF)</option>
                                </select>
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="promoDiscountVal" className="text-xs font-bold text-slate-700">Valeur de remise</Label>
                                <Input 
                                    id="promoDiscountVal" 
                                    type="number" 
                                    value={promoDiscountVal}
                                    onChange={(e) => setPromoDiscountVal(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="promoLimit" className="text-xs font-bold text-slate-700">Limite d'utilisation</Label>
                                <Input 
                                    id="promoLimit" 
                                    type="number" 
                                    value={promoLimit}
                                    onChange={(e) => setPromoLimit(e.target.value)}
                                    className="h-9 text-xs"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="promoCost" className="text-xs font-bold text-slate-700">Coût en points (facultatif)</Label>
                                <Input 
                                    id="promoCost" 
                                    type="number" 
                                    value={promoCost}
                                    onChange={(e) => setPromoCost(e.target.value)}
                                    className="h-9 text-xs"
                                />
                            </div>
                        </div>
                        <DialogFooter className="mt-6">
                            <DialogClose asChild>
                                <Button type="button" variant="outline" className="h-8 text-xs px-4">Annuler</Button>
                            </DialogClose>
                            <Button type="submit" className="h-8 bg-indigo-600 hover:bg-indigo-700 text-white text-xs px-4 font-bold">
                                Générer le code
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

        </div>
    );
}