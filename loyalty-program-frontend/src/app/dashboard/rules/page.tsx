"use client";

import { useState } from "react";
import { 
    Plus, Search, Filter, MoreVertical, 
    Play, Pause, Copy, Archive, 
    LayoutTemplate, SlidersHorizontal, 
    CreditCard, Coins, UserPlus, 
    CheckCircle2, ArrowLeft, ChevronRight
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import Link from "next/link";

export default function RulesPage() {
    const [view, setView] = useState("templates");
    const [configuringTemplate, setConfiguringTemplate] = useState<string | null>(null);

    const templates = [
        {
            id: "card",
            name: "Carte de fidélité",
            description: "Récompensez les clients après un nombre spécifique d'achats.",
            icon: CreditCard,
            color: "bg-blue-100 text-blue-600",
            params: [
                { label: "Nombre d'achats requis", placeholder: "ex: 10", type: "number" },
                { label: "Récompense offerte", placeholder: "ex: Un café gratuit", type: "text" }
            ]
        },
        {
            id: "points",
            name: "Programme de points",
            description: "Distribuez des points pour chaque euro dépensé.",
            icon: Coins,
            color: "bg-yellow-100 text-yellow-600",
            params: [
                { label: "Points par euro", placeholder: "ex: 5", type: "number" },
                { label: "Valeur d'un point", placeholder: "ex: 0.01 €", type: "text" }
            ]
        },
        {
            id: "welcome",
            name: "Bonus de bienvenue",
            description: "Offrez des points ou un cadeau lors de l'inscription.",
            icon: UserPlus,
            color: "bg-green-100 text-green-600",
            params: [
                { label: "Points offerts à l'inscription", placeholder: "ex: 100", type: "number" }
            ]
        }
    ];

    const rules = [
        { id: 1, name: "Offre Été 2026", status: "active", trigger: "Achat > 50€", triggersCount: 1245, created: "15/04/2026" },
        { id: 2, name: "Cashback Premium", status: "inactive", trigger: "Tout achat (Membres Gold)", triggersCount: 890, created: "02/05/2026" },
        { id: 3, name: "Double Points Weekend", status: "active", trigger: "Achat (Samedi - Dimanche)", triggersCount: 342, created: "20/05/2026" },
    ];

    return (
        <div className="space-y-8 max-w-7xl mx-auto">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Règles de fidélité</h1>
                    <p className="text-muted-foreground mt-2">Configurez la logique de récompense de votre programme.</p>
                </div>
                <Link href="/dashboard/rules/new">
                    <Button className="bg-purple-600 hover:bg-purple-700">
                        <Plus className="mr-2 h-4 w-4" /> Nouvelle règle
                    </Button>
                </Link>
            </div>

            <Tabs defaultValue="templates" onValueChange={setView} className="w-full">
                <TabsList className="grid w-full max-w-[400px] grid-cols-2 mb-8">
                    <TabsTrigger value="templates">Templates</TabsTrigger>
                    <TabsTrigger value="custom">Règles personnalisées</TabsTrigger>
                </TabsList>

                <TabsContent value="templates" className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {templates.map((template) => (
                            <Card key={template.id} className={`transition-all duration-300 overflow-hidden ${configuringTemplate === template.id ? 'ring-2 ring-purple-600' : ''}`}>
                                <CardHeader>
                                    <div className="flex items-center gap-4">
                                        <div className={`p-3 rounded-2xl ${template.color}`}>
                                            <template.icon size={24} />
                                        </div>
                                        <div>
                                            <CardTitle className="text-lg">{template.name}</CardTitle>
                                        </div>
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    {configuringTemplate === template.id ? (
                                        <div className="space-y-4 animate-in fade-in slide-in-from-top-1">
                                            {template.params.map((param, i) => (
                                                <div key={i} className="space-y-2">
                                                    <label className="text-sm font-medium">{param.label}</label>
                                                    <Input type={param.type} placeholder={param.placeholder} />
                                                </div>
                                            ))}
                                            <div className="flex gap-2 pt-4">
                                                <Button 
                                                    className="flex-1 bg-purple-600" 
                                                    onClick={() => setConfiguringTemplate(null)}
                                                >
                                                    Créer la règle
                                                </Button>
                                                <Button variant="outline" onClick={() => setConfiguringTemplate(null)}>Annuler</Button>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="space-y-4">
                                            <p className="text-sm text-muted-foreground min-h-[40px]">
                                                {template.description}
                                            </p>
                                            <Button 
                                                variant="outline" 
                                                className="w-full" 
                                                onClick={() => setConfiguringTemplate(template.id)}
                                            >
                                                Configurer
                                            </Button>
                                        </div>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </TabsContent>

                <TabsContent value="custom" className="space-y-4">
                    <Card>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead className="w-[300px]">Nom de la règle</TableHead>
                                        <TableHead>Statut</TableHead>
                                        <TableHead>Déclencheur</TableHead>
                                        <TableHead className="text-right">Déclenchements</TableHead>
                                        <TableHead>Date</TableHead>
                                        <TableHead className="text-right">Actions</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {rules.map((rule) => (
                                        <TableRow key={rule.id}>
                                            <TableCell className="font-medium">{rule.name}</TableCell>
                                            <TableCell>
                                                <div className="flex items-center gap-2">
                                                    <div className={`w-10 h-5 rounded-full p-1 cursor-pointer transition-colors ${rule.status === 'active' ? 'bg-purple-600' : 'bg-zinc-300'}`}>
                                                        <div className={`w-3 h-3 bg-white rounded-full transition-transform ${rule.status === 'active' ? 'translate-x-5' : 'translate-x-0'}`} />
                                                    </div>
                                                    <span className="text-xs uppercase font-bold text-zinc-500">
                                                        {rule.status === 'active' ? 'ON' : 'OFF'}
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="secondary" className="font-normal">
                                                    {rule.trigger}
                                                </Badge>
                                            </TableCell>
                                            <TableCell className="text-right font-mono">{rule.triggersCount}</TableCell>
                                            <TableCell className="text-zinc-500">{rule.created}</TableCell>
                                            <TableCell className="text-right">
                                                <div className="flex justify-end gap-1">
                                                    <Button variant="ghost" size="icon" className="h-8 w-8"><SlidersHorizontal size={16} /></Button>
                                                    <Button variant="ghost" size="icon" className="h-8 w-8"><Copy size={16} /></Button>
                                                    <Button variant="ghost" size="icon" className="h-8 w-8 text-red-500"><Archive size={16} /></Button>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}
