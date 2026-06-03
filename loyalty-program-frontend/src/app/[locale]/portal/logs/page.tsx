"use client";

import { Activity } from "lucide-react";
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
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground text-sm">
          {t("description")}
        </p>
      </div>

      <div className="border border-border bg-card rounded-xl overflow-hidden shadow-sm mt-6">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2 text-sm font-medium text-foreground">
            <Activity className="w-4 h-4 text-primary animate-pulse" />
            {t("liveStream")}
          </div>
          <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">
            {t("systemHealthy")}
          </span>
        </div>
        
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-muted-foreground uppercase bg-muted/50 border-b border-border">
              <tr>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("timeUtc")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("eventId")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("eventType")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("memberId")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("result")}</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log, index) => (
                <tr key={log.id} className={`border-b border-border/50 hover:bg-secondary/50 transition-colors ${index % 2 === 0 ? 'bg-background' : 'bg-muted/10'}`}>
                  <td className="px-6 py-4 font-mono text-muted-foreground text-xs whitespace-nowrap">{log.time}</td>
                  <td className="px-6 py-4 font-mono text-foreground text-xs">{log.id}</td>
                  <td className="px-6 py-4 font-mono text-primary text-xs">{log.type}</td>
                  <td className="px-6 py-4 text-foreground">{log.member}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-2 py-1 rounded-md text-xs font-medium ${
                      log.status === 200 ? 'bg-primary/10 text-primary' : 'bg-destructive/10 text-destructive'
                    }`}>
                      {log.points}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        <div className="p-4 border-t border-border bg-muted/30 text-center">
          <button className="text-xs text-primary font-medium hover:underline">{t("loadOlder")}</button>
        </div>
      </div>
    </div>
  );
}
