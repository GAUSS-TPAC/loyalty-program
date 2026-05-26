"use client";

import { useState, useMemo } from "react";
import { 
    Plus, Trash2, ChevronDown, ChevronUp, 
    Zap, Filter, Gift, ArrowRight,
    Save, X, Info
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { 
    DropdownMenu, DropdownMenuContent, DropdownMenuItem, 
    DropdownMenuTrigger 
} from "@/components/ui/dropdown-menu";
import { Separator } from "@/components/ui/separator";
import Link from "next/link";

export default function RuleBuilderPage() {
    const [trigger, setTrigger] = useState("Un achat est complété");
    const [conditions, setConditions] = useState([
        { metric: "Montant de l'achat", operator: "est supérieur ou égal à", value: "50" }
    ]);
    const [effects, setEffects] = useState([
        { type: "Offrir des points", value: "100" }
    ]);
    const [advancedOpen, setAdvancedOpen] = useState(false);

    const triggerOptions = [
        "Un achat est complété",
        "Un membre s'inscrit",
        "Un trajet est terminé",
        "Passage à un palier supérieur",
        "Anniversaire du membre"
    ];

    const metricOptions = [
        "Montant de l'achat",
        "Nombre d'achats cumulés",
        "Solde de points",
        "Palier du membre",
        "Heure de la journée"
    ];

    const effectOptions = [
        "Offrir des points",
        "Appliquer une remise",
        "Offrir un cadeau",
        "Changer de palier",
        "Envoyer une notification"
    ];

    // Live Summary Logic
    const summary = useMemo(() => {
        const condString = conditions.length > 0 
            ? ` si ${conditions.map(c => `${c.metric.toLowerCase()} ${c.operator} ${c.value}`).join(' et ')}`
            : "";
        const effectString = effects.length > 0
            ? `, le système va ${effects.map(e => `${e.type.toLowerCase()} (${e.value})`).join(' et ')}`
            : "";
        
        return `Quand ${trigger.toLowerCase()}${condString}${effectString}.`;
    }, [trigger, conditions, effects]);

    return (
        <div className="max-w-5xl mx-auto space-y-8 pb-20">
            {/* Header */}
            <div className="flex justify-between items-center bg-white p-6 rounded-2xl border border-zinc-200 sticky top-0 z-10 shadow-sm">
                <div className="flex items-center gap-4">
                    <Link href="/dashboard/rules">
                        <Button variant="ghost" size="icon"><X size={20} /></Button>
                    </Link>
                    <div>
                        <h1 className="text-xl font-bold">Nouveau Rule Builder</h1>
                        <p className="text-xs text-zinc-500 uppercase tracking-widest font-bold">Configuration de logique</p>
                    </div>
                </div>
                <div className="flex gap-3">
                    <Button variant="outline">Enregistrer en brouillon</Button>
                    <Button className="bg-purple-600 hover:bg-purple-700 font-semibold px-8">Activer la règle</Button>
                </div>
            </div>

            {/* Section: QUAND */}
            <div className="relative">
                <div className="absolute -left-4 top-0 bottom-0 w-1 bg-purple-200 rounded-full" />
                <div className="space-y-4">
                    <div className="flex items-center gap-2">
                        <Badge className="bg-purple-600 h-8 w-8 rounded-full p-0 flex items-center justify-center text-base">1</Badge>
                        <h2 className="text-xl font-bold uppercase tracking-tight text-purple-900">QUAND</h2>
                        <span className="text-zinc-400 font-medium">— L'événement déclencheur</span>
                    </div>
                    
                    <Card className="border-2 border-purple-50 shadow-sm">
                        <CardContent className="p-6">
                            <div className="space-y-6">
                                <div className="space-y-2">
                                    <label className="text-sm font-semibold text-zinc-700">Choisir un événement</label>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button variant="outline" className="w-full justify-between h-12 text-lg px-4 border-2">
                                                <div className="flex items-center gap-2">
                                                    <Zap className="text-purple-600" size={20} />
                                                    {trigger}
                                                </div>
                                                <ChevronDown size={20} />
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent className="w-[var(--radix-dropdown-menu-trigger-width)]">
                                            {triggerOptions.map(opt => (
                                                <DropdownMenuItem key={opt} onClick={() => setTrigger(opt)} className="h-10 text-base">
                                                    {opt}
                                                </DropdownMenuItem>
                                            ))}
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>

                                {trigger.includes("achat") && (
                                    <div className="grid grid-cols-2 gap-4 animate-in fade-in slide-in-from-left-2 transition-all">
                                        <div className="space-y-2">
                                            <label className="text-xs font-bold text-zinc-500">Catégorie de produit</label>
                                            <Input placeholder="Toutes les catégories" className="h-11" />
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-xs font-bold text-zinc-500">Montant minimum</label>
                                            <Input type="number" placeholder="0.00" className="h-11" />
                                        </div>
                                    </div>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Section: SI */}
            <div className="relative">
                <div className="absolute -left-4 top-0 bottom-0 w-1 bg-blue-200 rounded-full" />
                <div className="space-y-4">
                    <div className="flex items-center gap-2">
                        <Badge className="bg-blue-600 h-8 w-8 rounded-full p-0 flex items-center justify-center text-base">2</Badge>
                        <h2 className="text-xl font-bold uppercase tracking-tight text-blue-900">SI</h2>
                        <span className="text-zinc-400 font-medium">— Les conditions restrictives</span>
                    </div>

                    <Card className="border-2 border-blue-50 shadow-sm">
                        <CardContent className="p-6 space-y-4">
                            {conditions.map((cond, index) => (
                                <div key={index}>
                                    {index > 0 && (
                                        <div className="flex items-center gap-4 my-4">
                                            <Separator className="flex-1" />
                                            <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200 font-bold px-4 py-1">ET</Badge>
                                            <Separator className="flex-1" />
                                        </div>
                                    )}
                                    <div className="flex items-center gap-3">
                                        <DropdownMenu>
                                            <DropdownMenuTrigger asChild>
                                                <Button variant="outline" className="flex-1 h-11 justify-between">
                                                    {cond.metric} <ChevronDown size={14} />
                                                </Button>
                                            </DropdownMenuTrigger>
                                            <DropdownMenuContent>
                                                {metricOptions.map(m => (
                                                    <DropdownMenuItem key={m} onClick={() => {
                                                        const newC = [...conditions];
                                                        newC[index].metric = m;
                                                        setConditions(newC);
                                                    }}>{m}</DropdownMenuItem>
                                                ))}
                                            </DropdownMenuContent>
                                        </DropdownMenu>

                                        <DropdownMenu>
                                            <DropdownMenuTrigger asChild>
                                                <Button variant="outline" className="w-[200px] h-11 justify-between">
                                                    {cond.operator} <ChevronDown size={14} />
                                                </Button>
                                            </DropdownMenuTrigger>
                                            <DropdownMenuContent>
                                                <DropdownMenuItem onClick={() => {
                                                    const newC = [...conditions];
                                                    newC[index].operator = "est supérieur ou égal à";
                                                    setConditions(newC);
                                                }}>est supérieur ou égal à</DropdownMenuItem>
                                                <DropdownMenuItem onClick={() => {
                                                    const newC = [...conditions];
                                                    newC[index].operator = "est égal à";
                                                    setConditions(newC);
                                                }}>est égal à</DropdownMenuItem>
                                            </DropdownMenuContent>
                                        </DropdownMenu>

                                        <Input 
                                            className="w-[120px] h-11" 
                                            value={cond.value}
                                            onChange={(e) => {
                                                const newC = [...conditions];
                                                newC[index].value = e.target.value;
                                                setConditions(newC);
                                            }}
                                        />

                                        <Button variant="ghost" size="icon" onClick={() => setConditions(conditions.filter((_, i) => i !== index))}>
                                            <Trash2 size={18} className="text-zinc-400 hover:text-red-500" />
                                        </Button>
                                    </div>
                                </div>
                            ))}
                            <Button variant="ghost" className="text-blue-600 hover:bg-blue-50 font-semibold" onClick={() => setConditions([...conditions, { metric: "Solde de points", operator: "est supérieur ou égal à", value: "0" }])}>
                                <Plus size={18} className="mr-2" /> Ajouter une condition
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Section: ALORS */}
            <div className="relative">
                <div className="absolute -left-4 top-0 bottom-0 w-1 bg-green-200 rounded-full" />
                <div className="space-y-4">
                    <div className="flex items-center gap-2">
                        <Badge className="bg-green-600 h-8 w-8 rounded-full p-0 flex items-center justify-center text-base">3</Badge>
                        <h2 className="text-xl font-bold uppercase tracking-tight text-green-900">ALORS</h2>
                        <span className="text-zinc-400 font-medium">— Les effets produits</span>
                    </div>

                    <Card className="border-2 border-green-50 shadow-sm">
                        <CardContent className="p-6 space-y-4">
                            {effects.map((effect, index) => (
                                <div key={index} className="flex items-center gap-3">
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button variant="outline" className="flex-1 h-11 justify-between">
                                                {effect.type} <ChevronDown size={14} />
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent>
                                            {effectOptions.map(e => (
                                                <DropdownMenuItem key={e} onClick={() => {
                                                    const newE = [...effects];
                                                    newE[index].type = e;
                                                    setEffects(newE);
                                                }}>{e}</DropdownMenuItem>
                                            ))}
                                        </DropdownMenuContent>
                                    </DropdownMenu>

                                    <Input 
                                        className="w-[150px] h-11" 
                                        value={effect.value}
                                        onChange={(e) => {
                                            const newE = [...effects];
                                            newE[index].value = e.target.value;
                                            setEffects(newE);
                                        }}
                                    />

                                    <Button variant="ghost" size="icon" onClick={() => setEffects(effects.filter((_, i) => i !== index))}>
                                        <Trash2 size={18} className="text-zinc-400 hover:text-red-500" />
                                    </Button>
                                </div>
                            ))}
                            <Button variant="ghost" className="text-green-600 hover:bg-green-50 font-semibold" onClick={() => setEffects([...effects, { type: "Offrir des points", value: "0" }])}>
                                <Plus size={18} className="mr-2" /> Ajouter un effet
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Summary Sentence */}
            <div className="bg-zinc-900 text-white p-8 rounded-3xl shadow-2xl relative overflow-hidden group">
                <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                    <Info size={120} />
                </div>
                <div className="relative z-10 space-y-2">
                    <p className="text-zinc-400 text-xs font-bold uppercase tracking-widest flex items-center gap-2">
                        <ArrowRight size={14} className="text-purple-400" /> Résumé de la règle
                    </p>
                    <p className="text-2xl font-medium leading-relaxed italic">
                        "{summary}"
                    </p>
                </div>
            </div>

            {/* Advanced Settings */}
            <div className="space-y-4">
                <Button 
                    variant="ghost" 
                    className="w-full justify-between h-12 text-zinc-500 bg-zinc-50 border border-zinc-200"
                    onClick={() => setAdvancedOpen(!advancedOpen)}
                >
                    <span className="flex items-center gap-2 font-bold text-xs uppercase tracking-widest">
                        <Settings2 size={18} /> Configuration avancée
                    </span>
                    {advancedOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                </Button>

                {advancedOpen && (
                    <Card className="animate-in slide-in-from-bottom-5 duration-300">
                        <CardContent className="p-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="space-y-2">
                                <label className="text-xs font-bold text-zinc-500 uppercase">Priorité</label>
                                <Input type="number" defaultValue="1" />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-bold text-zinc-500 uppercase">Période de validité</label>
                                <Input placeholder="Illimitée" />
                            </div>
                            <div className="space-y-2">
                                <label className="text-xs font-bold text-zinc-500 uppercase">Cumulable</label>
                                <div className="flex items-center gap-2 h-10">
                                    <div className="w-10 h-5 bg-purple-600 rounded-full p-1"><div className="w-3 h-3 bg-white rounded-full translate-x-5" /></div>
                                    <span className="text-sm font-medium">Oui</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                )}
            </div>
        </div>
    );
}

// Sub-component for icons used in imports (mocking the specific Settings2 which might be generic in my head but I used it above)
function Settings2({ size }: { size: number }) {
    return <Filter size={size} />;
}
