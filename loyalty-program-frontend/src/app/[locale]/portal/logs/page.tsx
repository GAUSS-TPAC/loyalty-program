"use client";

import { useState } from "react";
import { Activity, Clock, Terminal, ChevronLeft, ChevronRight, AlertTriangle } from "lucide-react";
import { useAdminLogs } from "@/hooks/useBackend";

export default function LogsView() {
  const [page, setPage] = useState(0);
  const { data: logs, isLoading, error } = useAdminLogs(page, 20);

  const creditCount = logs?.filter((l) => l.type === "CREDIT").length ?? 0;
  const debitCount = logs?.filter((l) => l.type === "DEBIT").length ?? 0;

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Journal des <span className="text-primary italic">Transactions de Points</span>
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Historique tenant-wide des crédits/débits de points issus du moteur de règles.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-card border border-border rounded-2xl p-6 shadow-sm space-y-6">
            <div className="flex items-center gap-2 font-bold text-xs uppercase tracking-widest border-b border-border pb-4">
              <Terminal className="w-4 h-4 text-primary" /> Metrics (page courante)
            </div>

            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Transactions</span>
                <span className="font-mono text-sm font-bold">{logs?.length ?? 0}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Crédits</span>
                <span className="font-mono text-sm font-bold text-emerald-600">{creditCount}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-muted-foreground">Débits</span>
                <span className="font-mono text-sm font-bold text-primary">{debitCount}</span>
              </div>
            </div>
          </div>

          <div className="bg-secondary/30 rounded-2xl p-6 border border-border space-y-2">
            <p className="text-[10px] uppercase font-black text-primary tracking-widest">Aide</p>
            <p className="text-[11px] text-muted-foreground leading-relaxed">
              Ce flux affiche les transactions de points persistées, résultat des événements traités par le
              Loyalty Rule Engine.
            </p>
          </div>
        </div>

        <div className="lg:col-span-3 border border-border bg-card rounded-2xl overflow-hidden shadow-sm flex flex-col group">
          <div className="bg-secondary/40 px-8 py-5 border-b border-border flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-card rounded-lg shadow-sm border border-border">
                <Activity className="w-5 h-5 text-primary" />
              </div>
              <h3 className="font-bold text-sm uppercase tracking-widest text-foreground">Transactions de Points</h3>
            </div>
          </div>

          {isLoading ? (
            <div className="p-8 space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-10 bg-muted animate-pulse rounded-md" />
              ))}
            </div>
          ) : error ? (
            <div className="p-8 text-sm text-destructive flex items-center gap-2">
              <AlertTriangle className="w-4 h-4" />
              {error}
            </div>
          ) : !logs || logs.length === 0 ? (
            <div className="p-12 text-center text-sm text-muted-foreground">Aucune transaction trouvée.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="text-[10px] text-muted-foreground uppercase bg-muted/30 border-b border-border font-black tracking-widest">
                  <tr>
                    <th className="px-8 py-5">Temps (UTC)</th>
                    <th className="px-8 py-5">Type</th>
                    <th className="px-8 py-5">Source</th>
                    <th className="px-8 py-5 text-right">Points</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border/60">
                  {logs.map((log, index) => (
                    <tr
                      key={log.id}
                      className={`group/row transition-all hover:bg-secondary/30 ${
                        index % 2 === 0 ? "bg-background" : "bg-muted/5"
                      }`}
                    >
                      <td className="px-8 py-6">
                        <div className="flex items-center gap-3">
                          <Clock className="w-3.5 h-3.5 text-muted-foreground" />
                          <span className="font-mono text-muted-foreground text-[10px]">
                            {new Date(log.createdAt).toLocaleString()}
                          </span>
                        </div>
                      </td>
                      <td className="px-8 py-6">
                        <span className="text-[10px] font-black uppercase tracking-widest text-primary bg-primary/10 px-2 py-0.5 rounded border border-primary/20">
                          {log.type}
                        </span>
                        <p className="text-[9px] font-mono text-muted-foreground mt-1">
                          {log.pointsAccountId.substring(0, 8)}…
                        </p>
                      </td>
                      <td className="px-8 py-6 text-xs text-muted-foreground">{log.source}</td>
                      <td className="px-8 py-6 text-right">
                        <span
                          className={`font-mono text-sm font-black ${
                            log.type === "CREDIT" ? "text-emerald-600" : "text-rose-600"
                          }`}
                        >
                          {log.type === "CREDIT" ? "+" : "-"}
                          {log.amount}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="p-4 border-t border-border bg-muted/10 flex items-center justify-center gap-3">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-2 rounded-md border border-border hover:bg-secondary disabled:opacity-40 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs text-muted-foreground">Page {page + 1}</span>
            <button
              onClick={() => setPage((p) => p + 1)}
              disabled={!logs || logs.length < 20}
              className="p-2 rounded-md border border-border hover:bg-secondary disabled:opacity-40 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
