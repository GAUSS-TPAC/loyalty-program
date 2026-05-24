"use client";

import { useState, useEffect } from "react";
import {
    Award, Shield, Crown, Gem,
    CheckCircle2, Info, ArrowRight,
    Settings, Calendar, Clock, BarChart2,
    X, Plus, Trash2
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { Badge } from "@/components/ui/badge";
import {
    Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger, SheetFooter
} from "@/components/ui/sheet";
import { toast } from "sonner";

export default function TiersPage() {
    const [mounted, setMounted] = useState(false);
    const [editingTier, setEditingTier] = useState<any>(null);
    const [isSheetOpen, setIsSheetOpen] = useState(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    const tiers = [
        {
            id: "bronze",
            name: "Bronze",
            icon: Shield,
            threshold: "0 pts",
            multiplier: "1x",
            gradient: "from-slate-400 via-slate-500 to-slate-600",
            benefits: ["Points sur chaque achat", "Accès au catalogue", "Support standard"]
        },
        {
            id: "silver",
            name: "Silver",
            icon: Award,
            threshold: "5,000 pts",
            multiplier: "1.2x",
            gradient: "from-indigo-500 via-blue-600 to-indigo-700",
            benefits: ["-5% sur les trajets", "Points x1.2", "Accès prévente"]
        },
        {
            id: "gold",
            name: "Gold",
            icon: Crown,
            threshold: "15,000 pts",
            multiplier: "1.5x",
            gradient: "from-amber-400 via-orange-500 to-amber-600",
            benefits: ["-10% sur tout", "Points x1.5", "Support prioritaire", "Cadeau d'anniversaire"]
        },
        {
            id: "platinum",
            name: "Platinum",
            icon: Gem,
            threshold: "50,000 pts",
            multiplier: "2x",
            gradient: "from-purple-500 via-fuchsia-600 to-indigo-700",
            benefits: ["-20% VIP", "Points x2", "Conciergerie", "Accès Salons VIP"]
        }
    ];

    const distributionData = [
        { name: 'Bronze', value: 850, color: '#71717a' },
        { name: 'Silver', value: 320, color: '#94a3b8' },
        { name: 'Gold', value: 125, color: '#d97706' },
        { name: 'Platinum', value: 45, color: '#4f46e5' },
    ];

    const handleUpdatePolicy = () => {
        toast.success("Politique de maintien mise à jour avec succès");
    };

    const handleSaveTier = () => {
        toast.success(`Le palier ${editingTier?.name} a été modifié`);
        setIsSheetOpen(false);
    };

    return (
        <div className="space-y-12 pb-20">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Configuration des paliers</h1>
                    <p className="text-muted-foreground mt-2">Définissez les niveaux de statut et les avantages progressifs pour vos membres.</p>
                </div>
            </div>

            {/* Horizontal Tiers Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {tiers.map((tier, i) => (
                    <Card key={i} className="flex flex-col border-none shadow-lg overflow-hidden group hover:scale-[1.02] transition-transform duration-300">
                        <div className={`h-24 bg-gradient-to-br ${tier.gradient} p-6 flex items-center justify-between`}>
                            <tier.icon size={32} className="text-white/80" strokeWidth={1.5} />
                            <Badge className="bg-white/20 backdrop-blur-md text-white border-white/30 font-bold">
                                {tier.multiplier} Points
                            </Badge>
                        </div>
                        <CardHeader className="pt-6">
                            <CardTitle className="text-2xl font-bold">{tier.name}</CardTitle>
                            <CardDescription className="text-zinc-500 font-medium">Seuil : {tier.threshold}</CardDescription>
                        </CardHeader>
                        <CardContent className="flex-1">
                            <div className="space-y-3">
                                <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest">Avantages inclus</p>
                                <ul className="space-y-2">
                                    {tier.benefits.map((benefit, j) => (
                                        <li key={j} className="flex items-center gap-2 text-sm text-zinc-700">
                                            <CheckCircle2 size={14} className="text-green-500 flex-shrink-0" />
                                            {benefit}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </CardContent>
                        <CardFooter className="p-6 pt-0">
                            <Button
                                variant="outline"
                                className="w-full font-bold group-hover:bg-zinc-900 group-hover:text-white transition-colors"
                                onClick={() => {
                                    setEditingTier(tier);
                                    setIsSheetOpen(true);
                                }}
                            >
                                Modifier le palier
                            </Button>
                        </CardFooter>
                    </Card>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Maintenance Policy */}
                <Card className="border-zinc-200">
                    <CardHeader className="border-b bg-zinc-50/50">
                        <div className="flex items-center gap-2">
                            <Settings className="text-zinc-500" size={20} />
                            <CardTitle className="text-lg">Politique de maintien</CardTitle>
                        </div>
                        <CardDescription>Règles de conservation du statut après la période de validité.</CardDescription>
                    </CardHeader>
                    <CardContent className="p-8 space-y-6">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    <BarChart2 size={14} className="text-zinc-400" />
                                    Points requis par période
                                </Label>
                                <Input type="number" placeholder="ex: 50%" />
                                <p className="text-[10px] text-zinc-500">% du seuil d'accès nécessaire pour rester.</p>
                            </div>
                            <div className="space-y-2">
                                <Label className="flex items-center gap-2">
                                    <Calendar size={14} className="text-zinc-400" />
                                    Période de référence
                                </Label>
                                <Input placeholder="12 mois" />
                                <p className="text-[10px] text-zinc-500">Fenêtre glissante de calcul.</p>
                            </div>
                        </div>
                        <div className="space-y-2">
                            <Label className="flex items-center gap-2">
                                <Clock size={14} className="text-zinc-400" />
                                Délai de grâce
                            </Label>
                            <Input placeholder="30 jours" />
                            <p className="text-[10px] text-zinc-500">Délai avant rétrogradation effective.</p>
                        </div>
                        <Button
                            className="w-full bg-zinc-900 hover:bg-zinc-800 font-bold"
                            onClick={() => {
                                console.log("Mise à jour de la politique...");
                                toast.success("Politique de maintien mise à jour avec succès", {
                                    description: "Les nouveaux paramètres seront appliqués au prochain cycle de calcul.",
                                });
                            }}
                        >
                            Mettre à jour la politique
                        </Button>
                    </CardContent>
                </Card>

                {/* Preview Distribution */}
                <Card className="border-zinc-200">
                    <CardHeader className="border-b bg-zinc-50/50">
                        <div className="flex items-center gap-2">
                            <Info className="text-zinc-500" size={20} />
                            <CardTitle className="text-lg">Prévisualisation</CardTitle>
                        </div>
                        <CardDescription>Répartition théorique de la base membre actuelle.</CardDescription>
                    </CardHeader>
                    <CardContent className="p-8">
                        {mounted ? (
                            <div className="flex flex-col md:flex-row items-center gap-8">
                                <div className="w-full md:w-1/2 min-w-0" style={{ height: 250 }}>
                                    <ResponsiveContainer width="100%" height="100%">
                                        <PieChart>
                                            <Pie
                                                data={distributionData}
                                                cx="50%"
                                                cy="50%"
                                                innerRadius={60}
                                                outerRadius={100}
                                                paddingAngle={5}
                                                dataKey="value"
                                            >
                                                {distributionData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={entry.color} />
                                                ))}
                                            </Pie>
                                            <Tooltip />
                                        </PieChart>
                                    </ResponsiveContainer>
                                </div>
                                <div className="w-full md:w-1/2 space-y-4">
                                    {distributionData.map((item, index) => (
                                        <div key={index} className="flex items-center justify-between p-3 rounded-xl bg-zinc-50 border border-zinc-100">
                                            <div className="flex items-center gap-3">
                                                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                                                <span className="font-bold text-sm">{item.name}</span>
                                            </div>
                                            <div className="text-right">
                                                <p className="font-bold text-sm">{item.value} membres</p>
                                                <p className="text-[10px] text-zinc-500">
                                                    {Math.round((item.value / 1340) * 100)}% de la base
                                                </p>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        ) : (
                            <div className="w-full h-64 flex items-center justify-center text-zinc-400">
                                Chargement de la prévisualisation...
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>

            {/* Editing Sheet */}
            <Sheet open={isSheetOpen} onOpenChange={setIsSheetOpen}>
                <SheetContent className="sm:max-w-xl">
                    <SheetHeader className="pb-6 border-b">
                        <SheetTitle className="text-2xl flex items-center gap-2">
                            {editingTier && <editingTier.icon size={24} />}
                            Modifier le palier {editingTier?.name}
                        </SheetTitle>
                        <SheetDescription>Ajustez les conditions d'accès et les récompenses.</SheetDescription>
                    </SheetHeader>

                    <div className="py-8 space-y-6">
                        <div className="space-y-4">
                            <Label className="text-sm font-bold uppercase tracking-widest text-zinc-400">Conditions</Label>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label>Seuil de points</Label>
                                    <Input defaultValue={editingTier?.threshold.replace(' pts', '')} type="number" />
                                </div>
                                <div className="space-y-2">
                                    <Label>Multiplicateur</Label>
                                    <Input defaultValue={editingTier?.multiplier.replace('x', '')} type="number" step="0.1" />
                                </div>
                            </div>
                        </div>

                        <div className="space-y-4 pt-4 border-t">
                            <div className="flex justify-between items-center">
                                <Label className="text-sm font-bold uppercase tracking-widest text-zinc-400">Avantages</Label>
                                <Button variant="ghost" size="sm" className="text-purple-600 font-bold h-8 text-[10px]">
                                    <Plus size={12} className="mr-1" /> AJOUTER UN AVANTAGE
                                </Button>
                            </div>
                            <div className="space-y-3">
                                {editingTier?.benefits.map((benefit: string, idx: number) => (
                                    <div key={idx} className="flex items-center gap-3 bg-zinc-50 p-3 rounded-xl border border-zinc-100 group">
                                        <div className="w-2 h-2 rounded-full bg-zinc-300" />
                                        <Input defaultValue={benefit} className="flex-1 bg-transparent border-none p-0 h-auto focus-visible:ring-0 shadow-none text-sm" />
                                        <Button variant="ghost" size="icon" className="h-8 w-8 text-zinc-300 hover:text-red-500 opacity-0 group-hover:opacity-100">
                                            <Trash2 size={14} />
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <SheetFooter className="border-t pt-6 gap-3 sm:gap-0">
                        <Button variant="outline" className="flex-1" onClick={() => setIsSheetOpen(false)}>Annuler</Button>
                        <Button className="flex-[2] bg-zinc-900 group" onClick={handleSaveTier}>
                            Enregistrer les modifications <ArrowRight size={16} className="ml-2 group-hover:translate-x-1 transition-transform" />
                        </Button>
                    </SheetFooter>
                </SheetContent>
            </Sheet>
        </div>
    );
}
