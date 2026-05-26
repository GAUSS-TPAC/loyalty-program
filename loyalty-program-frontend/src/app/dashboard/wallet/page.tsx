"use client";

import { useState } from "react";
import { 
  Wallet, 
  ArrowUpRight, 
  ArrowDownRight, 
  Snowflake, 
  Search, 
  ShieldAlert, 
  RefreshCcw, 
  MoreVertical,
  History,
  AlertTriangle,
  CheckCircle2,
  Ban,
  FileSearch,
  HandCoins
} from "lucide-react";
import { 
  Card, 
  CardContent, 
  CardDescription, 
  CardFooter,
  CardHeader, 
  CardTitle 
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { 
  Tabs, 
  TabsContent, 
  TabsList, 
  TabsTrigger 
} from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger 
} from "@/components/ui/dropdown-menu";

// Mock data for the demo
const WALLETS_DATA = [
  { id: "W-1240", memberId: "#M-8821", name: "Jean Dupont", points: 4500, xaf: 45000, status: "active", txCount: 12 },
  { id: "W-1241", memberId: "#M-8822", name: "Marie Curie", points: 12800, xaf: 128000, status: "active", txCount: 45 },
  { id: "W-1242", memberId: "#M-8823", name: "Paul Valéry", points: 1500, xaf: 15000, status: "frozen", txCount: 8 },
  { id: "W-1243", memberId: "#M-8824", name: "Simone de Beauvoir", points: 30200, xaf: 302000, status: "active", txCount: 67 },
  { id: "W-1244", memberId: "#M-8825", name: "Albert Camus", points: 0, xaf: 0, status: "inactive", txCount: 2 },
];

const SUSPICIOUS_DATA = [
  { id: "TX-9901", memberId: "#M-8822", name: "Marie Curie", amount: 5000, reason: "Multi-rechargement rapide", date: "Il y a 2h" },
  { id: "TX-9902", memberId: "#M-8829", name: "Robert Sabatier", amount: 15000, reason: "Incohérence IP", date: "Il y a 5h" },
];

const RECONCILIATION_DATA = [
  { id: "W-1240", name: "Jean Dupont", balance: 4500, calculated: 4500, diff: 0, status: "ok" },
  { id: "W-1242", name: "Paul Valéry", balance: 1500, calculated: 1400, diff: 100, status: "error" },
];

