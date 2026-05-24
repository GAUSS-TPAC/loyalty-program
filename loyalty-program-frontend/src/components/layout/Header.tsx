// src/components/layout/Header.tsx
"use client";

export function Header() {
    return (
        <header className="h-16 bg-white border-b border-zinc-200 px-6 flex items-center justify-between">
            <div className="flex items-center gap-4">
                <h2 className="font-semibold text-lg">Tableau de bord</h2>
            </div>

            <div className="flex items-center gap-4">
                <div className="text-sm text-right">
                    <p className="font-medium">Bienvenue, Admin</p>
                    <p className="text-zinc-500 text-xs">Entreprise XYZ</p>
                </div>
                <div className="w-9 h-9 bg-purple-200 rounded-full flex items-center justify-center text-purple-700 font-semibold">
                    A
                </div>
            </div>
        </header>
    );
}