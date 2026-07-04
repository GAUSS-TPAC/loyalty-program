"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Key, Plus, Trash2, Copy, Check, AlertTriangle } from "lucide-react";
import { useApiKeys } from "@/hooks/useBackend";
import { apiKeyApi, type ApiKeyMode } from "@/lib/api";

export default function ApiKeysPage() {
  const t = useTranslations("Developer");
  const { data: keys, isLoading, error, refetch } = useApiKeys();
  const [name, setName] = useState("");
  const [mode, setMode] = useState<ApiKeyMode>("LIVE");
  const [creating, setCreating] = useState(false);
  const [revealedKey, setRevealedKey] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setCreating(true);
    try {
      const created = await apiKeyApi.create({ name: name.trim(), mode });
      if (created.rawKey) setRevealedKey(created.rawKey);
      setName("");
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setCreating(false);
    }
  };

  const handleRevoke = async (id: string) => {
    await apiKeyApi.revoke(id);
    refetch();
  };

  const handleCopy = () => {
    if (!revealedKey) return;
    navigator.clipboard.writeText(revealedKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("apiKeysTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("apiKeysDescription")}</p>
      </div>

      {revealedKey && (
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-yellow-700 text-sm font-medium">
            <AlertTriangle className="w-4 h-4" />
            Copy this key now — it won&apos;t be shown again.
          </div>
          <div className="flex items-center gap-2">
            <code className="flex-1 bg-card border border-border rounded-md px-3 py-2 text-xs font-mono overflow-x-auto">
              {revealedKey}
            </code>
            <button
              onClick={handleCopy}
              className="p-2 rounded-md border border-border hover:bg-secondary transition-colors"
            >
              {copied ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>
          <button
            onClick={() => setRevealedKey(null)}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            Dismiss
          </button>
        </div>
      )}

      <form
        onSubmit={handleCreate}
        className="bg-card border border-border rounded-xl p-5 flex flex-col sm:flex-row gap-3 sm:items-end"
      >
        <div className="flex-1 space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Key name</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Production backend"
            className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">Mode</label>
          <select
            value={mode}
            onChange={(e) => setMode(e.target.value as ApiKeyMode)}
            className="px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="LIVE">Live</option>
            <option value="TEST">Test</option>
          </select>
        </div>
        <button
          type="submit"
          disabled={creating}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          Create Key
        </button>
      </form>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {isLoading ? (
          <div className="p-6 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-10 bg-muted animate-pulse rounded-md" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 text-sm text-destructive flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : !keys || keys.length === 0 ? (
          <div className="p-8 text-center text-sm text-muted-foreground">
            <Key className="w-6 h-6 mx-auto mb-2 opacity-50" />
            No API keys yet.
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-xs text-muted-foreground">
                <th className="p-4 font-medium">Name</th>
                <th className="p-4 font-medium">Key</th>
                <th className="p-4 font-medium">Mode</th>
                <th className="p-4 font-medium">Status</th>
                <th className="p-4 font-medium">Last used</th>
                <th className="p-4"></th>
              </tr>
            </thead>
            <tbody>
              {keys.map((k) => (
                <tr key={k.id} className="border-b border-border last:border-0">
                  <td className="p-4 text-foreground">{k.name}</td>
                  <td className="p-4 font-mono text-xs text-muted-foreground">{k.keyPrefix}</td>
                  <td className="p-4">
                    <span
                      className={`text-xs px-2 py-0.5 rounded-full border ${
                        k.mode === "TEST"
                          ? "bg-blue-500/10 text-blue-600 border-blue-500/20"
                          : "bg-green-500/10 text-green-600 border-green-500/20"
                      }`}
                    >
                      {k.mode}
                    </span>
                  </td>
                  <td className="p-4">
                    <span
                      className={`text-xs px-2 py-0.5 rounded-full border ${
                        k.active
                          ? "bg-green-500/10 text-green-600 border-green-500/20"
                          : "bg-muted text-muted-foreground border-border"
                      }`}
                    >
                      {k.active ? "Active" : "Revoked"}
                    </span>
                  </td>
                  <td className="p-4 text-muted-foreground text-xs">
                    {k.lastUsedAt ? new Date(k.lastUsedAt).toLocaleString() : "Never"}
                  </td>
                  <td className="p-4 text-right">
                    {k.active && (
                      <button
                        onClick={() => handleRevoke(k.id)}
                        className="p-1.5 rounded-md hover:bg-destructive/10 text-destructive transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
