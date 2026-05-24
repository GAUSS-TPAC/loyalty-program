"use client";

import { useState } from "react";
import { 
    Plus, Search, Filter, SlidersHorizontal, 
    Gift, Percent, Banknote, ShoppingBag, 
    Infinity, Image as ImageIcon, Check,
    MoreVertical, ArrowUpDown, ChevronDown
} from "lucide-react";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { 
    Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger, SheetFooter 
} from "@/components/ui/sheet";
import { 
    DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger, DropdownMenuSeparator 
} from "@/components/ui/dropdown-menu";

export default function RewardsPage() {
    const [isSheetOpen, setIsSheetOpen] = useState(false);
    const [rewardType, setRewardType] = useState("product");
    const [isUnlimited, setIsUnlimited] = useState(false);

    const rewards = [
        {
            id: 1,
            name: "Café de bienvenue",
            description: "Un expresso ou un café au lait offert dans tous nos points de vente.",
            type: "product",
            points: 50,
            stock: 120,
            status: "active",
            icon: ShoppingBag,
            redemptions: 450
        },
        {
            id: 2,
            name: "Réduction -15% sur ticket",
            description: "Sur votre prochain achat de trajet premium.",
            type: "percent",
            points: 250,
            stock: "illimité",
            status: "active",
            icon: Percent,
            redemptions: 890
        },
        {
            id: 3,
            name: "Crédit 1000 XAF",
            description: "Crédit ajouté directement à votre wallet Yowyob.",
            type: "wallet",
            points: 1000,
            stock: 50,
            status: "inactive",
            icon: Banknote,
            redemptions: 120
        },
        {
            id: 4,
            name: "Bon d'achat 5€",
            description: "Valable sur toute la boutique en ligne.",
            type: "fixed",
            points: 500,
            stock: 200,
            status: "active",
            icon: Banknote,
            redemptions: 230
        }
    ];

    const rewardTypes = [
        { id: "product", name: "Produit ou service offert", icon: ShoppingBag, desc: "Ex: Un café, un lavage auto..." },
        { id: "percent", name: "Réduction pourcentage", icon: Percent, desc: "Ex: -20% sur un achat" },
        { id: "fixed", name: "Réduction montant fixe", icon: Banknote, desc: "Ex: -10€ de remise" },
        { id: "wallet", name: "Crédit wallet", icon: Banknote, desc: "Ajout de solde direct" }
    ];

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Catalogue de récompenses</h1>
                    <p className="text-muted-foreground mt-2">Créez et gérez les avantages offerts à vos membres.</p>
                </div>

                <Sheet open={isSheetOpen} onOpenChange={setIsSheetOpen}>
                    <SheetTrigger asChild>
                        <Button className="bg-purple-600 hover:bg-purple-700">
                            <Plus className="mr-2 h-4 w-4" /> Ajouter une récompense
                        </Button>
                    </SheetTrigger>
                    <SheetContent className="sm:max-w-xl overflow-y-auto">
                        <SheetHeader className="pb-6 border-b">
                            <SheetTitle className="text-2xl">Nouvelle récompense</SheetTitle>
                            <SheetDescription>Configurez un nouvel avantage pour vos membres.</SheetDescription>
                        </SheetHeader>
                        
                        <div className="space-y-8 py-8">
                            <div className="space-y-4">
                                <Label className="text-base font-bold">Informations générales</Label>
                                <div className="space-y-2">
                                    <Label htmlFor="name">Nom de la récompense</Label>
                                    <Input id="name" placeholder="Ex: Café Premium gratuit" />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="desc">Description (vue par le membre)</Label>
                                    <Input id="desc" placeholder="Décrivez l'avantage en une phrase..." />
                                </div>
                            </div>

                            <div className="space-y-4">
                                <Label className="text-base font-bold">Type de récompense</Label>
                                <div className="grid grid-cols-2 gap-3">
                                    {rewardTypes.map((type) => (
                                        <div 
                                            key={type.id}
                                            onClick={() => setRewardType(type.id)}
                                            className={`p-4 rounded-xl border-2 cursor-pointer transition-all ${
                                                rewardType === type.id 
                                                ? "border-purple-600 bg-purple-50" 
                                                : "border-zinc-100 hover:border-zinc-200"
                                            }`}
                                        >
                                            <div className={`p-2 w-10 h-10 rounded-lg mb-3 flex items-center justify-center ${
                                                rewardType === type.id ? "bg-purple-600 text-white" : "bg-zinc-100 text-zinc-500"
                                            }`}>
                                                <type.icon size={20} />
                                            </div>
                                            <p className="font-bold text-sm leading-tight">{type.name}</p>
                                            <p className="text-[10px] text-zinc-500 mt-1">{type.desc}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <Label htmlFor="points">Coût en points</Label>
                                    <Input id="points" type="number" placeholder="500" />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="validity">Période de validité (jours)</Label>
                                    <Input id="validity" type="number" placeholder="30" />
                                </div>
                            </div>

                            <div className="space-y-4">
                                <div className="flex justify-between items-center">
                                    <Label className="text-sm font-semibold">Gestion du stock</Label>
                                    <div className="flex items-center gap-2">
                                        <div 
                                            onClick={() => setIsUnlimited(!isUnlimited)}
                                            className={`w-10 h-5 rounded-full p-1 cursor-pointer transition-all ${isUnlimited ? "bg-purple-600" : "bg-zinc-200"}`}
                                        >
                                            <div className={`w-3 h-3 bg-white rounded-full transition-all ${isUnlimited ? "translate-x-5" : "translate-x-0"}`} />
                                        </div>
                                        <span className="text-xs font-bold text-zinc-500 uppercase">Illimité</span>
                                    </div>
                                </div>
                                {!isUnlimited && (
                                    <Input type="number" placeholder="Quantité disponible" className="animate-in fade-in slide-in-from-top-2" />
                                )}
                            </div>

                            <div className="space-y-4">
                                <Label className="text-sm font-semibold">Visuel de la récompense</Label>
                                <div className="border-2 border-dashed border-zinc-200 rounded-2xl p-8 flex flex-col items-center justify-center text-zinc-500 hover:border-purple-300 hover:bg-purple-50 transition-all cursor-pointer">
                                    <ImageIcon size={40} className="mb-2" />
                                    <p className="text-sm font-medium">Cliquez pour uploader une image</p>
                                    <p className="text-xs">PNG, JPG jusqu'à 5MB (Déposé sur MinIO)</p>
                                </div>
                            </div>
                        </div>

                        <SheetFooter className="border-t pt-6">
                            <Button variant="outline" onClick={() => setIsSheetOpen(false)}>Annuler</Button>
                            <Button className="bg-purple-600 hover:bg-purple-700 font-bold px-8">Enregistrer la récompense</Button>
                        </SheetFooter>
                    </SheetContent>
                </Sheet>
            </div>

            {/* Filters Bar */}
            <div className="flex flex-col sm:flex-row gap-4 items-center justify-between bg-white p-4 rounded-2xl border border-zinc-200">
                <div className="flex flex-1 gap-2 w-full max-w-xl">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                        <Input placeholder="Rechercher une récompense..." className="pl-9 h-10" />
                    </div>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline" className="h-10">
                                <Filter className="mr-2 h-4 w-4" /> Type
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent>
                            <DropdownMenuItem>Produits</DropdownMenuItem>
                            <DropdownMenuItem>Réductions</DropdownMenuItem>
                            <DropdownMenuItem>Crédits</DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
                
                <div className="flex items-center gap-3">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-10 text-zinc-600">
                                <ArrowUpDown className="mr-2 h-4 w-4" /> Trier par : Popularité
                                <ChevronDown className="ml-2 h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem>Coût (Croissant)</DropdownMenuItem>
                            <DropdownMenuItem>Coût (Décroissant)</DropdownMenuItem>
                            <DropdownMenuItem>Popularité</DropdownMenuItem>
                            <DropdownMenuItem>Nouveauté</DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>

            {/* Gallery Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {rewards.map((reward) => (
                    <Card key={reward.id} className="group hover:shadow-xl transition-all duration-300 border-zinc-200 flex flex-col">
                        <CardHeader className="p-0 overflow-hidden rounded-t-xl relative">
                            <div className="aspect-[4/3] bg-zinc-100 flex items-center justify-center text-zinc-300 group-hover:scale-105 transition-transform duration-500">
                                <ImageIcon size={48} strokeWidth={1} />
                            </div>
                            <Badge className={`absolute top-3 right-3 ${reward.status === 'active' ? 'bg-green-500' : 'bg-zinc-400'}`}>
                                {reward.status === 'active' ? 'Actif' : 'Inactif'}
                            </Badge>
                            <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-sm p-2 rounded-xl text-purple-700 shadow-sm">
                                <reward.icon size={20} />
                            </div>
                        </CardHeader>
                        <CardContent className="p-5 flex-1">
                            <div className="flex justify-between items-start mb-2">
                                <CardTitle className="text-lg leading-tight">{reward.name}</CardTitle>
                                <Button variant="ghost" size="icon" className="h-8 w-8 -mr-2"><MoreVertical size={16} /></Button>
                            </div>
                            <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
                                {reward.description}
                            </p>
                            <div className="flex items-center gap-4 text-sm">
                                <div className="space-y-1">
                                    <p className="text-xs text-zinc-500 uppercase font-bold tracking-wider">Coût</p>
                                    <p className="font-bold text-purple-700 flex items-center gap-1">
                                        <Gift size={14} /> {reward.points} pts
                                    </p>
                                </div>
                                <div className="space-y-1">
                                    <p className="text-xs text-zinc-500 uppercase font-bold tracking-wider">Stock</p>
                                    <p className="font-medium text-zinc-700 flex items-center gap-1">
                                        {reward.stock === 'illimité' ? <Infinity size={14} className="text-zinc-400" /> : reward.stock}
                                        <span className="text-[10px] ml-1">{reward.stock === 'illimité' ? "" : "unités"}</span>
                                    </p>
                                </div>
                            </div>
                        </CardContent>
                        <CardFooter className="px-5 pb-5 pt-0">
                            <div className="w-full h-px bg-zinc-100 mb-4" />
                            <div className="flex items-center justify-between w-full">
                                <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-tighter">
                                    {reward.redemptions} rédemptions
                                </span>
                                <Button size="sm" variant="ghost" className="text-xs h-8 text-purple-600 hover:text-purple-700 hover:bg-purple-50 font-bold p-0">
                                    Modifier →
                                </Button>
                            </div>
                        </CardFooter>
                    </Card>
                ))}
            </div>
        </div>
    );
}

