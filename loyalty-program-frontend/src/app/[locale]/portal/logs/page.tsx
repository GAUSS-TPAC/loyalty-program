"use client";

import { Activity, Clock, Terminal, ChevronRight, AlertCircle, CheckCircle2 } from "lucide-react";
import { useTranslations } from "next-intl";

export default function LogsView() {
  const logs = [
    { id: "evt_1a9b", type: "purchase.completed", status: 200, time: "2026-06-03T10:14:22Z", member: "usr_9921", points: "+500" },
    { id: "evt_1a9c", type: "account.created", status: 200, time: "2026-06-03T10:15:01Z", member: "usr_9922", points: "+100" },
    { id: "evt_1a9d", type: "review.posted", status: 400, time: "2026-06-03T10:22:15Z", member: "usr_9921", points: "0 (Trigger Mismatch)" },
    { id: "evt_1a9e", type: "purchase.completed", status: 200, time: "2026-06-03T11:05:44Z", member: "usr_9923", points: "+1500" },
  ];

  const t = useTranslations("Logs");

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Flux des <span className="text-primary italic">Événements</span> (OSINT)
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Monitoring en temps réel de l&apos;activité du moteur de règles.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="px-4 py-2 bg-emerald-50 border border-emerald-200 rounded-full flex items-center gap-2.5 text-[10px] font-black uppercase tracking-widest text-emerald-700 shadow-sm">
            <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            KAFKA : LISTENING
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Terminal Stats */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-card border border-border rounded-2xl p-6 shadow-sm space-y-6">
            <div className="flex items-center gap-2 font-bold text-xs uppercase tracking-widest border-b border-border pb-4">
              <Terminal className="w-4 h-4 text-primary" /> Metrics Session
            </div>

            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Events Totaux</span>
                <span className="font-mono text-sm font-bold">1,284</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Success Rate</span>
                <span className="font-mono text-sm font-bold text-emerald-600">99.2%</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Latence Moy.</span>
                <span className="font-mono text-sm font-bold text-primary">42ms</span>
              </div>
            </div>
          </div>

          <div className="bg-secondary/30 rounded-2xl p-6 border border-border space-y-2">
            <p className="text-[10px] uppercase font-black text-primary tracking-widest">Aide</p>
            <p className="text-[11px] text-muted-foreground leading-relaxed">
              Ce flux affiche les événements reçus via Kafka et traités par le Loyalty Rule Engine.
            </p>
          </div>
        </div>

        {/* Live Logs Table */}
        <div className="lg:col-span-3 border border-border bg-card rounded-2xl overflow-hidden shadow-sm flex flex-col group">
          <div className="bg-secondary/40 px-8 py-5 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-card rounded-lg shadow-sm border border-border">
                <Activity className="w-5 h-5 text-primary animate-pulse" />
              </div>
              <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Flux Live (Logs JSON)</h3>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead className="text-[10px] text-muted-foreground uppercase bg-muted/30 border-b border-border font-black tracking-widest">
                <tr>
                  <th className="px-8 py-5">Temps (UTC)</th>
                  <th className="px-8 py-5">Type d&apos;Événement</th>
                  <th className="px-8 py-5">Membre</th>
                  <th className="px-8 py-5 text-right">Résultat / Points</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {logs.map((log, index) => (
                  <tr key={log.id} className={`group/row transition-all hover:bg-secondary/30 ${index % 2 === 0 ? 'bg-background' : 'bg-muted/5'}`}>
                    <td className="px-8 py-6">
                      <div className="flex items-center gap-3">
                        <Clock className="w-3.5 h-3.5 text-muted-foreground" />
                        <span className="font-mono text-muted-foreground text-[10px]">
                          {new Date(log.time).toLocaleTimeString()}
                        </span>
                      </div>
                    </td>
                    <td className="px-8 py-6">
                      <div className="space-y-1">
                        <span className="text-[10px] font-black uppercase tracking-widest text-primary bg-primary/10 px-2 py-0.5 rounded border border-primary/20">
                          {log.type}
                        </span>
                        <p className="text-[9px] font-mono text-muted-foreground">{log.id}</p>
                      </div>
                    </td>
                    <td className="px-8 py-6">
                      <div className="flex items-center gap-2">
                        <div className="w-6 h-6 rounded-full bg-secondary border border-border flex items-center justify-center text-[10px] font-bold text-primary">
                          {log.member.substring(4, 5)}
                        </div>
                        <span className="text-xs font-medium text-foreground">{log.member}</span>
                      </div>
                    </td>
                    <td className="px-8 py-6 text-right">
                      <div className="flex flex-col items-end gap-1">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[10px] font-black uppercase tracking-wider ${log.status === 200 ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' : 'bg-rose-50 text-rose-700 border border-rose-100'
                          }`}>
                          {log.status === 200 ? <CheckCircle2 className="w-3 h-3" /> : <AlertCircle className="w-3 h-3" />}
                          {log.points}
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="p-4 border-t border-border bg-muted/10 text-center">
            <button className="text-[10px] uppercase font-black text-primary hover:underline flex items-center gap-2 mx-auto">
              Charger l&apos;historique complet <ChevronRight className="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
