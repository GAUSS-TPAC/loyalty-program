// src/components/layout/Sidebar.tsx
"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
    LayoutDashboard, Users, Award, Gift, TrendingUp,
    Wallet, UserPlus, Settings, BarChart3, Sparkles
} from "lucide-react";

const navItems = [
    { name: "Vue d'ensemble", href: "/dashboard", icon: LayoutDashboard, color: "text-blue-500" },
    { name: "Gestion des membres", href: "/dashboard/members", icon: Users, color: "text-indigo-500" },
    { name: "Règles de fidélité", href: "/dashboard/rules", icon: Award, color: "text-purple-500" },
    { name: "Catalogue de récompenses", href: "/dashboard/rewards", icon: Gift, color: "text-fuchsia-500" },
    { name: "Campagnes & Codes Promo", href: "/dashboard/campaigns", icon: TrendingUp, color: "text-rose-500" },
    { name: "Paliers", href: "/dashboard/tiers", icon: BarChart3, color: "text-orange-500" },
    { name: "Parrainage", href: "/dashboard/referrals", icon: UserPlus, color: "text-emerald-500" },
    { name: "Wallet & Transactions", href: "/dashboard/wallet", icon: Wallet, color: "text-amber-500" },
    { name: "Analytics", href: "/dashboard/analytics", icon: BarChart3, color: "text-cyan-500" },
    { name: "Configuration", href: "/dashboard/settings", icon: Settings, color: "text-zinc-500" },
];

export function Sidebar() {
    const pathname = usePathname();

    return (
        <div className="w-72 bg-white border-r border-zinc-200 flex flex-col">
            <div className="p-6 border-b flex items-center gap-3">
                <div className="w-8 h-8 bg-gradient-to-br from-purple-600 to-indigo-600 rounded-lg flex items-center justify-center text-white">
                    <Sparkles size={18} />
                </div>
                <div>
                    <h1 className="text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-purple-700 to-indigo-700">Yowyob Loyalty</h1>
                    <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest">Admin Dashboard</p>
                </div>
            </div>

            <nav className="flex-1 p-4 space-y-1.5 overflow-auto">
                {navItems.map((item) => {
                    const isActive = pathname === item.href;
                    return (
                        <Link
                            key={item.href}
                            href={item.href}
                            className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-bold transition-all ${isActive
                                ? "bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-200"
                                : "text-zinc-600 hover:bg-zinc-50 hover:text-purple-600"
                                }`}
                        >
                            <item.icon size={18} className={isActive ? "text-white" : item.color} />
                            {item.name}
                        </Link>
                    );
                })}
            </nav>
        </div>
    );
}