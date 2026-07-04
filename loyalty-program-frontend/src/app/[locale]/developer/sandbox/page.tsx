"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { FlaskConical, AlertTriangle, Send } from "lucide-react";
import { useApiKeys } from "@/hooks/useBackend";
import { eventsApi } from "@/lib/api";

export default function SandboxPage() {
  const t = useTranslations("Developer");
  const { data: keys } = useApiKeys();
  const testKeys = keys?.filter((k) => k.mode === "TEST" && k.active) ?? [];

  const [eventType, setEventType] = useState("purchase.completed");
  const [memberId, setMemberId] = useState("usr_test_001");
  const [amount, setAmount] = useState("49.90");
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState<string | null>(null);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    setSending(true);
    setResult(null);
    try {
      const response = await eventsApi.processEvent({
        eventType,
        memberId,
        amount: amount ? Number(amount) : undefined,
      });
      setResult(`Success: ${response.pointsAwarded} points awarded, rules applied: ${response.rulesApplied.join(", ") || "none"}`);
    } catch (err) {
      setResult(err instanceof Error ? err.message : "Error sending test event");
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("sandboxTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("sandboxDescription")}</p>
      </div>

      <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-5 flex gap-3">
        <AlertTriangle className="w-4.5 h-4.5 text-yellow-700 shrink-0 mt-0.5" />
        <p className="text-sm text-yellow-800">
          Sandbox data isolation is not implemented yet: events sent below run through the real rule
          engine and affect real member data. Only the API key is tagged as TEST mode today — treat this
          as a way to trigger real events end-to-end, not as an isolated environment.
        </p>
      </div>

      <div className="bg-card border border-border rounded-xl p-6 space-y-4">
        <h2 className="text-sm font-semibold text-foreground">Your TEST-mode keys</h2>
        {testKeys.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            No TEST-mode key yet. Create one from the API Keys page to clearly mark integration traffic.
          </p>
        ) : (
          <div className="space-y-2">
            {testKeys.map((k) => (
              <div key={k.id} className="flex items-center justify-between text-sm">
                <span className="text-foreground">{k.name}</span>
                <code className="text-xs font-mono text-muted-foreground">{k.keyPrefix}</code>
              </div>
            ))}
          </div>
        )}
      </div>

      <form onSubmit={handleSend} className="bg-card border border-border rounded-xl p-6 space-y-4">
        <h2 className="text-sm font-semibold text-foreground flex items-center gap-2">
          <FlaskConical className="w-4 h-4" />
          Send a Test Event
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Event type</label>
            <input
              value={eventType}
              onChange={(e) => setEventType(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Member ID</label>
            <input
              value={memberId}
              onChange={(e) => setMemberId(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Amount</label>
            <input
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
        </div>
        <button
          type="submit"
          disabled={sending}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <Send className="w-4 h-4" />
          Send Event
        </button>
        {result && (
          <p className="text-sm text-muted-foreground border-t border-border pt-3">{result}</p>
        )}
      </form>
    </div>
  );
}
