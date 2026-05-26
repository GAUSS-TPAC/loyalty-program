"use client";

import React, { useState } from "react";
import {
   Settings,
   Building2,
   Wallet,
   Bell,
   Zap,
   ShieldCheck,
   Globe,
   Mail,
   Phone,
   Upload,
   Key,
   Webhook,
   Plus,
   Trash2,
   Eye,
   Copy,
   ChevronRight,
   Info,
   Smartphone,
   Lock,
   Save,
   RefreshCw,
   MoreVertical,
   CheckCircle2
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
   Table,
   TableBody,
   TableCell,
   TableHead,
   TableHeader,
   TableRow
} from "@/components/ui/table";
import {
   DropdownMenu,
   DropdownMenuContent,
   DropdownMenuItem,
   DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

// --- Custom Components ---

const Toggle = ({ enabled, onChange, label }: { enabled: boolean, onChange: (val: boolean) => void, label: string }) => (
   <div className="flex items-center justify-between py-2">
      <span className="text-sm font-medium text-zinc-700">{label}</span>
      <button
         onClick={() => onChange(!enabled)}
         className={cn(
            "relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2",
            enabled ? "bg-purple-600" : "bg-zinc-200"
         )}
      >
         <span
            className={cn(
               "inline-block h-4 w-4 transform rounded-full bg-white transition-transform",
               enabled ? "translate-x-6" : "translate-x-1"
            )}
         />
      </button>
   </div>
);

const Chip = ({ label, onClick }: { label: string, onClick: () => void }) => (
   <button
      onClick={onClick}
      className="px-2 py-1 bg-zinc-100 hover:bg-purple-100 hover:text-purple-700 text-zinc-600 rounded-md text-[10px] font-black uppercase tracking-tight transition-all border border-zinc-200 border-dashed"
   >
      {`{${label}}`}
   </button>
);

// --- Settings Page ---

export default function TenantSettingsPage() {
   const [activeTab, setActiveTab] = useState("identity");
   const [tenantName, setTenantName] = useState("Yowyob Loyalty");

   const navItems = [
      { id: "identity", label: "Identité", icon: Building2, color: "text-indigo-600", bg: "bg-indigo-50", desc: "Branding" },
      { id: "wallet", label: "Wallet & Monnaie", icon: Wallet, color: "text-emerald-600", bg: "bg-emerald-50", desc: "Économie" },
      { id: "notifications", label: "Notifications", icon: Bell, color: "text-amber-600", bg: "bg-amber-50", desc: "Flux" },
      { id: "integrations", label: "Intégrations", icon: Zap, color: "text-purple-600", bg: "bg-purple-50", desc: "API" },
      { id: "security", label: "Sécurité", icon: ShieldCheck, color: "text-rose-600", bg: "bg-rose-50", desc: "Accès" },
   ];

   return (
      <div className="flex flex-col gap-6 pb-12">
         {/* Header Section */}
         <div className="bg-white p-8 rounded-[32px] shadow-sm border border-zinc-100 flex flex-col md:flex-row md:items-center justify-between gap-6 relative overflow-hidden">
            <div className="absolute right-0 top-0 w-64 h-64 bg-purple-500/5 rounded-full -mr-32 -mt-32 blur-3xl" />
            <div className="relative z-10">
               <h1 className="text-3xl font-black tracking-tight text-zinc-900">Configuration du Tenant</h1>
               <p className="text-zinc-500 mt-1 font-medium">Gérez les paramètres globaux de votre instance et votre image de marque.</p>
            </div>
            <div className="relative z-10 flex gap-3">
               <Button variant="outline" className="h-11 px-6 rounded-xl font-bold text-xs uppercase tracking-widest border-zinc-200 hover:bg-zinc-50">
                  <RefreshCw className="mr-2 h-4 w-4" /> Réinitialiser
               </Button>
               <Button className="h-11 px-8 bg-zinc-900 hover:bg-black text-white rounded-xl font-black text-xs uppercase tracking-widest shadow-xl shadow-zinc-950/10">
                  <Save className="mr-2 h-4 w-4" /> Enregistrer tout
               </Button>
            </div>
         </div>

         {/* Horizontal Secondary Navigation */}
         <div className="bg-zinc-50/50 p-1.5 rounded-2xl flex gap-1 overflow-x-auto no-scrollbar border border-zinc-100">
            {navItems.map((item) => {
               const isActive = activeTab === item.id;
               return (
                  <button
                     key={item.id}
                     onClick={() => setActiveTab(item.id)}
                     className={cn(
                        "flex items-center gap-3 px-6 py-3.5 rounded-xl transition-all duration-300 whitespace-nowrap border border-transparent",
                        isActive
                           ? `bg-white shadow-sm ring-1 ring-zinc-950/5 font-black uppercase text-[11px] tracking-widest ${item.color}`
                           : "text-zinc-500 hover:text-zinc-900 hover:bg-white/50 font-bold uppercase text-[11px] tracking-widest"
                     )}
                  >
                     <item.icon size={18} className={cn(
                        "transition-colors",
                        isActive ? item.color : "text-zinc-400"
                     )} />
                     {item.label}
                     {isActive && (
                        <Badge className={cn("ml-1 border-none text-[9px] px-1.5 h-4", item.bg, item.color)}>Actif</Badge>
                     )}
                  </button>
               );
            })}
         </div>

         {/* Content Area */}
         <main className="w-full animate-in fade-in slide-in-from-bottom-4 duration-500 mt-2">

            {/* --- Section: Identité --- */}
            {activeTab === "identity" && (
               <div className="space-y-6">
                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8 flex flex-row items-center justify-between">
                        <div>
                           <CardTitle className="text-xl font-black">Branding & Logo</CardTitle>
                           <CardDescription>Personnalisez l'apparence de votre dashboard pour vos administrateurs.</CardDescription>
                        </div>
                        <Button variant="outline" className="h-9 px-4 text-xs font-black uppercase rounded-xl">Prévisualiser</Button>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 space-y-8">
                        <div className="flex flex-col md:flex-row gap-12">
                           <div className="space-y-4">
                              <Label className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Logo du Tenant</Label>
                              <div className="w-32 h-32 rounded-3xl bg-zinc-50 border-2 border-dashed border-zinc-200 flex flex-col items-center justify-center gap-2 group hover:border-purple-300 hover:bg-purple-50 transition-all cursor-pointer">
                                 <Upload className="text-zinc-300 group-hover:text-purple-500" size={24} />
                                 <span className="text-[9px] font-bold text-zinc-400 group-hover:text-purple-700 uppercase">Uploader</span>
                              </div>
                           </div>
                           <div className="flex-1 space-y-6">
                              <div className="space-y-2">
                                 <Label className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Nom du Programme</Label>
                                 <Input value={tenantName} onChange={(e) => setTenantName(e.target.value)} className="bg-zinc-50 border-zinc-100 rounded-xl font-bold" />
                              </div>
                              <div className="space-y-3">
                                 <Label className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Couleurs Primaires</Label>
                                 <div className="flex gap-4">
                                    <div className="flex items-center gap-3 bg-zinc-50 border border-zinc-100 p-2 rounded-xl pr-4">
                                       <div className="w-8 h-8 rounded-lg bg-purple-600 shadow-lg shadow-purple-500/30" />
                                       <span className="text-xs font-bold font-mono">PURPLE</span>
                                    </div>
                                    <div className="flex items-center gap-3 bg-zinc-50 border border-zinc-100 p-2 rounded-xl pr-4">
                                       <div className="w-8 h-8 rounded-lg bg-zinc-900" />
                                       <span className="text-xs font-bold font-mono">BLACK</span>
                                    </div>
                                    <Button variant="ghost" size="icon" className="h-12 w-12 rounded-xl bg-zinc-100 text-zinc-500"><Plus size={20} /></Button>
                                 </div>
                              </div>
                           </div>
                        </div>
                     </CardContent>
                  </Card>

                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <CardTitle className="text-xl font-black">Informations Légales & Contact</CardTitle>
                        <CardDescription>Coordonnées affichées sur les factures et communications.</CardDescription>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase flex items-center gap-2"><Globe size={12} /> Site Web</Label>
                           <Input placeholder="https://votre-site.com" className="bg-zinc-50 border-zinc-100 rounded-xl" />
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase flex items-center gap-2"><Mail size={12} /> Email de Support</Label>
                           <Input placeholder="support@brand.com" className="bg-zinc-50 border-zinc-100 rounded-xl" />
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase flex items-center gap-2"><Phone size={12} /> Téléphone</Label>
                           <Input placeholder="+237 6xx xxx xxx" className="bg-zinc-50 border-zinc-100 rounded-xl" />
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase flex items-center gap-2">Adresse Siège</Label>
                           <Input placeholder="Rue, Ville, Pays" className="bg-zinc-50 border-zinc-100 rounded-xl" />
                        </div>
                     </CardContent>
                     <CardFooter className="bg-zinc-50 p-6 border-t border-zinc-100 flex justify-end gap-3">
                        <Button variant="ghost" className="font-bold text-xs uppercase tracking-widest text-zinc-500">Annuler</Button>
                        <Button className="bg-purple-600 hover:bg-purple-700 text-white px-8 font-black text-xs uppercase tracking-widest rounded-xl shadow-lg shadow-purple-500/20">
                           <Save className="mr-2 h-4 w-4" /> Enregistrer les modifications
                        </Button>
                     </CardFooter>
                  </Card>
               </div>
            )}

            {/* --- Section: Wallet --- */}
            {activeTab === "wallet" && (
               <div className="space-y-6">
                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <CardTitle className="text-xl font-black">Économie Virtuelle</CardTitle>
                        <CardDescription>Configurez comment vos membres perçoivent la valeur de leurs points.</CardDescription>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 space-y-8">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                           <div className="space-y-2">
                              <Label className="text-[10px] font-black uppercase text-zinc-400">Nom de la monnaie</Label>
                              <Input defaultValue="Loyalty Points" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold" />
                           </div>
                           <div className="space-y-2">
                              <Label className="text-[10px] font-black uppercase text-zinc-400">Symbole / Unité</Label>
                              <Input defaultValue="PTS" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold font-mono" />
                           </div>
                           <div className="space-y-2">
                              <Label className="text-[10px] font-black uppercase text-zinc-400">Taux de conversion (XAF)</Label>
                              <div className="relative">
                                 <Input defaultValue="10" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pl-8" />
                                 <span className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 text-xs font-bold">1 pt = </span>
                                 <span className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 text-[10px] font-black">XAF</span>
                              </div>
                           </div>
                        </div>

                        <Separator className="bg-zinc-100" />

                        <div className="space-y-6">
                           <h3 className="text-xs font-black uppercase tracking-widest text-zinc-900">Autorisations & Opérations</h3>
                           <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-2">
                              <Toggle enabled={true} onChange={() => { }} label="Autoriser les transferts entre membres" />
                              <Toggle enabled={true} onChange={() => { }} label="Conversion directe en cash/crédit" />
                              <Toggle enabled={false} onChange={() => { }} label="Permettre les soldes négatifs (Découvert)" />
                              <Toggle enabled={true} onChange={() => { }} label="Expédition automatique des points expirés" />
                           </div>
                        </div>
                     </CardContent>
                  </Card>

                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <CardTitle className="text-xl font-black">Wallet Policy & Limites</CardTitle>
                        <CardDescription>Prévention des abus et gestion des plafonds financiers.</CardDescription>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 grid grid-cols-1 md:grid-cols-2 gap-8">
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase text-zinc-400">Solde Maximum par Wallet</Label>
                           <div className="relative">
                              <Input defaultValue="1,000,000" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-12" />
                              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">PTS</span>
                           </div>
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase text-zinc-400">Montant Transaction Maximum</Label>
                           <div className="relative">
                              <Input defaultValue="50,000" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-12" />
                              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">PTS</span>
                           </div>
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase text-zinc-400">Nombre de transactions / Jour (Quota)</Label>
                           <div className="relative">
                              <Input defaultValue="10" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-12" />
                              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">TX</span>
                           </div>
                        </div>
                        <div className="space-y-2">
                           <Label className="text-[10px] font-black uppercase text-zinc-400">Durée de validité des points</Label>
                           <div className="relative">
                              <Input defaultValue="365" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-16" />
                              <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">JOURS</span>
                           </div>
                        </div>
                     </CardContent>
                  </Card>
               </div>
            )}

            {/* --- Section: Notifications --- */}
            {activeTab === "notifications" && (
               <div className="space-y-6">
                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <div className="flex items-center justify-between">
                           <div>
                              <CardTitle className="text-xl font-black">Templates de Messages</CardTitle>
                              <CardDescription>Éditez le contenu des messages automatiques envoyés aux membres.</CardDescription>
                           </div>
                           <Button className="bg-zinc-900 hover:bg-black text-white text-[10px] font-black uppercase tracking-widest h-10 px-6 rounded-xl">
                              <Plus className="mr-2 h-4 w-4" /> Nouveau Template
                           </Button>
                        </div>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 space-y-6">
                        <div className="space-y-4">
                           <div className="flex items-center justify-between p-4 bg-zinc-50 rounded-2xl border border-zinc-100 ring-2 ring-purple-500/20">
                              <div className="flex items-center gap-4">
                                 <div className="p-2 bg-purple-100 text-purple-700 rounded-lg"><Plus size={16} /></div>
                                 <span className="text-xs font-black uppercase">Crédit de Points (Gagné)</span>
                              </div>
                              <Button variant="ghost" size="sm" className="text-[10px] font-black uppercase text-purple-700">Configurer <ChevronRight size={14} /></Button>
                           </div>

                           <div className="p-6 bg-white border border-zinc-100 rounded-2xl space-y-4 shadow-sm">
                              <div className="flex flex-wrap gap-2 mb-2">
                                 <Chip label="member_name" onClick={() => { }} />
                                 <Chip label="amount" onClick={() => { }} />
                                 <Chip label="currency" onClick={() => { }} />
                                 <Chip label="reason" onClick={() => { }} />
                              </div>
                              <div className="relative">
                                 <textarea
                                    className="w-full h-32 bg-zinc-50 border-zinc-100 rounded-xl p-4 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-purple-500/10"
                                    defaultValue="Félicitations {member_name} ! Vous venez de recevoir {amount} {currency} pour votre action : {reason}. Votre nouveau solde est disponible dans votre application."
                                 />
                                 <div className="absolute top-2 right-2 flex gap-1">
                                    <Badge className="bg-zinc-900/5 text-zinc-500 text-[8px] font-black rounded-md">SMS</Badge>
                                    <Badge className="bg-purple-600 text-white text-[8px] font-black rounded-md">PUSH</Badge>
                                 </div>
                              </div>
                              <div className="flex justify-between items-center">
                                 <p className="text-[9px] font-medium text-zinc-400 italic">Dernière modification : Il y a 2 jours</p>
                                 <Button variant="outline" size="sm" className="h-8 text-[10px] font-black uppercase rounded-lg border-zinc-200">
                                    <CheckCircle2 className="mr-2 h-3.5 w-3.5 text-green-500" /> Envoyer un test
                                 </Button>
                              </div>
                           </div>

                           {['Débit de Points', 'Récompense Débloquée', 'Changement de Palier'].map(name => (
                              <div key={name} className="flex items-center justify-between p-4 bg-zinc-50 rounded-2xl border border-zinc-100 opacity-60">
                                 <div className="flex items-center gap-4">
                                    <div className="p-2 bg-zinc-200 text-zinc-500 rounded-lg"><Bell size={16} /></div>
                                    <span className="text-xs font-black uppercase text-zinc-500">{name}</span>
                                 </div>
                                 <Button variant="ghost" size="sm" className="text-[10px] font-black uppercase text-zinc-400">Éditer <ChevronRight size={14} /></Button>
                              </div>
                           ))}
                        </div>
                     </CardContent>
                  </Card>
               </div>
            )}

            {/* --- Section: Intégrations --- */}
            {activeTab === "integrations" && (
               <div className="space-y-6">
                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <div className="flex items-center justify-between">
                           <div>
                              <CardTitle className="text-xl font-black">Clés API du Tenant</CardTitle>
                              <CardDescription>Clés secrètes permettant d'authentifier vos appels vers l'API Loyalty.</CardDescription>
                           </div>
                           <Button className="bg-purple-600 hover:bg-purple-700 text-white text-[10px] font-black uppercase tracking-widest h-10 px-6 rounded-xl">
                              <Plus className="mr-2 h-4 w-4" /> Nouvelle Clé
                           </Button>
                        </div>
                     </CardHeader>
                     <CardContent className="p-0">
                        <Table>
                           <TableHeader className="bg-zinc-50 border-y border-zinc-100">
                              <TableRow>
                                 <TableHead className="pl-8 text-[10px] font-black uppercase tracking-widest">Nom / Description</TableHead>
                                 <TableHead className="text-[10px] font-black uppercase tracking-widest">Attribuée le</TableHead>
                                 <TableHead className="text-[10px] font-black uppercase tracking-widest">Dernier usage</TableHead>
                                 <TableHead className="text-[10px] font-black uppercase tracking-widest">Valeur</TableHead>
                                 <TableHead className="pr-8 text-right"></TableHead>
                              </TableRow>
                           </TableHeader>
                           <TableBody>
                              <TableRow className="border-zinc-50">
                                 <TableCell className="pl-8 font-bold text-sm">Production App Backend</TableCell>
                                 <TableCell className="text-xs text-zinc-500">22 Mai 2026</TableCell>
                                 <TableCell><Badge variant="outline" className="bg-green-50 text-green-600 text-[9px] border-none">À l'instant</Badge></TableCell>
                                 <TableCell className="font-mono text-xs text-zinc-400">loy_prod_••••••••••••x7a2</TableCell>
                                 <TableCell className="pr-8 text-right">
                                    <div className="flex justify-end gap-2 text-zinc-400">
                                       <Button variant="ghost" size="icon" className="h-8 w-8"><Copy size={16} /></Button>
                                       <Button variant="ghost" size="icon" className="h-8 w-8"><Trash2 size={16} className="text-red-400" /></Button>
                                    </div>
                                 </TableCell>
                              </TableRow>
                           </TableBody>
                        </Table>
                     </CardContent>
                  </Card>

                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <CardTitle className="text-xl font-black">Webhooks & Journal</CardTitle>
                        <CardDescription>Configurez des notifications HTTP vers vos services tiers.</CardDescription>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 space-y-6">
                        <div className="flex items-center gap-4 bg-zinc-900 text-white p-6 rounded-3xl relative overflow-hidden">
                           <Webhook className="text-purple-400 relative z-10" size={32} />
                           <div className="relative z-10 flex-1">
                              <p className="text-xs font-black uppercase tracking-widest">Slack Integration</p>
                              <p className="text-[10px] text-zinc-400 mt-1 font-mono">const webhookUrl = process.env.NEXT_PUBLIC_SLACK_WEBHOOK_URL;</p>
                              <div className="flex gap-2 mt-3">
                                 <Badge className="bg-white/10 text-white border-none text-[8px] font-black uppercase">Points.Earned</Badge>
                                 <Badge className="bg-white/10 text-white border-none text-[8px] font-black uppercase">Reward.Claimed</Badge>
                              </div>
                           </div>
                           <Button variant="outline" className="bg-transparent border-white/20 text-white hover:bg-white/10 text-[10px] font-black uppercase rounded-xl">Monitorer</Button>
                        </div>

                        <div className="space-y-4">
                           <h4 className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Journal des appels (API & Webhooks)</h4>
                           <div className="space-y-2">
                              {[1, 2, 3].map(i => (
                                 <div key={i} className="flex items-center justify-between p-3 bg-zinc-50 rounded-xl border border-zinc-100">
                                    <div className="flex items-center gap-3">
                                       <div className="w-1.5 h-1.5 rounded-full bg-green-500" />
                                       <span className="text-[10px] font-black font-mono">POST <span className="text-zinc-400">/v1/wallets/credit</span></span>
                                    </div>
                                    <span className="text-[10px] font-medium text-zinc-400">200 OK • 42ms • Il y a 5 min</span>
                                 </div>
                              ))}
                           </div>
                        </div>
                     </CardContent>
                  </Card>
               </div>
            )}

            {/* --- Section: Sécurité --- */}
            {activeTab === "security" && (
               <div className="space-y-6">
                  <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                     <CardHeader className="pt-8 px-8">
                        <CardTitle className="text-xl font-black">Validation OTP & Transactions</CardTitle>
                        <CardDescription>Sécurisez les opérations sensibles via une double authentification.</CardDescription>
                     </CardHeader>
                     <CardContent className="p-8 pt-4 space-y-8">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
                           <div className="space-y-4">
                              <Toggle enabled={true} onChange={() => { }} label="Activer l'OTP pour les débits" />
                              <Toggle enabled={false} onChange={() => { }} label="OTP pour les transferts entre membres" />
                           </div>
                           <div className="space-y-2">
                              <Label className="text-[10px] font-black uppercase text-zinc-400">Seuil de déclenchement OTP</Label>
                              <div className="relative">
                                 <Input defaultValue="5,000" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-12" />
                                 <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">PTS</span>
                              </div>
                              <p className="text-[10px] text-zinc-400 italic">Passé ce montant, une validation par code est requise.</p>
                           </div>
                        </div>
                     </CardContent>
                  </Card>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                     <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                        <CardHeader className="pt-8 px-8">
                           <CardTitle className="text-lg font-black flex items-center gap-2"><Lock size={18} className="text-zinc-400" /> Pool IP Autorisées</CardTitle>
                        </CardHeader>
                        <CardContent className="px-8 pb-8 space-y-4">
                           <div className="bg-zinc-50 p-4 rounded-2xl flex items-center justify-between">
                              <span className="text-xs font-mono font-bold">192.168.1.1/32</span>
                              <Badge className="bg-blue-100 text-blue-700 border-none text-[8px] font-black uppercase">Main Server</Badge>
                           </div>
                           <Button variant="outline" className="w-full h-10 border-dashed border-zinc-200 text-zinc-400 text-[10px] font-black uppercase rounded-xl">
                              <Plus className="mr-2 h-4 w-4" /> Ajouter une adresse IP
                           </Button>
                        </CardContent>
                     </Card>

                     <Card className="border-none shadow-xl bg-white rounded-3xl overflow-hidden">
                        <CardHeader className="pt-8 px-8">
                           <CardTitle className="text-lg font-black flex items-center gap-2"><Smartphone size={18} className="text-zinc-400" /> Police de Session</CardTitle>
                        </CardHeader>
                        <CardContent className="px-8 pb-8 space-y-4">
                           <div className="space-y-2">
                              <Label className="text-[10px] font-black uppercase text-zinc-400">Expiration Session (Inactivité)</Label>
                              <div className="relative">
                                 <Input defaultValue="30" className="bg-zinc-50 border-zinc-100 rounded-xl font-bold pr-16" />
                                 <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-zinc-400">MINUTES</span>
                              </div>
                           </div>
                           <Toggle enabled={true} onChange={() => { }} label="Forcer la reconnexion tous les 30 jours" />
                        </CardContent>
                     </Card>
                  </div>
               </div>
            )}
         </main>
      </div>
   );
}
