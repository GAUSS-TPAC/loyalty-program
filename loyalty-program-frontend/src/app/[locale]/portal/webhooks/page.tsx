"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Webhook, Plus, Trash2, Send, RefreshCw, AlertTriangle, Copy, Check } from "lucide-react";
import { useWebhooks } from "@/hooks/useBackend";
import { webhookApi } from "@/lib/api";

const EVENT_TYPES = [
  "points.earned",
  "points.redeemed",
  "reward.granted",
  "reward.redeemed",
  "tier.changed",
];

export default function WebhooksPage() {
  const t = useTranslations("Developer");
  const { data: webhooks, isLoading, error, refetch } = useWebhooks();
  const [url, setUrl] = useState("");
  const [description, setDescription] = useState("");
  const [selectedEvents, setSelectedEvents] = useState<string[]>([]);
  const [creating, setCreating] = useState(false);
  const [revealedSecret, setRevealedSecret] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [pingResults, setPingResults] = useState<Record<string, string>>({});

  const toggleEvent = (evt: string) => {
    setSelectedEvents((prev) => (prev.includes(evt) ? prev.filter((e) => e !== evt) : [...prev, evt]));
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!url.trim() || selectedEvents.length === 0) return;
    setCreating(true);
    try {
      const created = await webhookApi.create({ url: url.trim(), description, eventTypes: selectedEvents });
      if (created.secret) setRevealedSecret(created.secret);
      setUrl("");
      setDescription("");
      setSelectedEvents([]);
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id: string) => {
    await webhookApi.remove(id);
    refetch();
  };

  const handleTestPing = async (id: string) => {
    setPingResults((prev) => ({ ...prev, [id]: "sending..." }));
    try {
      const result = await webhookApi.sendTestPing(id);
      setPingResults((prev) => ({
        ...prev,
        [id]: result.success ? `OK (${result.httpStatus})` : `Failed (${result.httpStatus ?? "no response"})`,
      }));
    } catch {
      setPingResults((prev) => ({ ...prev, [id]: "Error" }));
    }
  };

  const handleCopy = () => {
    if (!revealedSecret) return;
    navigator.clipboard.writeText(revealedSecret);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("webhooksTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("webhooksDescription")}</p>
      </div>

      {revealedSecret && (
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-yellow-700 text-sm font-medium">
            <AlertTriangle className="w-4 h-4" />
            Copy this signing secret now — it won&apos;t be shown again. Use it to verify the
            X-Webhook-Signature header.
          </div>
          <div className="flex items-center gap-2">
            <code className="flex-1 bg-card border border-border rounded-md px-3 py-2 text-xs font-mono overflow-x-auto">
              {revealedSecret}
            </code>
            <button
              onClick={handleCopy}
              className="p-2 rounded-md border border-border hover:bg-secondary transition-colors"
            >
              {copied ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>
          <button
            onClick={() => setRevealedSecret(null)}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            Dismiss
          </button>
        </div>
      )}

      <form onSubmit={handleCreate} className="bg-card border border-border rounded-xl p-5 space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Endpoint URL</label>
            <input
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://your-system.example.com/webhooks/loyalty"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Description (optional)</label>
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Order system sync"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Events</label>
          <div className="flex flex-wrap gap-2">
            {EVENT_TYPES.map((evt) => (
              <button
                type="button"
                key={evt}
                onClick={() => toggleEvent(evt)}
                className={`px-3 py-1.5 text-xs rounded-full border transition-colors ${
                  selectedEvents.includes(evt)
                    ? "bg-primary text-primary-foreground border-primary"
                    : "bg-background text-muted-foreground border-border hover:border-primary/50"
                }`}
              >
                {evt}
              </button>
            ))}
          </div>
        </div>
        <button
          type="submit"
          disabled={creating || !url.trim() || selectedEvents.length === 0}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          Create Webhook
        </button>
      </form>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="p-6 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-14 bg-muted animate-pulse rounded-md" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 text-sm text-destructive flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : !webhooks || webhooks.length === 0 ? (
          <div className="p-8 text-center text-sm text-muted-foreground">
            <Webhook className="w-6 h-6 mx-auto mb-2 opacity-50" />
            No webhooks registered yet.
          </div>
        ) : (
          <div className="divide-y divide-border">
            {webhooks.map((w) => (
              <div key={w.id} className="p-4 space-y-2">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-sm font-medium text-foreground break-all">{w.url}</div>
                    {w.description && (
                      <div className="text-xs text-muted-foreground mt-0.5">{w.description}</div>
                    )}
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      onClick={() => handleTestPing(w.id)}
                      title="Send test ping"
                      className="p-1.5 rounded-md hover:bg-secondary text-muted-foreground transition-colors"
                    >
                      <Send className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(w.id)}
                      title="Delete webhook"
                      className="p-1.5 rounded-md hover:bg-destructive/10 text-destructive transition-colors"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {w.eventTypes.map((evt) => (
                    <span
                      key={evt}
                      className="text-xs px-2 py-0.5 rounded-full border bg-secondary text-muted-foreground border-border"
                    >
                      {evt}
                    </span>
                  ))}
                </div>
                {pingResults[w.id] && (
                  <div className="text-xs text-muted-foreground flex items-center gap-1">
                    <RefreshCw className="w-3 h-3" />
                    {pingResults[w.id]}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
