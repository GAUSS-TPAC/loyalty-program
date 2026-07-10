"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Terminal, AlertTriangle, ChevronLeft, ChevronRight } from "lucide-react";
import { useWebhookDeliveries } from "@/hooks/useBackend";
import type { WebhookDeliveryStatus } from "@/lib/api";

const STATUS_COLORS: Record<WebhookDeliveryStatus, string> = {
  SUCCEEDED: "bg-green-500/10 text-green-600 border-green-500/20",
  PENDING: "bg-yellow-500/10 text-yellow-600 border-yellow-500/20",
  FAILED: "bg-red-500/10 text-red-600 border-red-500/20",
  EXHAUSTED: "bg-red-500/10 text-red-700 border-red-500/20",
};

export default function LogsPage() {
  const t = useTranslations("Developer");
  const [page, setPage] = useState(0);
  const { data: deliveries, isLoading, error } = useWebhookDeliveries(page, 20);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("logsTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("logsDescription")}</p>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="p-6 space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-10 bg-muted animate-pulse rounded-md" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 text-sm text-destructive flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : !deliveries || deliveries.length === 0 ? (
          <div className="p-8 text-center text-sm text-muted-foreground">
            <Terminal className="w-6 h-6 mx-auto mb-2 opacity-50" />
            No webhook deliveries yet.
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-xs text-muted-foreground">
                <th className="p-4 font-medium">Time</th>
                <th className="p-4 font-medium">Event</th>
                <th className="p-4 font-medium">Status</th>
                <th className="p-4 font-medium">HTTP</th>
                <th className="p-4 font-medium">Attempts</th>
              </tr>
            </thead>
            <tbody>
              {deliveries.map((d) => (
                <tr key={d.id} className="border-b border-border last:border-0">
                  <td className="p-4 text-xs text-muted-foreground">
                    {new Date(d.createdAt).toLocaleString()}
                  </td>
                  <td className="p-4 font-mono text-xs text-foreground">{d.eventType}</td>
                  <td className="p-4">
                    <span className={`text-xs px-2 py-0.5 rounded-full border ${STATUS_COLORS[d.status]}`}>
                      {d.status}
                    </span>
                  </td>
                  <td className="p-4 text-xs text-muted-foreground">{d.httpStatusCode ?? "—"}</td>
                  <td className="p-4 text-xs text-muted-foreground">{d.attemptCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="flex items-center justify-end gap-2">
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
          disabled={!deliveries || deliveries.length < 20}
          className="p-2 rounded-md border border-border hover:bg-secondary disabled:opacity-40 transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
