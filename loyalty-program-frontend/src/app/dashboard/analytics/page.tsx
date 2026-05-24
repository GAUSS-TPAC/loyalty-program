"use client";

import React, { useState } from "react";
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  AreaChart, 
  Area, 
  PieChart, 
  Pie, 
  Cell,
  LineChart,
  Line,
  Legend
} from "recharts";
import { 
  Download, 
  Calendar, 
  Users, 
  Gift, 
  Wallet, 
  Share2, 
  ArrowUpRight, 
  ArrowDownRight, 
  Filter,
  MoreHorizontal,
  ChevronRight,
  TrendingUp,
  Clock,
  Zap
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger 
} from "@/components/ui/dropdown-menu";

// --- Mock Data ---

const MEMBER_EVOLUTION = [
  { name: "Jan", Bronze: 4000, Silver: 2400, Gold: 1200, Platinum: 400 },
  { name: "Feb", Bronze: 4500, Silver: 2800, Gold: 1400, Platinum: 500 },
  { name: "Mar", Bronze: 5200, Silver: 3200, Gold: 1800, Platinum: 700 },
  { name: "Apr", Bronze: 5800, Silver: 3800, Gold: 2300, Platinum: 900 },
  { name: "May", Bronze: 6500, Silver: 4400, Gold: 2900, Platinum: 1200 },
  { name: "Jun", Bronze: 7200, Silver: 5100, Gold: 3600, Platinum: 1600 },
];

const TRIGGERED_RULES = [
  { name: "Achat Direct", count: 12450, color: "#8b5cf6" },
  { name: "Parrainage", count: 3200, color: "#a855f7" },
  { name: "Anniversaire", count: 1850, color: "#d946ef" },
  { name: "Premier Achat", count: 1200, color: "#ec4899" },
  { name: "Visite Magasin", count: 850, color: "#f43f5e" },
];

const REWARDS_RANKING = [
  { name: "Bon d'achat 5000 XAF", count: 850, cost: 500000, color: "#3b82f6" },
  { name: "Pack Internet 20Go", count: 620, cost: 310000, color: "#60a5fa" },
  { name: "Réduction 20% Article", count: 430, cost: 215000, color: "#93c5fd" },
  { name: "Cadeau Surprise", count: 310, cost: 155000, color: "#bfdbfe" },
  { name: "Livraison Gratuite", count: 120, cost: 60000, color: "#dbeafe" },
];

const WALLET_SOURCES = [
  { name: "MTN", value: 45, color: "#fbbf24" },
  { name: "Orange", value: 30, color: "#f97316" },
  { name: "Stripe", value: 15, color: "#6366f1" },
  { name: "Loyalty", value: 7, color: "#a855f7" },
  { name: "Cashback", value: 3, color: "#ec4899" },
];

const WALLET_CIRCULATION = [
  { name: "Lun", balance: 12400000 },
  { name: "Mar", balance: 12600000 },
  { name: "Mer", balance: 12300000 },
  { name: "Jeu", balance: 12900000 },
  { name: "Ven", balance: 13500000 },
  { name: "Sam", balance: 14200000 },
  { name: "Dim", balance: 13800000 },
];

const REFERRAL_FUNNEL = [
  { step: "Liens Générés", count: 5000, rate: "100%" },
  { step: "Clics", count: 3200, rate: "64%" },
  { step: "Inscriptions", count: 850, rate: "26.5%" },
  { step: "Premier Achat", count: 310, rate: "36.4%" },
  { step: "Récompense Débloquée", count: 280, rate: "90.3%" },
];

// --- Sub-components ---

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white/90 backdrop-blur-md border border-zinc-200 p-3 rounded-xl shadow-xl">
        <p className="text-xs font-bold text-zinc-900 mb-2">{label}</p>
        <div className="space-y-1">
          {payload.map((entry: any, index: number) => (
            <div key={index} className="flex items-center gap-3 justify-between">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
                <span className="text-[10px] text-zinc-500 font-medium">{entry.name}</span>
              </div>
              <span className="text-[10px] font-black text-zinc-900">{entry.value.toLocaleString()}</span>
            </div>
          ))}
        </div>
      </div>
    );
  }
  return null;
};

