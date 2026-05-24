
"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
    Search, Filter, Download, User, Calendar,
    ArrowUpRight
} from "lucide-react";

export default function MembersPage() {
    const [searchTerm, setSearchTerm] = useState("");
    const [showFilters, setShowFilters] = useState(false);

    // Mock data (we'll connect to real API later)
    const members = [
        {
            id: "1",
            name: "Marie Ndjomo",
            email: "marie.ndjomo@email.com",
            phone: "+237 690 123 456",
            tier: "Gold",
            points: 12450,
            walletBalance: 8750,
            walletStatus: "ACTIVE",
            joined: "2025-02-12",
            lastActive: "2026-05-23"
        },
        {
            id: "2",
            name: "Jean Dupont",
            email: "jean.dupont@email.com",
            phone: "+237 655 789 012",
            tier: "Silver",
            points: 3420,
            walletBalance: 1250,
            walletStatus: "FROZEN",
            joined: "2025-11-05",
            lastActive: "2026-05-20"
        },
        // Add more mock data as needed
    ];

    const getTierColor = (tier: string) => {
        switch (tier) {
            case "Platinum": return "bg-purple-100 text-purple-700";
            case "Gold": return "bg-yellow-100 text-yellow-700";
            case "Silver": return "bg-zinc-100 text-zinc-700";
            default: return "bg-orange-100 text-orange-700";
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold">Gestion des Membres</h1>
                    <p className="text-zinc-600">1,248 membres au total</p>
                </div>
                <Button className="flex items-center gap-2">
                    <Download size={18} />
                    Exporter CSV
                </Button>
            </div>

            {/* Search + Filters */}
            <Card className="p-4">
                <div className="flex gap-4">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-3 text-zinc-400" />
                        <Input
                            placeholder="Rechercher par nom, email, téléphone ou ID..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10"
                        />
                    </div>
                    <Button variant="outline" onClick={() => setShowFilters(!showFilters)}>
                        <Filter className="mr-2" /> Filtres
                    </Button>
                </div>
            </Card>

            {/* Members Table */}
            <Card>
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="border-b bg-zinc-50">
                                <th className="text-left p-4">Membre</th>
                                <th className="text-left p-4">Palier</th>
                                <th className="text-left p-4">Points</th>
                                <th className="text-left p-4">Wallet</th>
                                <th className="text-left p-4">Statut</th>
                                <th className="text-left p-4">Inscription</th>
                                <th className="text-left p-4">Dernière activité</th>
                                <th className="w-10"></th>
                            </tr>
                        </thead>
                        <tbody>
                            {members.map((member) => (
                                <tr
                                    key={member.id}
                                    className="border-b hover:bg-zinc-50 cursor-pointer"
                                    onClick={() => window.location.href = `/dashboard/members/${member.id}`}
                                >
                                    <td className="p-4">
                                        <div className="flex items-center gap-3">
                                            <div className="w-9 h-9 bg-zinc-200 rounded-full flex items-center justify-center">
                                                👤
                                            </div>
                                            <div>
                                                <p className="font-medium">{member.name}</p>
                                                <p className="text-sm text-zinc-500">{member.email}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="p-4">
                                        <Badge className={getTierColor(member.tier)}>
                                            {member.tier}
                                        </Badge>
                                    </td>
                                    <td className="p-4 font-semibold">{member.points.toLocaleString()}</td>
                                    <td className="p-4">{member.walletBalance} XAF</td>
                                    <td className="p-4">
                                        <Badge variant={member.walletStatus === "ACTIVE" ? "default" : "destructive"}>
                                            {member.walletStatus}
                                        </Badge>
                                    </td>
                                    <td className="p-4 text-sm text-zinc-500">{member.joined}</td>
                                    <td className="p-4 text-sm text-zinc-500">{member.lastActive}</td>
                                    <td className="p-4">
                                        <ArrowUpRight size={18} className="text-zinc-400" />
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}