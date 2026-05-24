"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { 
    Users, Award, TrendingUp, Wallet, UserPlus, Gift,
    ArrowUp, ArrowDown
} from "lucide-react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function HomeDashboard() {
    // Mock Data
    const metrics = [
        { 
            title: "Membres Actifs", 
            value: "1,248", 
            change: "+12.5%", 
            trend: "up", 
            icon: Users,
            color: "text-blue-600",
            bg: "bg-blue-50"
        },
        { 
            title: "Points Distribués", 
            value: "245,890", 
            change: "-3.2%", 
            trend: "down", 
            icon: Award,
            color: "text-purple-600",
            bg: "bg-purple-50"
        },
        { 
            title: "Taux de Rédemption", 
            value: "28.4%", 
            change: "+5.1%", 
            trend: "up", 
            icon: TrendingUp,
            color: "text-fuchsia-600",
            bg: "bg-fuchsia-50"
        },
        { 
            title: "Volume Wallet", 
            value: "8.4M XAF", 
            change: "+18.2%", 
            trend: "up", 
            icon: Wallet,
            color: "text-amber-600",
            bg: "bg-amber-50"
        },
        { 
            title: "Parrainages", 
            value: "87", 
            change: "+24.0%", 
            trend: "up", 
            icon: UserPlus,
            color: "text-emerald-600",
            bg: "bg-emerald-50"
        },
        { 
            title: "Récompenses", 
            value: "312", 
            change: "+14.8%", 
            trend: "up", 
            icon: Gift,
            color: "text-rose-600",
            bg: "bg-rose-50"
        },
    ];

    const chartData = [
        { day: 'Lun', points: 1200 },
        { day: 'Mar', points: 1900 },
        { day: 'Mer', points: 1500 },
        { day: 'Jeu', points: 2100 },
        { day: 'Ven', points: 1800 },
        { day: 'Sam', points: 2400 },
        { day: 'Dim', points: 2000 },
    ];

    return (
        <div className="space-y-8 pb-10">
            <div>
                <h1 className="text-3xl font-bold tracking-tight text-slate-900">Vue d'ensemble</h1>
                <p className="text-slate-500 mt-1 font-medium">Suivez les performances de votre programme Loyalty.</p>
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-6">
                {metrics.map((metric, i) => (
                    <Card key={i} className="border-slate-200/60 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
                        <div className={`h-1 ${metric.color.replace('text-', 'bg-')}`} />
                        <CardContent className="p-6">
                            <div className="flex items-center justify-between mb-4">
                                <div className={`p-2 rounded-lg ${metric.bg} ${metric.color}`}>
                                    <metric.icon size={20} />
                                </div>
                                <div className={`flex items-center text-xs font-bold ${
                                    metric.trend === 'up' ? 'text-emerald-600' : 'text-rose-600'
                                }`}>
                                    {metric.trend === 'up' ? <ArrowUp size={12} /> : <ArrowDown size={12} />}
                                    {metric.change}
                                </div>
                            </div>
                            <div className="space-y-1">
                                <p className="text-2xl font-bold text-slate-900 tracking-tight">{metric.value}</p>
                                <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">{metric.title}</p>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {/* Main Chart */}
            <Card className="border-slate-200/60 shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle className="text-lg font-bold">Distribution des Points</CardTitle>
                        <p className="text-xs text-slate-400 font-medium">Evolution hebdomadaire</p>
                    </div>
                </CardHeader>
                <CardContent className="pt-4">
                    <div className="h-[400px] w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis 
                                    dataKey="day" 
                                    axisLine={false} 
                                    tickLine={false} 
                                    tick={{ fill: '#94a3b8', fontSize: 12, fontWeight: 500 }}
                                />
                                <YAxis 
                                    axisLine={false} 
                                    tickLine={false} 
                                    tick={{ fill: '#94a3b8', fontSize: 12, fontWeight: 500 }}
                                />
                                <Tooltip 
                                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}
                                />
                                <Line 
                                    type="monotone" 
                                    dataKey="points" 
                                    stroke="#6366f1" 
                                    strokeWidth={4} 
                                    dot={{ r: 4, fill: '#6366f1', strokeWidth: 2, stroke: '#fff' }}
                                    activeDot={{ r: 6, strokeWidth: 0 }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}