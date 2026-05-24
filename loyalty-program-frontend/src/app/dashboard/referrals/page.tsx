"use client";

import { useState } from "react";
import { 
    Users, UserPlus, Gift, ShieldAlert, 
    ArrowUpRight, TrendingUp, Award, 
    Settings, Info, Save, ChevronRight,
    Trophy, MousePointerClick
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { toast } from "sonner";

export default function ReferralsPage() {
    const [isLoading, setIsLoading] = useState(false);

    const handleSaveConfig = () => {
        setIsLoading(true);
        setTimeout(() => {
            setIsLoading(false);
            toast.success("Configuration du parrainage mise à jour");
        }, 1000);
    };

    const topReferrers = [
        { id: 1, name: "Jean Dupont", referrals: 45, conversion: "78%", rewards: "4,500 pts" },
        { id: 2, name: "Marie Curie", referrals: 38, conversion: "82%", rewards: "3,800 pts" },
        { id: 3, name: "Paul Valéry", referrals: 29, conversion: "65%", rewards: "2,900 pts" },
        { id: 4, name: "Simone Veil", referrals: 22, conversion: "90%", rewards: "2,200 pts" },
        { id: 5, name: "Victor Hugo", referrals: 18, conversion: "55%", rewards: "1,800 pts" },
    ];

    return (
        <div className="space-y-8 pb-20">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Programme de parrainage</h1>
                    <p className="text-muted-foreground mt-2">Gérez et suivez le moteur de croissance organique de votre communauté.</p>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                
                {/* --- Left Column: Configuration --- */}
                <div className="lg:col-span-7 space-y-6">
                    <Card className="border-none shadow-xl bg-white overflow-hidden">
                        <CardHeader className="bg-zinc-900 text-white pb-8">
                            <div className="flex items-center gap-2 mb-2">
                                <Settings size={18} className="text-zinc-500" />
                                <Badge variant="outline" className="text-zinc-400 border-zinc-700 text-[10px] uppercase font-bold tracking-tighter">Paramètres</Badge>
                            </div>
                            <CardTitle className="text-2xl">Configuration du programme</CardTitle>
                            <CardDescription className="text-zinc-400">Définissez les récompenses et les conditions de validation.</CardDescription>
                        </CardHeader>
                        <CardContent className="p-8 space-y-8">
                            {/* Rewards Section */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                <div className="space-y-4">
                                    <div className="flex items-center gap-2">
                                        <div className="p-2 bg-purple-100 rounded-lg text-purple-600">
                                            <Award size={18} />
                                        </div>
                                        <Label className="text-base font-bold">Récompense Parrain</Label>
                                    </div>
                                    <div className="space-y-2">
                                        <Label className="text-xs text-zinc-500 uppercase font-bold">Bonus en points</Label>
                                        <Input type="number" placeholder="ex: 500" className="h-12 bg-zinc-50 border-zinc-200" />
                                        <p className="text-[10px] text-zinc-400">Crédité au parrain après conversion.</p>
                                    </div>
                                </div>
                                <div className="space-y-4">
                                    <div className="flex items-center gap-2">
                                        <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                                            <UserPlus size={18} />
                                        </div>
                                        <Label className="text-base font-bold">Récompense Filleul</Label>
                                    </div>
                                    <div className="space-y-2">
                                        <Label className="text-xs text-zinc-500 uppercase font-bold">Avantage immédiat</Label>
                                        <Input placeholder="ex: 200 pts + remise 10%" className="h-12 bg-zinc-50 border-zinc-200" />
                                        <p className="text-[10px] text-zinc-400">Offert au filleul dès son inscription.</p>
                                    </div>
                                </div>
                            </div>

                            <Separator />

                            {/* Conversion Section */}
                            <div className="space-y-6">
                                <div className="flex items-center gap-2">
                                    <div className="p-2 bg-green-100 rounded-lg text-green-600">
                                        <TrendingUp size={18} />
                                    </div>
                                    <Label className="text-base font-bold">Conditions de conversion</Label>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <Label className="text-xs text-zinc-500 font-bold uppercase tracking-widest">Événement déclencheur</Label>
                                        <select className="flex h-12 w-full rounded-md border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950 focus:ring-offset-2">
                                            <option>Premier achat complété</option>
                                            <option>Premier trajet effectué</option>
                                            <option>Inscription validée</option>
                                        </select>
                                    </div>
                                    <div className="space-y-2">
                                        <Label className="text-xs text-zinc-500 font-bold uppercase tracking-widest">Montant minimum</Label>
                                        <Input type="number" placeholder="20.00 €" className="h-12 bg-zinc-50 border-zinc-200" />
                                    </div>
                                    <div className="space-y-2">
                                        <Label className="text-xs text-zinc-500 font-bold uppercase tracking-widest">Délai max de conversion</Label>
                                        <Input placeholder="30 jours" className="h-12 bg-zinc-50 border-zinc-200" />
                                    </div>
                                </div>
                            </div>

                            <Separator />

                            {/* Anti-Fraud Section */}
                            <div className="bg-red-50/50 border border-red-100 rounded-2xl p-6 space-y-4">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <ShieldAlert className="text-red-600" size={20} />
                                        <Label className="text-base font-bold text-red-900">Politique Anti-Fraude</Label>
                                    </div>
                                    <Badge className="bg-red-100 text-red-700 border-none uppercase text-[10px] font-bold">Activée</Badge>
                                </div>
                                <div className="space-y-4">
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium text-red-800/70">Vérification de l'IP identique</span>
                                        <div className="w-10 h-5 bg-red-600 rounded-full p-1"><div className="w-3 h-3 bg-white rounded-full translate-x-5" /></div>
                                    </div>
                                    <div className="flex items-center justify-between">
                                        <span className="text-sm font-medium text-red-800/70">Validité email requise</span>
                                        <div className="w-10 h-5 bg-red-600 rounded-full p-1"><div className="w-3 h-3 bg-white rounded-full translate-x-5" /></div>
                                    </div>
                                </div>
                            </div>
                        </CardContent>
                        <CardFooter className="p-8 pt-0 flex justify-end">
                            <Button className="bg-zinc-900 hover:bg-zinc-800 font-bold px-8 h-12" onClick={handleSaveConfig} disabled={isLoading}>
                                {isLoading ? "Mise à jour..." : "Enregistrer la configuration"} 
                                <Save size={18} className="ml-2" />
                            </Button>
                        </CardFooter>
                    </Card>
                </div>

                {/* --- Right Column: Stats & Leaderboard --- */}
                <div className="lg:col-span-5 space-y-6">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <Card className="border-none shadow-md">
                            <CardContent className="p-6">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="p-2 bg-zinc-100 rounded-lg">
                                        <Users className="text-zinc-600" size={18} />
                                    </div>
                                    <Badge className="bg-zinc-100 text-zinc-600 border-none font-bold">+12%</Badge>
                                </div>
                                <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest mb-1">Parrainages Actifs</p>
                                <p className="text-3xl font-black">1,248</p>
                            </CardContent>
                        </Card>
                        <Card className="border-none shadow-md">
                            <CardContent className="p-6">
                                <div className="flex justify-between items-start mb-4">
                                    <div className="p-2 bg-green-100 rounded-lg">
                                        <MousePointerClick className="text-green-600" size={18} />
                                    </div>
                                    <Badge className="bg-green-100 text-green-600 border-none font-bold">Stable</Badge>
                                </div>
                                <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest mb-1">Taux de conversion</p>
                                <p className="text-3xl font-black">24.5%</p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card className="border-none shadow-md bg-zinc-50 border border-zinc-100">
                        <CardContent className="p-6 flex items-center justify-between">
                            <div>
                                <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest mb-1">Récompenses distribuées (Mai)</p>
                                <p className="text-2xl font-black text-zinc-900 underline decoration-purple-500 decoration-4 underline-offset-4">42,500 pts</p>
                            </div>
                            <div className="h-12 w-12 rounded-full border-2 border-zinc-200 border-t-purple-600 flex items-center justify-center text-[10px] font-black italic">84%</div>
                        </CardContent>
                    </Card>

                    <Card className="border-none shadow-xl">
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-6">
                            <div>
                                <CardTitle className="text-lg">Meilleurs parraineurs</CardTitle>
                                <CardDescription className="text-xs">Classement des membres les plus actifs.</CardDescription>
                            </div>
                            <Trophy className="text-amber-500" size={24} />
                        </CardHeader>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader>
                                    <TableRow className="hover:bg-transparent border-zinc-100">
                                        <TableHead className="pl-6 font-bold text-[10px] uppercase">Membre</TableHead>
                                        <TableHead className="text-right font-bold text-[10px] uppercase">Invitations</TableHead>
                                        <TableHead className="text-right pr-6 font-bold text-[10px] uppercase">Récompenses</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {topReferrers.map((user, idx) => (
                                        <TableRow key={user.id} className="group border-zinc-50">
                                            <TableCell className="pl-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <span className={`text-[10px] font-black ${idx < 3 ? 'text-amber-600' : 'text-zinc-300'}`}>#0{idx + 1}</span>
                                                    <span className="font-bold text-sm text-zinc-700 group-hover:text-zinc-950 transition-colors">{user.name}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <Badge variant="secondary" className="bg-zinc-100 text-zinc-600 font-bold border-none">{user.referrals}</Badge>
                                            </TableCell>
                                            <TableCell className="text-right pr-6">
                                                <span className="text-sm font-bold text-green-600">{user.rewards}</span>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </CardContent>
                        <CardFooter className="p-6 justify-center bg-zinc-50/50">
                            <Button variant="ghost" className="text-zinc-500 text-xs font-bold hover:bg-zinc-100 group">
                                VOIR LE CLASSEMENT COMPLET <ChevronRight size={14} className="ml-1 group-hover:translate-x-1 transition-transform" />
                            </Button>
                        </CardFooter>
                    </Card>
                </div>
            </div>
        </div>
    );
}

function Separator() {
    return <div className="h-[1px] w-full bg-zinc-100" />;
}