export default function AnalyticsPage() {
  const [period, setPeriod] = useState("30d");

  return (
    <div className="flex flex-col gap-8 pb-12">
      {/* Header & Controls */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-zinc-900">Analytics Dashboard</h1>
          <p className="text-zinc-500 mt-1 font-medium">Analyse complète des performances de votre programme de fidélité.</p>
        </div>
        <div className="flex items-center gap-3">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" className="w-[200px] bg-white border-zinc-200 shadow-sm rounded-xl font-bold text-xs uppercase tracking-wider h-11 justify-between">
                <div className="flex items-center">
                  <Calendar className="mr-2 h-4 w-4 text-purple-600" />
                  {period === "7d" && "7 derniers jours"}
                  {period === "30d" && "30 derniers jours"}
                  {period === "90d" && "90 derniers jours"}
                  {period === "12m" && "12 derniers mois"}
                  {period === "custom" && "Personnalisé..."}
                </div>
                <Filter className="h-3 w-3 text-zinc-400" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-[200px] bg-white border-zinc-200 rounded-xl overflow-hidden p-1 shadow-2xl ring-1 ring-zinc-950/5">
              <DropdownMenuItem onClick={() => setPeriod("7d")} className="text-xs font-bold py-3 focus:bg-purple-50 cursor-pointer rounded-lg">7 derniers jours</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setPeriod("30d")} className="text-xs font-bold py-3 focus:bg-purple-50 cursor-pointer rounded-lg">30 derniers jours</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setPeriod("90d")} className="text-xs font-bold py-3 focus:bg-purple-50 cursor-pointer rounded-lg">90 derniers jours</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setPeriod("12m")} className="text-xs font-bold py-3 focus:bg-purple-50 cursor-pointer rounded-lg">12 derniers mois</DropdownMenuItem>
              <DropdownMenuItem onClick={() => setPeriod("custom")} className="text-xs font-bold py-3 focus:bg-purple-50 cursor-pointer rounded-lg italic text-zinc-400">Personnalisé...</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button className="h-11 px-6 bg-zinc-950 text-white rounded-xl font-black text-xs uppercase tracking-widest hover:bg-purple-700 transition-all shadow-lg active:scale-95">
            <Download className="mr-2 h-4 w-4" /> Export PDF
          </Button>
        </div>
      </div>

      {/* Quick Overview */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50 ring-1 ring-zinc-100 overflow-hidden relative">
          <div className="absolute right-0 top-0 w-24 h-24 bg-purple-100/30 rounded-bl-[100px] -mr-8 -mt-8" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Total Membres</CardTitle>
            <Users className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">24,582</div>
            <p className="text-[10px] text-green-600 font-bold mt-1 flex items-center gap-1">
              <ArrowUpRight size={12} /> +12% ce mois
            </p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50 ring-1 ring-zinc-100 overflow-hidden relative">
           <div className="absolute right-0 top-0 w-24 h-24 bg-blue-100/30 rounded-bl-[100px] -mr-8 -mt-8" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Rédemptions</CardTitle>
            <Gift className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">2,330</div>
            <p className="text-[10px] text-zinc-500 font-medium mt-1">Valeur: 12.5M pts</p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50 ring-1 ring-zinc-100 overflow-hidden relative">
           <div className="absolute right-0 top-0 w-24 h-24 bg-green-100/30 rounded-bl-[100px] -mr-8 -mt-8" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Wallet circulation</CardTitle>
            <Wallet className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">13.8M pts</div>
            <p className="text-[10px] text-zinc-500 font-medium mt-1">≈ 138,000,000 XAF</p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50 ring-1 ring-zinc-100 overflow-hidden relative">
           <div className="absolute right-0 top-0 w-24 h-24 bg-orange-100/30 rounded-bl-[100px] -mr-8 -mt-8" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Taux de Rétention</CardTitle>
            <TrendingUp className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">84.2%</div>
            <p className="text-[10px] text-zinc-500 font-medium mt-1">Churn: 12 mois moy.</p>
          </CardContent>
        </Card>
      </div>

      {/* --- Section: Fidélisation --- */}
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <div className="h-[2px] flex-1 bg-zinc-100" />
          <h2 className="text-sm font-black uppercase tracking-[0.3em] text-zinc-400">Section 01 — Fidélisation</h2>
          <div className="h-[2px] flex-1 bg-zinc-100" />
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          <Card className="lg:col-span-2 border-none shadow-xl bg-white rounded-3xl overflow-hidden">
            <CardHeader className="pb-0 pt-8 px-8">
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="text-xl font-black">Évolution des Members par Palier</CardTitle>
                  <CardDescription>Répartition historique de la base membre.</CardDescription>
                </div>
                <div className="flex gap-2">
                  {['Bronze', 'Silver', 'Gold', 'Platinum'].map((tier, idx) => (
                    <div key={tier} className="flex items-center gap-1.5 bg-zinc-50 px-2 py-1 rounded-full border border-zinc-100">
                      <div className={`w-2 h-2 rounded-full ${['bg-orange-400', 'bg-zinc-400', 'bg-yellow-400', 'bg-purple-400'][idx]}`} />
                      <span className="text-[9px] font-bold text-zinc-500">{tier}</span>
                    </div>
                  ))}
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-8">
              <div className="h-[300px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={MEMBER_EVOLUTION}>
                    <defs>
                      <linearGradient id="colorBronze" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#fbbf24" stopOpacity={0.8}/><stop offset="95%" stopColor="#fbbf24" stopOpacity={0}/></linearGradient>
                      <linearGradient id="colorSilver" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#94a3b8" stopOpacity={0.8}/><stop offset="95%" stopColor="#94a3b8" stopOpacity={0}/></linearGradient>
                      <linearGradient id="colorGold" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#facc15" stopOpacity={0.8}/><stop offset="95%" stopColor="#facc15" stopOpacity={0}/></linearGradient>
                      <linearGradient id="colorPlatinum" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#a855f7" stopOpacity={0.8}/><stop offset="95%" stopColor="#a855f7" stopOpacity={0}/></linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f4f4f5" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} dy={10} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} />
                    <Tooltip content={<CustomTooltip />} />
                    <Area type="monotone" dataKey="Bronze" stackId="1" stroke="#fbbf24" strokeWidth={3} fillOpacity={1} fill="url(#colorBronze)" />
                    <Area type="monotone" dataKey="Silver" stackId="1" stroke="#94a3b8" strokeWidth={3} fillOpacity={1} fill="url(#colorSilver)" />
                    <Area type="monotone" dataKey="Gold" stackId="1" stroke="#facc15" strokeWidth={3} fillOpacity={1} fill="url(#colorGold)" />
                    <Area type="monotone" dataKey="Platinum" stackId="1" stroke="#a855f7" strokeWidth={3} fillOpacity={1} fill="url(#colorPlatinum)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden flex flex-col">
            <CardHeader className="pt-8 px-8 flex-none">
              <CardTitle className="text-xl font-black">Top Règles Déclenchées</CardTitle>
              <CardDescription>Règles les plus actives.</CardDescription>
            </CardHeader>
            <CardContent className="px-8 pb-8 flex-1">
              <div className="space-y-6 mt-4">
                {TRIGGERED_RULES.map((rule, idx) => (
                  <div key={rule.name} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-black text-zinc-900 flex items-center gap-2">
                        <span className="text-zinc-300 font-mono text-[10px]">0{idx + 1}</span> {rule.name}
                      </span>
                      <span className="text-xs font-bold text-zinc-500">{rule.count.toLocaleString()} activations</span>
                    </div>
                    <div className="h-2 bg-zinc-50 rounded-full overflow-hidden">
                      <div 
                        className="h-full rounded-full transition-all duration-1000" 
                        style={{ 
                          width: `${(rule.count / TRIGGERED_RULES[0].count) * 100}%`,
                          backgroundColor: rule.color 
                        }} 
                      />
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
            <CardFooter className="bg-zinc-50 p-6 pt-6 border-t border-zinc-100 flex-none self-end w-full">
              <div className="flex items-center justify-between w-full">
                <div className="text-[10px] font-black uppercase text-zinc-400">Total mensuel: 19,550</div>
                <Button variant="ghost" className="h-7 text-[10px] font-black uppercase p-0 hover:bg-transparent">Détails <ChevronRight size={14}/></Button>
              </div>
            </CardFooter>
          </Card>
        </div>
      </div>

      {/* --- Section: Récompenses --- */}
      <div className="space-y-6 mt-4">
        <div className="flex items-center gap-4">
          <div className="h-[2px] flex-1 bg-zinc-100" />
          <h2 className="text-sm font-black uppercase tracking-[0.3em] text-zinc-400">Section 02 — Récompenses</h2>
          <div className="h-[2px] flex-1 bg-zinc-100" />
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
           <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
            <CardHeader className="pt-8 px-8">
              <CardTitle className="text-xl font-black">Coût Total des Rédemptions</CardTitle>
              <CardDescription>Volume de points réinjectés dans le catalogue mensuellement.</CardDescription>
            </CardHeader>
            <CardContent className="p-8">
              <div className="h-[250px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={MEMBER_EVOLUTION}>
                     <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f4f4f5" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} />
                    <Tooltip content={<CustomTooltip />} cursor={{fill: '#f8fafc'}} />
                    <Bar dataKey="Bronze" name="Coût (pts)" fill="#3b82f6" radius={[6, 6, 0, 0]} barSize={40} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>

          <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
            <CardHeader className="pt-8 px-8">
               <div className="flex items-center justify-between">
                <div>
                  <CardTitle className="text-xl font-black">Performance Rédemptions</CardTitle>
                  <CardDescription>Efficacité et engagement catalogue.</CardDescription>
                </div>
                <Badge className="bg-blue-50 text-blue-600 border-none font-bold">Catalogue Actif</Badge>
              </div>
            </CardHeader>
            <CardContent className="px-8 pb-8">
               <div className="grid grid-cols-2 gap-4 mt-4">
                  <div className="bg-zinc-50 p-4 rounded-2xl flex flex-col items-center justify-center text-center">
                    <Clock className="text-blue-500 mb-2" size={24} />
                    <span className="text-2xl font-black">4.2h</span>
                    <span className="text-[9px] font-black uppercase text-zinc-400 mt-1">Délai Attribution/Usage</span>
                  </div>
                  <div className="bg-zinc-50 p-4 rounded-2xl flex flex-col items-center justify-center text-center">
                    <Zap className="text-orange-500 mb-2" size={24} />
                    <span className="text-2xl font-black">1.8%</span>
                    <span className="text-[9px] font-black uppercase text-zinc-400 mt-1">Taux d'Expiration</span>
                  </div>
               </div>
               <div className="mt-8 space-y-4">
                  <span className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Classement Horizontal</span>
                  {REWARDS_RANKING.map(reward => (
                    <div key={reward.name} className="flex items-center gap-4">
                      <div className="flex-1 space-y-1">
                        <div className="flex justify-between">
                          <span className="text-[11px] font-bold">{reward.name}</span>
                          <span className="text-[11px] font-black">{reward.count}</span>
                        </div>
                        <div className="h-1.5 w-full bg-zinc-50 rounded-full overflow-hidden">
                          <div className="h-full bg-blue-500 rounded-full" style={{ width: `${(reward.count / REWARDS_RANKING[0].count) * 100}%` }} />
                        </div>
                      </div>
                    </div>
                  ))}
               </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* --- Section: Wallet --- */}
      <div className="space-y-6 mt-4">
        <div className="flex items-center gap-4">
          <div className="h-[2px] flex-1 bg-zinc-100" />
          <h2 className="text-sm font-black uppercase tracking-[0.3em] text-zinc-400">Section 03 — Economy & Wallet</h2>
          <div className="h-[2px] flex-1 bg-zinc-100" />
        </div>

        <div className="grid gap-6 lg:grid-cols-12">
          <Card className="lg:col-span-8 border-none shadow-xl bg-white rounded-3xl overflow-hidden">
            <CardHeader className="pt-8 px-8">
              <CardTitle className="text-xl font-black">Courbe des Soldes Cumulés</CardTitle>
              <CardDescription>Points totaux en circulation sur les 7 derniers jours.</CardDescription>
            </CardHeader>
            <CardContent className="p-8">
              <div className="h-[300px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={WALLET_CIRCULATION}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f4f4f5" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} dy={10} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fontWeight: 700, fill: '#a1a1aa' }} />
                    <Tooltip content={<CustomTooltip />} />
                    <Line type="monotone" dataKey="balance" stroke="#10b981" strokeWidth={4} dot={{ r: 6, fill: '#10b981', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 8, strokeWidth: 0 }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
            <CardFooter className="bg-zinc-50 p-6 flex justify-around">
               <div className="text-center">
                 <p className="text-[10px] font-black text-zinc-400 uppercase tracking-wider">Ticket Moyen</p>
                 <p className="text-xl font-black text-zinc-900">4,500 pts</p>
               </div>
               <div className="w-[1px] h-8 bg-zinc-200 mt-2" />
               <div className="text-center">
                 <p className="text-[10px] font-black text-zinc-400 uppercase tracking-wider">Usage Fréquence</p>
                 <p className="text-xl font-black text-zinc-900">3.4x / sem</p>
               </div>
            </CardFooter>
          </Card>

          <Card className="lg:col-span-4 border-none shadow-xl bg-white rounded-3xl overflow-hidden">
            <CardHeader className="pt-8 px-8">
              <CardTitle className="text-xl font-black">Sources de Crédit</CardTitle>
              <CardDescription>Ventilation par canal d'entrée.</CardDescription>
            </CardHeader>
            <CardContent className="p-0">
               <div className="h-[250px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={WALLET_SOURCES}
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {WALLET_SOURCES.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
               </div>
               <div className="px-8 pb-8 space-y-3">
                  {WALLET_SOURCES.map(source => (
                    <div key={source.name} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: source.color }} />
                        <span className="text-[11px] font-bold text-zinc-600">{source.name}</span>
                      </div>
                      <span className="text-[11px] font-black">{source.value}%</span>
                    </div>
                  ))}
               </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* --- Section: Parrainage --- */}
      <div className="space-y-6 mt-4">
        <div className="flex items-center gap-4">
          <div className="h-[2px] flex-1 bg-zinc-100" />
          <h2 className="text-sm font-black uppercase tracking-[0.3em] text-zinc-400">Section 04 — Parrainage & Growth</h2>
          <div className="h-[2px] flex-1 bg-zinc-100" />
        </div>

        <Card className="border-none shadow-xl bg-gradient-to-br from-zinc-900 to-black text-white rounded-[40px] overflow-hidden">
           <CardHeader className="p-10 pb-0">
             <div className="flex items-center gap-4">
               <div className="p-3 bg-purple-500/20 rounded-2xl">
                 <Share2 className="text-purple-400" />
               </div>
               <div>
                  <CardTitle className="text-2xl font-black">Entonnoir de Conversion Parrainage</CardTitle>
                  <CardDescription className="text-zinc-400">Visualisation de l'efficacité du programme ambassadeur.</CardDescription>
               </div>
             </div>
           </CardHeader>
           <CardContent className="p-10 pt-16">
              <div className="flex flex-col lg:flex-row items-center justify-between gap-8">
                 {REFERRAL_FUNNEL.map((step, idx) => (
                   <React.Fragment key={step.step}>
                     <div className="flex flex-col items-center text-center flex-1 max-w-[200px] group">
                        <div className="relative mb-4">
                           <div className="w-20 h-20 rounded-[30px] bg-white/5 border border-white/10 flex items-center justify-center transition-all group-hover:scale-110 group-hover:bg-purple-500/10 group-hover:border-purple-500/30">
                              <span className="text-2xl font-black text-white">{idx + 1}</span>
                           </div>
                           {idx < REFERRAL_FUNNEL.length - 1 && (
                             <div className="hidden lg:block absolute top-1/2 -right-12 -translate-y-1/2">
                               <ChevronRight className="text-zinc-700" size={24} />
                             </div>
                           )}
                        </div>
                        <h4 className="text-[10px] font-black uppercase tracking-widest text-zinc-500 mb-1">{step.step}</h4>
                        <p className="text-2xl font-black text-white">{step.count.toLocaleString()}</p>
                        <Badge className="mt-2 bg-purple-500 text-white border-none font-black text-[9px] px-2 py-0.5">{step.rate}</Badge>
                     </div>
                   </React.Fragment>
                 ))}
              </div>
           </CardContent>
           <CardFooter className="p-10 pt-0 flex justify-center">
              <div className="bg-white/5 border border-white/10 rounded-3xl p-6 flex flex-col md:flex-row items-center gap-10">
                 <div className="text-center">
                   <p className="text-[9px] font-black uppercase text-zinc-500 mb-1">CPA (Coût par acquisition)</p>
                   <p className="text-xl font-black text-white">850 pts / user</p>
                 </div>
                 <div className="w-[1px] h-10 bg-white/10 hidden md:block" />
                 <div className="text-center">
                   <p className="text-[9px] font-black uppercase text-zinc-500 mb-1">ROI Parrainage</p>
                   <p className="text-xl font-black text-green-400">4.2x</p>
                 </div>
                 <div className="w-[1px] h-10 bg-white/10 hidden md:block" />
                 <div className="text-center">
                   <p className="text-[9px] font-black uppercase text-zinc-500 mb-1">Virality Factor</p>
                   <p className="text-xl font-black text-purple-400">1.25</p>
                 </div>
              </div>
           </CardFooter>
        </Card>
      </div>
    </div>
  );
}
