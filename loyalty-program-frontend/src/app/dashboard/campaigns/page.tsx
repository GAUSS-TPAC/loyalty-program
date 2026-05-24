"use client";

import { useState, useMemo } from "react";
import {
    Plus, Search, Calendar, Zap,
    MoreVertical, Copy, TrendingUp,
    Gift, Timer, Users,
    ArrowRight, Check, X, Info,
    Percent, Ticket
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
    Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger, SheetFooter
} from "@/components/ui/sheet";
import { Label } from "@/components/ui/label";

export default function CampaignsPage() {
    const [campaignStep, setCampaignStep] = useState(1);
    const [multiplier, setMultiplier] = useState(2);
    const [isCampaignSheetOpen, setIsCampaignSheetOpen] = useState(false);
    const [isPromoSheetOpen, setIsPromoSheetOpen] = useState(false);

    // Mock Data for Campaigns
    const campaigns = [
        { id: 1, name: "Summer Boost 2026", type: "Multiplicateur", start: "2026-06-01", end: "2026-08-31", progress: 0, status: "scheduled" },
        { id: 2, name: "Anniversaire Yowyob", type: "Cadeau Direct", start: "2026-05-15", end: "2026-05-25", progress: 80, status: "active" },
        { id: 3, name: "Weekend Double Points", type: "Multiplicateur", start: "2026-05-01", end: "2026-05-05", progress: 100, status: "finished" },
    ];

    // Mock Data for Promo Codes
    const promoCodes = [
        { id: 1, name: "WELCOME50", code: "WELCOME50", type: "Fixe", value: "50 pts", uses: 450, total: 500, end: "2026-12-31" },
        { id: 2, name: "VIP20", code: "GOLDVIP20", type: "Pourcentage", value: "20%", uses: 85, total: 100, end: "2026-07-15" },
        { id: 3, name: "SUMMER26", code: "SUMMERLOVE", type: "Points", value: "200 pts", uses: 95, total: 100, end: "2026-06-30" },
    ];

    const getProgressColor = (percent: number) => {
        if (percent >= 90) return "bg-red-500";
        if (percent >= 75) return "bg-orange-500";
        return "bg-green-500";
    };

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Campagnes & Codes Promo</h1>
                    <p className="text-muted-foreground mt-2">Stimulez l'engagement avec des événements temporaires et des avantages exclusifs.</p>
                </div>
            </div>

            <Tabs defaultValue="campaigns" className="w-full">
                <TabsList className="grid w-full max-w-[450px] grid-cols-2 mb-8">
                    <TabsTrigger value="campaigns">Campagnes automatiques</TabsTrigger>
                    <TabsTrigger value="promos">Codes promo</TabsTrigger>
                </TabsList>

                {/* --- TAB: CAMPAIGNS --- */}
                <TabsContent value="campaigns" className="space-y-6">
                    <div className="flex justify-between items-center">
                        <div className="relative w-full max-w-sm">
                            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input placeholder="Rechercher une campagne..." className="pl-9" />
                        </div>

                        <Sheet open={isCampaignSheetOpen} onOpenChange={(open) => {
                            setIsCampaignSheetOpen(open);
                            if (!open) setCampaignStep(1);
                        }}>
                            <SheetTrigger asChild>
                                <Button className="bg-purple-600 hover:bg-purple-700">
                                    <Plus className="mr-2 h-4 w-4" /> Nouvelle campagne
                                </Button>
                            </SheetTrigger>
                            <SheetContent className="sm:max-w-xl">
                                <SheetHeader className="pb-6 border-b">
                                    <div className="flex items-center gap-2 mb-2">
                                        <Badge variant="outline" className="rounded-sm px-1 font-mono text-[10px]">Step {campaignStep}/2</Badge>
                                    </div>
                                    <SheetTitle className="text-2xl">Créer une campagne</SheetTitle>
                                    <SheetDescription>Configurez un événement de boost de fidélité.</SheetDescription>
                                </SheetHeader>

                                <div className="py-8">
                                    {campaignStep === 1 ? (
                                        <div className="space-y-8 animate-in fade-in slide-in-from-right-4">
                                            <div className="space-y-4">
                                                <Label className="text-base font-bold">Informations de base</Label>
                                                <div className="space-y-2">
                                                    <Label>Nom de la campagne</Label>
                                                    <Input placeholder="Ex: Soldes d'été 2026" />
                                                </div>
                                            </div>

                                            <div className="space-y-4">
                                                <Label className="text-base font-bold">Type de campagne</Label>
                                                <div className="grid grid-cols-2 gap-3">
                                                    <div className="border-2 border-purple-600 bg-purple-50 p-4 rounded-xl cursor-pointer">
                                                        <ZapIcon size={24} className="text-purple-600 mb-2" />
                                                        <p className="font-bold text-sm">Multiplicateur de points</p>
                                                        <p className="text-[10px] text-zinc-500 mt-1">Multipliez les points gagnés</p>
                                                    </div>
                                                    <div className="border-2 border-zinc-100 p-4 rounded-xl cursor-pointer hover:border-zinc-200">
                                                        <Gift size={24} className="text-zinc-400 mb-2" />
                                                        <p className="font-bold text-sm text-zinc-600">Points offerts</p>
                                                        <p className="text-[10px] text-zinc-500 mt-1">Bonus fixe à chaque achat</p>
                                                    </div>
                                                </div>
                                            </div>

                                            <div className="space-y-4">
                                                <Label className="text-base font-bold">Fenêtre temporelle</Label>
                                                <div className="grid grid-cols-2 gap-4">
                                                    <div className="space-y-2">
                                                        <Label className="text-xs">Date de début</Label>
                                                        <Input type="date" />
                                                    </div>
                                                    <div className="space-y-2">
                                                        <Label className="text-xs">Date de fin</Label>
                                                        <Input type="date" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="space-y-8 animate-in fade-in slide-in-from-right-4">
                                            <div className="space-y-4">
                                                <Label className="text-base font-bold">Configuration du boost</Label>
                                                <div className="space-y-6">
                                                    <div className="flex justify-between">
                                                        <Label>Multiplicateur : <span className="text-purple-700 font-bold text-lg">{multiplier}x</span></Label>
                                                    </div>
                                                    <input
                                                        type="range"
                                                        min="1" max="5" step="0.5"
                                                        value={multiplier}
                                                        onChange={(e) => setMultiplier(parseFloat(e.target.value))}
                                                        className="w-full h-2 bg-purple-100 rounded-lg appearance-none cursor-pointer accent-purple-600"
                                                    />
                                                    <div className="flex justify-between text-[10px] font-bold text-zinc-400 px-1">
                                                        <span>1x</span><span>1.5x</span><span>2x</span><span>2.5x</span><span>3x</span><span>3.5x</span><span>4x</span><span>4.5x</span><span>5x</span>
                                                    </div>
                                                </div>
                                            </div>

                                            <div className="bg-zinc-900 rounded-2xl p-6 text-white relative overflow-hidden">
                                                <TrendingUp className="absolute right-[-10px] bottom-[-10px] w-24 h-24 opacity-10" />
                                                <div className="relative z-10">
                                                    <p className="text-xs font-bold text-purple-400 uppercase tracking-widest mb-2">Visualisation de l'impact</p>
                                                    <div className="flex items-center gap-4">
                                                        <div className="text-center">
                                                            <p className="text-zinc-500 text-[10px]">SANS BOOST</p>
                                                            <p className="text-xl font-bold line-through opacity-50">100 pts</p>
                                                        </div>
                                                        <ArrowRight size={24} className="text-zinc-600" />
                                                        <div className="text-center bg-purple-600/20 px-4 py-1 rounded-xl border border-purple-500/30">
                                                            <p className="text-purple-300 text-[10px]">AVEC CAMPAGNE</p>
                                                            <p className="text-3xl font-black text-purple-400">{100 * multiplier} pts</p>
                                                        </div>
                                                    </div>
                                                    <p className="text-[11px] text-zinc-400 mt-4 leading-relaxed italic">
                                                        "Un membre gagnant habituellement 100 points recevra {100 * multiplier} points durant toute la durée de la campagne."
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>

                                <SheetFooter className="border-t pt-6">
                                    {campaignStep === 1 ? (
                                        <Button className="w-full bg-purple-600" onClick={() => setCampaignStep(2)}>
                                            Suivant : Paramètres <ArrowRight size={16} className="ml-2" />
                                        </Button>
                                    ) : (
                                        <div className="flex w-full gap-3">
                                            <Button variant="outline" className="flex-1" onClick={() => setCampaignStep(1)}>Précédent</Button>
                                            <Button className="flex-[2] bg-purple-600" onClick={() => setIsCampaignSheetOpen(false)}>Lancer la campagne</Button>
                                        </div>
                                    )}
                                </SheetFooter>
                            </SheetContent>
                        </Sheet>
                    </div>

                    <Card>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead className="w-[250px]">Campagne</TableHead>
                                        <TableHead>Type</TableHead>
                                        <TableHead className="w-[300px]">Progression du temps</TableHead>
                                        <TableHead>Statut</TableHead>
                                        <TableHead className="text-right">Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {campaigns.map((camp) => (
                                        <TableRow key={camp.id}>
                                            <TableCell className="font-bold">{camp.name}</TableCell>
                                            <TableCell>
                                                <Badge variant="secondary" className="font-normal">{camp.type}</Badge>
                                            </TableCell>
                                            <TableCell>
                                                <div className="space-y-1.5">
                                                    <div className="flex justify-between text-[10px] font-bold text-zinc-400 uppercase tracking-tight">
                                                        <span>{camp.start}</span>
                                                        <span>{camp.end}</span>
                                                    </div>
                                                    <div className="h-2 w-full bg-zinc-100 rounded-full overflow-hidden">
                                                        <div
                                                            className={`h-full rounded-full transition-all duration-700 ${camp.status === 'active' ? 'bg-purple-600' : 'bg-zinc-300'}`}
                                                            style={{ width: `${camp.progress}%` }}
                                                        />
                                                    </div>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {camp.status === 'scheduled' && <Badge className="bg-blue-100 text-blue-700 border-none">Programmée</Badge>}
                                                {camp.status === 'active' && <Badge className="bg-green-100 text-green-700 border-none flex w-fit items-center gap-1.5"><div className="w-1.5 h-1.5 bg-green-600 rounded-full animate-pulse" /> Active</Badge>}
                                                {camp.status === 'finished' && <Badge className="bg-zinc-100 text-zinc-500 border-none">Terminée</Badge>}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <Button variant="ghost" size="icon"><MoreVertical size={16} /></Button>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </CardContent>
                    </Card>
                </TabsContent>

                {/* --- TAB: PROMO CODES --- */}
                <TabsContent value="promos" className="space-y-6">
                    <div className="flex justify-between items-center">
                        <div className="relative w-full max-w-sm">
                            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input placeholder="Rechercher un code..." className="pl-9" />
                        </div>

                        <Sheet open={isPromoSheetOpen} onOpenChange={setIsPromoSheetOpen}>
                            <SheetTrigger asChild>
                                <Button className="bg-purple-600 hover:bg-purple-700">
                                    <Plus className="mr-2 h-4 w-4" /> Nouveau code
                                </Button>
                            </SheetTrigger>
                            <SheetContent className="sm:max-w-xl">
                                <SheetHeader className="pb-6 border-b">
                                    <SheetTitle className="text-2xl">Nouveau code promo</SheetTitle>
                                    <SheetDescription>Générez un code pour une distribution manuelle.</SheetDescription>
                                </SheetHeader>
                                <div className="py-8 space-y-6">
                                    <div className="space-y-2">
                                        <Label>Libellé interne</Label>
                                        <Input placeholder="Ex: Black Friday 2026" />
                                    </div>
                                    <div className="space-y-2">
                                        <div className="flex justify-between items-center">
                                            <Label>Code promo</Label>
                                            <Button variant="ghost" size="sm" className="text-[10px] h-6 text-purple-600 font-bold uppercase">Générer aléatoirement</Button>
                                        </div>
                                        <Input placeholder="PROMO2026" className="uppercase font-mono tracking-widest text-lg" />
                                    </div>
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <Label>Valeur du bonus</Label>
                                            <Input type="number" placeholder="200" />
                                        </div>
                                        <div className="space-y-2">
                                            <Label>Limite d'utilisations</Label>
                                            <Input type="number" placeholder="1000" />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Date d'expiration</Label>
                                        <Input type="date" />
                                    </div>
                                </div>
                                <SheetFooter className="border-t pt-6">
                                    <Button variant="outline" onClick={() => setIsPromoSheetOpen(false)}>Annuler</Button>
                                    <Button className="bg-purple-600 hover:bg-purple-700 px-8">Créer le code</Button>
                                </SheetFooter>
                            </SheetContent>
                        </Sheet>
                    </div>

                    <Card>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead className="w-[200px]">Nom</TableHead>
                                        <TableHead>Code</TableHead>
                                        <TableHead>Valeur</TableHead>
                                        <TableHead className="w-[250px]">Utilisations</TableHead>
                                        <TableHead>Fin de validité</TableHead>
                                        <TableHead className="text-right">Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {promoCodes.map((promo) => {
                                        const usagePercent = Math.round((promo.uses / promo.total) * 100);
                                        return (
                                            <TableRow key={promo.id}>
                                                <TableCell className="font-bold">{promo.name}</TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-2 group">
                                                        <code className="bg-zinc-100 px-2 py-1 rounded text-zinc-700 font-mono text-sm tracking-wider uppercase group-hover:bg-purple-100 transition-colors">
                                                            {promo.code}
                                                        </code>
                                                        <Button variant="ghost" size="icon" className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity">
                                                            <Copy size={12} className="text-zinc-500" />
                                                        </Button>
                                                    </div>
                                                </TableCell>
                                                <TableCell>
                                                    <Badge className="bg-purple-50 text-purple-700 border-purple-100 font-bold">{promo.value}</Badge>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="space-y-1.5">
                                                        <div className="flex justify-between text-[10px] font-bold text-zinc-500">
                                                            <span>{promo.uses} / {promo.total}</span>
                                                            <span className={usagePercent >= 90 ? "text-red-600" : ""}>{usagePercent}%</span>
                                                        </div>
                                                        <div className="h-2 w-full bg-zinc-100 rounded-full overflow-hidden">
                                                            <div
                                                                className={`h-full rounded-full transition-all duration-1000 ${getProgressColor(usagePercent)}`}
                                                                style={{ width: `${usagePercent}%` }}
                                                            />
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="text-zinc-500 text-sm">
                                                    {promo.end}
                                                </TableCell>
                                                <TableCell className="text-right">
                                                    <Button variant="ghost" size="icon"><MoreVertical size={16} /></Button>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}

// Sub-component mocks
function ZapIcon({ size, className }: { size: number, className?: string }) {
    return <Zap size={size} className={className} />;
}