export default function WalletPage() {
  const [isReconciling, setIsReconciling] = useState(false);

  const startReconciliation = () => {
    setIsReconciling(true);
    setTimeout(() => setIsReconciling(false), 2000);
  };

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Wallet et Transactions</h1>
        <p className="text-muted-foreground mt-2">Vue macro sur l'économie de votre programme de fidélité.</p>
      </div>

      {/* Top Metrics */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Volume total en circulation</CardTitle>
            <Wallet className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">12.4M pts</div>
            <p className="text-[10px] text-zinc-500 font-medium mt-1">≈ 124,000,000 XAF</p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Recharges (Mai)</CardTitle>
            <ArrowUpRight className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">+850K pts</div>
            <p className="text-[10px] text-green-600 font-bold mt-1 text-xs">↑ 12% vs Avril</p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Dépenses (Mai)</CardTitle>
            <ArrowDownRight className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">-420K pts</div>
            <p className="text-[10px] text-zinc-500 font-medium mt-1">Utilisés dans le catalogue</p>
          </CardContent>
        </Card>
        <Card className="border-none shadow-md bg-gradient-to-br from-white to-zinc-50">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Wallets Gelés</CardTitle>
            <Snowflake className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-black">14</div>
            <Badge variant="outline" className="bg-orange-50 text-orange-700 border-none mt-1 text-[9px] h-4">En attente de décision</Badge>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="wallets" className="w-full">
        <TabsList className="grid w-full max-w-[500px] grid-cols-3 mb-8 bg-zinc-100 p-1">
          <TabsTrigger value="wallets" className="data-[state=active]:bg-white data-[state=active]:shadow-sm">Gestion des Wallets</TabsTrigger>
          <TabsTrigger value="suspects" className="data-[state=active]:bg-white data-[state=active]:shadow-sm flex items-center gap-2">
            Suspects <Badge className="h-5 w-5 p-0 flex items-center justify-center bg-red-100 text-red-600 border-none text-[10px]">2</Badge>
          </TabsTrigger>
          <TabsTrigger value="reconciliation" className="data-[state=active]:bg-white data-[state=active]:shadow-sm">Réconciliation</TabsTrigger>
        </TabsList>

        {/* --- Tab Content: Wallets --- */}
        <TabsContent value="wallets" className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
          <Card className="border-none shadow-xl bg-white overflow-hidden">
            <CardHeader className="flex flex-row items-center justify-between pb-6">
              <div className="space-y-1">
                <CardTitle className="text-lg">Registre des Wallets</CardTitle>
                <CardDescription>Liste exhaustive des comptes et états financiers par membre.</CardDescription>
              </div>
              <div className="flex items-center gap-4">
                <div className="relative w-72">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
                  <Input placeholder="Rechercher par membre ou wallet ID..." className="pl-9 bg-zinc-50 border-zinc-100" />
                </div>
                <Button variant="outline"><FileSearch className="mr-2 h-4 w-4" /> Filtres</Button>
              </div>
            </CardHeader>
            <CardContent className="p-0">
              <Table>
                <TableHeader className="bg-zinc-50 border-y border-zinc-100">
                  <TableRow className="hover:bg-transparent">
                    <TableHead className="pl-6 font-bold text-xs uppercase tracking-widest">Membre</TableHead>
                    <TableHead className="font-bold text-xs uppercase tracking-widest">Solde (Points / XAF)</TableHead>
                    <TableHead className="font-bold text-xs uppercase tracking-widest text-center">Status</TableHead>
                    <TableHead className="font-bold text-xs uppercase tracking-widest text-center">Tx / Mois</TableHead>
                    <TableHead className="pr-6 font-bold text-xs uppercase tracking-widest text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {WALLETS_DATA.map((wallet) => (
                    <TableRow key={wallet.id} className="border-zinc-50 group hover:bg-zinc-50/50 transition-colors">
                      <TableCell className="pl-6 py-4">
                        <div className="flex flex-col">
                          <span className="font-bold text-sm text-zinc-900">{wallet.name}</span>
                          <span className="text-[10px] text-zinc-400 font-mono">{wallet.memberId}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-black text-purple-700">{wallet.points} pts</span>
                          <span className="text-[10px] text-zinc-500">≈ {wallet.xaf.toLocaleString()} XAF</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-center">
                        {wallet.status === "active" && (
                          <Badge variant="outline" className="bg-green-50 text-green-700 border-none text-[10px] px-2 py-0.5">Actif</Badge>
                        )}
                        {wallet.status === "frozen" && (
                          <Badge variant="outline" className="bg-orange-50 text-orange-700 border-none text-[10px] px-2 py-0.5">Gelé</Badge>
                        )}
                        {wallet.status === "inactive" && (
                          <Badge variant="outline" className="bg-zinc-100 text-zinc-500 border-none text-[10px] px-2 py-0.5">Inactif</Badge>
                        )}
                      </TableCell>
                      <TableCell className="text-center font-bold text-zinc-700">{wallet.txCount}</TableCell>
                      <TableCell className="pr-6 text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8 hover:bg-white hover:shadow-sm"><MoreVertical size={16} /></Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end" className="w-48 bg-white border-zinc-100">
                            <DropdownMenuItem className="text-xs font-medium focus:bg-purple-50 focus:text-purple-700 cursor-pointer">
                              <History className="mr-2 h-4 w-4" /> Détail historique
                            </DropdownMenuItem>
                            <DropdownMenuItem className="text-xs font-medium focus:bg-orange-50 focus:text-orange-700 cursor-pointer">
                              <Snowflake className="mr-2 h-4 w-4" /> Geler le wallet
                            </DropdownMenuItem>
                            <DropdownMenuItem className="text-xs font-medium focus:bg-zinc-50 cursor-pointer">
                              <HandCoins className="mr-2 h-4 w-4" /> Ajustement manuel
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* --- Tab Content: Suspects --- */}
        <TabsContent value="suspects" className="animate-in fade-in slide-in-from-bottom-2 duration-300">
          <Card className="border-none shadow-xl bg-white">
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className="p-2 bg-red-50 rounded-lg">
                  <ShieldAlert className="text-red-600" size={20} />
                </div>
                <div>
                  <CardTitle className="text-lg">Files d'attente Suspicion de Fraude</CardTitle>
                  <CardDescription>Transactions signalées automatiquement par l'algorithme de détection.</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-0">
              <Table>
                <TableHeader className="bg-red-50/30 border-y border-red-50">
                  <TableRow className="hover:bg-transparent">
                    <TableHead className="pl-6 font-bold text-xs uppercase text-red-900">Motif du Signalement</TableHead>
                    <TableHead className="font-bold text-xs uppercase text-red-900">Membre</TableHead>
                    <TableHead className="font-bold text-xs uppercase text-red-900">Montant</TableHead>
                    <TableHead className="font-bold text-xs uppercase text-red-900">Horodatage</TableHead>
                    <TableHead className="pr-6 font-bold text-xs uppercase text-red-900 text-right">Action Requise</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {SUSPICIOUS_DATA.map((tx) => (
                    <TableRow key={tx.id} className="border-zinc-50 bg-red-50/5">
                      <TableCell className="pl-6 py-5">
                        <div className="flex items-center gap-2">
                          <AlertTriangle className="text-orange-500" size={14} />
                          <span className="font-bold text-zinc-900 text-sm">{tx.reason}</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-semibold text-zinc-700">{tx.name}</span>
                          <span className="text-[10px] text-zinc-400 font-mono">{tx.memberId}</span>
                        </div>
                      </TableCell>
                      <TableCell><span className="font-black text-red-600">{tx.amount} pts</span></TableCell>
                      <TableCell className="text-zinc-500 text-xs">{tx.date}</TableCell>
                      <TableCell className="pr-6 text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" className="text-zinc-500 hover:text-green-600 hover:bg-green-50 h-8">
                            <CheckCircle2 className="mr-2 h-4 w-4" /> Valider
                          </Button>
                          <Button variant="ghost" size="sm" className="text-zinc-500 hover:text-red-600 hover:bg-red-50 h-8">
                            <Ban className="mr-2 h-4 w-4" /> Bloquer
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* --- Tab Content: Reconciliation --- */}
        <TabsContent value="reconciliation" className="animate-in fade-in slide-in-from-bottom-2 duration-300">
           <Card className="border-none shadow-xl bg-white overflow-hidden">
            <CardHeader className="bg-zinc-900 text-white pb-10 relative">
              <div className="absolute right-6 top-6">
                <Button 
                  onClick={startReconciliation} 
                  disabled={isReconciling}
                  className="bg-white text-zinc-950 hover:bg-zinc-200 transition-all font-bold px-8"
                >
                  {isReconciling ? (
                    <RefreshCcw className="mr-2 h-4 w-4 animate-spin" />
                  ) : (
                    <RefreshCcw className="mr-2 h-4 w-4" />
                  )}
                  Lancer la vérification
                </Button>
              </div>
              <CardTitle className="text-xl">Outil de Réconciliation</CardTitle>
              <CardDescription className="text-zinc-400 mt-2">Compare le solde actuel affiché avec la somme agrégée de l'historique transactionnel.</CardDescription>
            </CardHeader>
            <CardContent className="p-0 -mt-6">
              <Card className="mx-6 border-none shadow-2xl overflow-hidden ring-1 ring-zinc-100">
                <Table>
                  <TableHeader className="bg-zinc-50">
                    <TableRow className="hover:bg-transparent border-b border-zinc-100">
                      <TableHead className="pl-6 py-4 font-bold text-[10px] uppercase">ID Wallet</TableHead>
                      <TableHead className="py-4 font-bold text-[10px] uppercase text-center">Solde Actuel</TableHead>
                      <TableHead className="py-4 font-bold text-[10px] uppercase text-center">Somme Transactions</TableHead>
                      <TableHead className="py-4 font-bold text-[10px] uppercase text-center">Delta (Incohérence)</TableHead>
                      <TableHead className="pr-6 py-4 font-bold text-[10px] uppercase text-right">Status</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {RECONCILIATION_DATA.map((item) => (
                      <TableRow key={item.id} className="border-zinc-50">
                        <TableCell className="pl-6 py-4">
                          <div className="flex flex-col">
                            <span className="font-bold text-sm">{item.id}</span>
                            <span className="text-[10px] text-zinc-500">{item.name}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-center font-mono font-bold text-zinc-600">{item.balance} pts</TableCell>
                        <TableCell className="text-center font-mono font-bold">{item.calculated} pts</TableCell>
                        <TableCell className="text-center">
                          {item.diff === 0 ? (
                            <span className="text-zinc-400 font-mono">0</span>
                          ) : (
                            <Badge className="bg-red-100 text-red-600 border-none font-mono font-black">+{item.diff} pts</Badge>
                          )}
                        </TableCell>
                        <TableCell className="pr-6 text-right">
                          {item.status === "ok" ? (
                            <Badge className="bg-green-100 text-green-700 border-none flex w-fit ml-auto items-center gap-1">
                              <CheckCircle2 size={12} /> Intègre
                            </Badge>
                          ) : (
                            <Badge className="bg-red-100 text-red-700 border-none flex w-fit ml-auto items-center gap-1">
                              <AlertTriangle size={12} /> Erreur de calcul
                            </Badge>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Card>
            </CardContent>
            <CardFooter className="p-8 pt-10 text-center flex flex-col gap-2">
              <p className="text-xs text-zinc-400 italic">Dernière vérification complète effectuée il y a 8 heures.</p>
              <div className="flex gap-4 justify-center mt-4">
                <div className="flex items-center gap-2">
                   <div className="w-2 h-2 rounded-full bg-green-500" />
                   <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-tighter">1,245 Wallets Intègres</span>
                </div>
                <div className="flex items-center gap-2">
                   <div className="w-2 h-2 rounded-full bg-red-500" />
                   <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-tighter">1 Erreur Détectée</span>
                </div>
              </div>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
