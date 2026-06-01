"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, Wallet, Star, Award, Users } from "lucide-react";

const navItems = [
    { label: "Accueil",    href: "/member",          icon: LayoutDashboard },
    { label: "Wallet",     href: "/member/wallet",    icon: Wallet },
    { label: "Points",     href: "/member/points",    icon: Star },
    { label: "Palier",     href: "/member/tier",      icon: Award },
    { label: "Parrainage", href: "/member/referral",  icon: Users },
];

export default function MemberLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();

    return (
        <div className="min-h-screen bg-slate-50 flex flex-col">
            {/* Top header */}
            <header className="bg-white border-b border-slate-200 px-4 py-3 flex items-center justify-between sticky top-0 z-30">
                <div className="flex items-center gap-2.5">
                    <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-purple-600 to-indigo-600 flex items-center justify-center">
                        <Star size={14} className="text-white" />
                    </div>
                    <span className="text-sm font-black text-slate-900 tracking-tight">
                        Yowyob <span className="text-indigo-600">Loyalty</span>
                    </span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-indigo-50 border-2 border-indigo-200 flex items-center justify-center text-xs font-extrabold text-indigo-700">
                        MN
                    </div>
                </div>
            </header>

            {/* Main content */}
            <main className="flex-1 overflow-auto pb-20">
                {children}
            </main>

            {/* Bottom navigation */}
            <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 z-30 safe-bottom">
                <div className="flex items-center justify-around px-2 py-2">
                    {navItems.map((item) => {
                        const isActive = pathname === item.href;
                        return (
                            <Link
                                key={item.href}
                                href={item.href}
                                className="flex flex-col items-center gap-0.5 px-3 py-1.5 rounded-xl transition-all min-w-0"
                            >
                                <div className={`p-1.5 rounded-lg transition-all ${
                                    isActive
                                        ? "bg-indigo-100 text-indigo-700"
                                        : "text-slate-400"
                                }`}>
                                    <item.icon size={20} />
                                </div>
                                <span className={`text-[10px] font-bold truncate ${
                                    isActive ? "text-indigo-700" : "text-slate-400"
                                }`}>
                                    {item.label}
                                </span>
                            </Link>
                        );
                    })}
                </div>
            </nav>
        </div>
    );
}
