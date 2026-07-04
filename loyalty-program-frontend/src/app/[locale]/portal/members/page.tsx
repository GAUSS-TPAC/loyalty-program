"use client";

import { useState } from "react";
import { Link } from "@/i18n/routing";
import { Users, Search, ArrowRight, ShieldAlert, ShieldCheck, User, AlertTriangle } from "lucide-react";
import { useAdminMembers } from "@/hooks/useBackend";

export default function MembersDirectory() {
  const [searchQuery, setSearchQuery] = useState("");
  const { data: members, isLoading, error } = useAdminMembers(0, 100);

  const filteredMembers = (members ?? []).filter((m) =>
    m.memberId.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8">
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-6">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Annuaire des <span className="text-primary italic">Membres</span>
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Portefeuilles enregistrés pour ce tenant.
          </p>
        </div>

        <div className="relative w-full lg:w-96 group">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Search className="w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
          </div>
          <input
            type="text"
            placeholder="Rechercher par ID membre..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-card border border-border rounded-2xl pl-12 pr-4 py-3.5 text-sm focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary/50 shadow-sm transition-all placeholder:text-muted-foreground/60"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div className="bg-card border border-border rounded-2xl p-6 flex items-center gap-4 shadow-sm group hover:border-primary/20 transition-all">
          <div className="p-3 bg-secondary rounded-xl text-primary font-bold">
            <Users className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Total Membres</p>
            <p className="text-xl font-bold font-mono tracking-tight">{members?.length ?? 0}</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-2xl p-6 flex items-center gap-4 shadow-sm">
          <div className="p-3 bg-emerald-50 rounded-xl text-emerald-600">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Actifs</p>
            <p className="text-xl font-bold font-mono tracking-tight">
              {members?.filter((m) => m.status === "ACTIVE").length ?? 0}
            </p>
          </div>
        </div>
      </div>

      <div className="border border-border bg-card rounded-2xl overflow-hidden shadow-sm shadow-primary/5">
        <div className="bg-secondary/30 px-8 py-5 border-b border-border flex items-center gap-3">
          <Users className="w-5 h-5 text-primary/60" />
          <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">
            Base Clients ({filteredMembers.length})
          </h3>
        </div>

        {isLoading ? (
          <div className="p-12 space-y-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-12 bg-muted rounded-xl animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="p-8 text-center text-sm text-destructive flex items-center justify-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left border-collapse">
              <thead className="text-[10px] text-muted-foreground uppercase bg-muted/20 border-b border-border font-black tracking-widest">
                <tr>
                  <th className="px-8 py-5 font-black uppercase">Membre (UUID)</th>
                  <th className="px-8 py-5 font-black uppercase text-right">Solde Wallet</th>
                  <th className="px-8 py-5 font-black uppercase">Statut</th>
                  <th className="px-8 py-5 text-right font-black uppercase">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {filteredMembers.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="p-12 text-center text-muted-foreground italic text-sm">
                      Aucun membre ne correspond à votre recherche.
                    </td>
                  </tr>
                ) : (
                  filteredMembers.map((member, index) => (
                    <tr
                      key={member.memberId}
                      className={`group/row transition-all hover:bg-secondary/30 ${
                        index % 2 === 0 ? "bg-background" : "bg-muted/5"
                      }`}
                    >
                      <td className="px-8 py-6">
                        <div className="flex items-center gap-4">
                          <div className="w-10 h-10 rounded-full bg-secondary border border-border flex items-center justify-center text-primary font-bold group-hover/row:scale-110 transition-transform">
                            <User className="w-5 h-5" />
                          </div>
                          <span className="font-mono text-xs text-foreground">{member.memberId}</span>
                        </div>
                      </td>
                      <td className="px-8 py-6 font-mono text-primary font-black text-right text-base tracking-tighter">
                        {member.balance.toLocaleString()}{" "}
                        <span className="text-[10px] font-bold text-muted-foreground tracking-widest ml-1">
                          {member.currencyCode}
                        </span>
                      </td>
                      <td className="px-8 py-6">
                        {member.status === "ACTIVE" && (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-100 shadow-sm">
                            <ShieldCheck className="w-3 h-3" /> Actif
                          </span>
                        )}
                        {member.status === "PENDING_KYC" && (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-wider bg-orange-50 text-orange-700 border border-orange-100">
                            <ShieldAlert className="w-3 h-3" /> Attente KYC
                          </span>
                        )}
                        {(member.status === "FROZEN" || member.status === "CLOSED") && (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-wider bg-rose-50 text-rose-700 border border-rose-100">
                            <ShieldAlert className="w-3 h-3" /> {member.status === "FROZEN" ? "Gelé" : "Fermé"}
                          </span>
                        )}
                      </td>
                      <td className="px-8 py-6 text-right">
                        <Link
                          href={`/portal/members/${member.memberId}`}
                          className="inline-flex items-center gap-2 text-[11px] font-black uppercase tracking-widest text-primary hover:text-primary-foreground hover:bg-primary px-4 py-2.5 rounded-xl border border-primary/20 transition-all shadow-sm active:scale-95"
                        >
                          Gérer
                          <ArrowRight className="w-3.5 h-3.5" />
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
