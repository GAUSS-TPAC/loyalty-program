"use client";

import { useMemo, useState } from "react";
import {
  AppWindow,
  Plus,
  Trash2,
  Copy,
  Check,
  AlertTriangle,
  Key,
  Search,
  Boxes,
} from "lucide-react";
import { useApiKeys, useApiKeyPointsFlow } from "@/hooks/useBackend";
import { apiKeyApi, type ApiKeyMode, type ApiKeyResponse } from "@/lib/api";

// Une "application" = l'ensemble des clés API partageant le même nom
// (le backend n'a pas d'entité application ; le nom de clé sert d'identifiant d'app).
interface Application {
  name: string;
  keys: ApiKeyResponse[];
  createdAt: string;
}

function groupKeysByApplication(keys: ApiKeyResponse[]): Application[] {
  const byName = new Map<string, ApiKeyResponse[]>();
  for (const key of keys) {
    const list = byName.get(key.name) ?? [];
    list.push(key);
    byName.set(key.name, list);
  }
  return [...byName.entries()]
    .map(([name, appKeys]) => ({
      name,
      keys: appKeys.sort((a, b) => (a.mode === b.mode ? 0 : a.mode === "LIVE" ? -1 : 1)),
      createdAt: appKeys.reduce(
        (min, k) => (k.createdAt < min ? k.createdAt : min),
        appKeys[0].createdAt
      ),
    }))
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
}

const MODE_BADGE: Record<ApiKeyMode, string> = {
  TEST: "bg-blue-500/10 text-blue-600 border-blue-500/20",
  LIVE: "bg-green-500/10 text-green-600 border-green-500/20",
};

export default function ApplicationsPage() {
  const { data: keys, isLoading, error, refetch } = useApiKeys();
  const { data: pointsFlows, refetch: refetchFlows } = useApiKeyPointsFlow();

  const [searchQuery, setSearchQuery] = useState("");
  const [newAppName, setNewAppName] = useState("");
  const [creating, setCreating] = useState(false);
  const [addingKeyFor, setAddingKeyFor] = useState<string | null>(null);
  // Clés brutes révélées une seule fois après création, groupées par application
  const [revealedKeys, setRevealedKeys] = useState<{ appName: string; keys: { mode: ApiKeyMode; rawKey: string }[] } | null>(null);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const applications = useMemo(() => groupKeysByApplication(keys ?? []), [keys]);
  const flowByKeyId = useMemo(() => {
    const map = new Map<string, { credited: number; debited: number }>();
    for (const flow of pointsFlows ?? []) {
      map.set(flow.apiKeyId, { credited: flow.credited, debited: flow.debited });
    }
    return map;
  }, [pointsFlows]);
  const filteredApps = applications.filter((app) =>
    app.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const activeKeysCount = keys?.filter((k) => k.active).length ?? 0;

  const handleCreateApp = async (e: React.FormEvent) => {
    e.preventDefault();
    const name = newAppName.trim();
    if (!name) return;
    if (applications.some((a) => a.name.toLowerCase() === name.toLowerCase())) {
      setActionError(`Une application nommée « ${name} » existe déjà.`);
      return;
    }
    setCreating(true);
    setActionError(null);
    try {
      // Une application naît avec sa paire de clés TEST + LIVE
      const test = await apiKeyApi.create({ name, mode: "TEST" });
      const live = await apiKeyApi.create({ name, mode: "LIVE" });
      const revealed = [
        ...(test.rawKey ? [{ mode: "TEST" as ApiKeyMode, rawKey: test.rawKey }] : []),
        ...(live.rawKey ? [{ mode: "LIVE" as ApiKeyMode, rawKey: live.rawKey }] : []),
      ];
      if (revealed.length > 0) setRevealedKeys({ appName: name, keys: revealed });
      setNewAppName("");
      refetch();
      refetchFlows();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Erreur lors de la création de l'application");
    } finally {
      setCreating(false);
    }
  };

  const handleAddKey = async (appName: string, mode: ApiKeyMode) => {
    setAddingKeyFor(`${appName}:${mode}`);
    setActionError(null);
    try {
      const created = await apiKeyApi.create({ name: appName, mode });
      if (created.rawKey) {
        setRevealedKeys({ appName, keys: [{ mode, rawKey: created.rawKey }] });
      }
      refetch();
      refetchFlows();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Erreur lors de la création de la clé");
    } finally {
      setAddingKeyFor(null);
    }
  };

  const handleRevoke = async (id: string) => {
    setActionError(null);
    try {
      await apiKeyApi.revoke(id);
      refetch();
      refetchFlows();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Erreur lors de la révocation");
    }
  };

  const handleCopy = (rawKey: string) => {
    navigator.clipboard.writeText(rawKey);
    setCopiedKey(rawKey);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-6">
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            Mes <span className="text-primary italic">Applications</span>
          </h1>
          <p className="text-muted-foreground text-sm font-sans italic">
            Vos applications et leurs clés d&apos;API pour intégrer le moteur de fidélité.
          </p>
        </div>

        <div className="relative w-full lg:w-96 group">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Search className="w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
          </div>
          <input
            type="text"
            placeholder="Rechercher une application..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-card border border-border rounded-2xl pl-12 pr-4 py-3.5 text-sm focus:outline-none focus:ring-4 focus:ring-primary/10 focus:border-primary/50 shadow-sm transition-all placeholder:text-muted-foreground/60"
          />
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div className="bg-card border border-border rounded-2xl p-6 flex items-center gap-4 shadow-sm group hover:border-primary/20 transition-all">
          <div className="p-3 bg-secondary rounded-xl text-primary font-bold">
            <Boxes className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Applications</p>
            <p className="text-xl font-bold font-mono tracking-tight">{applications.length}</p>
          </div>
        </div>
        <div className="bg-card border border-border rounded-2xl p-6 flex items-center gap-4 shadow-sm">
          <div className="p-3 bg-emerald-50 rounded-xl text-emerald-600">
            <Key className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-black uppercase text-muted-foreground tracking-widest">Clés actives</p>
            <p className="text-xl font-bold font-mono tracking-tight">{activeKeysCount}</p>
          </div>
        </div>
      </div>

      {/* Clés brutes révélées (une seule fois) */}
      {revealedKeys && (
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-yellow-700 text-sm font-medium">
            <AlertTriangle className="w-4 h-4" />
            Clés de « {revealedKeys.appName} » — copiez-les maintenant, elles ne seront plus affichées.
          </div>
          {revealedKeys.keys.map(({ mode, rawKey }) => (
            <div key={rawKey} className="flex items-center gap-2">
              <span className={`text-xs px-2 py-0.5 rounded-full border font-semibold ${MODE_BADGE[mode]}`}>
                {mode}
              </span>
              <code className="flex-1 bg-card border border-border rounded-md px-3 py-2 text-xs font-mono overflow-x-auto">
                {rawKey}
              </code>
              <button
                onClick={() => handleCopy(rawKey)}
                className="p-2 rounded-md border border-border hover:bg-secondary transition-colors"
                title="Copier la clé"
              >
                {copiedKey === rawKey ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          ))}
          <button
            onClick={() => setRevealedKeys(null)}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            Fermer
          </button>
        </div>
      )}

      {actionError && (
        <div className="p-4 flex items-start gap-3 text-sm text-destructive bg-destructive/5 border border-destructive/20 rounded-xl">
          <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {/* Créer une application */}
      <form
        onSubmit={handleCreateApp}
        className="bg-card border border-border rounded-2xl p-5 flex flex-col sm:flex-row gap-3 sm:items-end shadow-sm"
      >
        <div className="flex-1 space-y-1.5">
          <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
            Nom de l&apos;application
          </label>
          <input
            value={newAppName}
            onChange={(e) => setNewAppName(e.target.value)}
            placeholder="Ex : Mon site e-commerce"
            className="w-full px-4 py-2.5 text-sm rounded-lg border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
          />
        </div>
        <button
          type="submit"
          disabled={creating || !newAppName.trim()}
          className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
        >
          <Plus className="w-4 h-4" />
          {creating ? "Création…" : "Nouvelle application"}
        </button>
      </form>

      {/* Liste des applications */}
      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-32 bg-muted rounded-2xl animate-pulse" />
          ))}
        </div>
      ) : error ? (
        <div className="p-8 text-center text-sm text-destructive flex items-center justify-center gap-2 border border-destructive/20 bg-destructive/5 rounded-2xl">
          <AlertTriangle className="w-4 h-4" />
          {error}
        </div>
      ) : filteredApps.length === 0 ? (
        <div className="border border-border bg-card rounded-2xl p-12 text-center space-y-3">
          <AppWindow className="w-8 h-8 mx-auto text-muted-foreground/50" />
          <p className="text-sm text-muted-foreground">
            {applications.length === 0
              ? "Aucune application pour le moment. Créez-en une pour obtenir vos clés d'API."
              : "Aucune application ne correspond à votre recherche."}
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {filteredApps.map((app) => {
            const hasActiveMode = (mode: ApiKeyMode) =>
              app.keys.some((k) => k.mode === mode && k.active);
            return (
              <div
                key={app.name}
                className="border border-border bg-card rounded-2xl overflow-hidden shadow-sm shadow-primary/5"
              >
                {/* En-tête application */}
                <div className="bg-secondary/30 px-6 py-4 border-b border-border flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-secondary border border-border flex items-center justify-center text-primary">
                      <AppWindow className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="font-bold text-sm text-foreground">{app.name}</h3>
                      <p className="text-[10px] text-muted-foreground font-mono uppercase tracking-widest">
                        {app.keys.length} clé{app.keys.length > 1 ? "s" : ""} · créée le{" "}
                        {new Date(app.createdAt).toLocaleDateString("fr-FR")}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {(["TEST", "LIVE"] as ApiKeyMode[]).map(
                      (mode) =>
                        !hasActiveMode(mode) && (
                          <button
                            key={mode}
                            onClick={() => handleAddKey(app.name, mode)}
                            disabled={addingKeyFor === `${app.name}:${mode}`}
                            className="inline-flex items-center gap-1.5 text-xs font-semibold border border-border px-3 py-1.5 rounded-lg hover:bg-secondary transition-all disabled:opacity-50"
                          >
                            <Plus className="w-3 h-3" />
                            Clé {mode}
                          </button>
                        )
                    )}
                  </div>
                </div>

                {/* Clés de l'application */}
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-border text-left text-[10px] text-muted-foreground uppercase font-black tracking-widest bg-muted/20">
                        <th className="px-6 py-3">Clé</th>
                        <th className="px-4 py-3">Mode</th>
                        <th className="px-4 py-3">Statut</th>
                        <th className="px-4 py-3">Flux de points</th>
                        <th className="px-4 py-3">Dernière utilisation</th>
                        <th className="px-6 py-3 text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border/60">
                      {app.keys.map((k) => {
                        const flow = flowByKeyId.get(k.id);
                        return (
                        <tr key={k.id} className="hover:bg-secondary/20 transition-colors">
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-2.5">
                              <Key className="w-3.5 h-3.5 text-muted-foreground flex-shrink-0" />
                              <span className="font-mono text-xs text-foreground">{k.keyPrefix}</span>
                            </div>
                          </td>
                          <td className="px-4 py-4">
                            <span className={`text-xs px-2.5 py-0.5 rounded-full border font-semibold ${MODE_BADGE[k.mode]}`}>
                              {k.mode}
                            </span>
                          </td>
                          <td className="px-4 py-4">
                            <span
                              className={`text-xs px-2.5 py-0.5 rounded-full border font-semibold ${
                                k.active
                                  ? "bg-green-500/10 text-green-600 border-green-500/20"
                                  : "bg-muted text-muted-foreground border-border"
                              }`}
                            >
                              {k.active ? "Active" : "Révoquée"}
                            </span>
                          </td>
                          <td className="px-4 py-4">
                            <div className="flex items-center gap-2 font-mono text-xs font-bold whitespace-nowrap">
                              <span className="text-emerald-600">+{(flow?.credited ?? 0).toLocaleString()}</span>
                              <span className="text-muted-foreground font-normal">/</span>
                              <span className="text-rose-600">-{(flow?.debited ?? 0).toLocaleString()}</span>
                              <span className="text-[10px] text-muted-foreground font-normal">pts</span>
                            </div>
                          </td>
                          <td className="px-4 py-4 text-xs text-muted-foreground">
                            {k.lastUsedAt
                              ? new Date(k.lastUsedAt).toLocaleString("fr-FR", {
                                  dateStyle: "short",
                                  timeStyle: "short",
                                })
                              : "Jamais"}
                          </td>
                          <td className="px-6 py-4 text-right">
                            {k.active && (
                              <button
                                onClick={() => handleRevoke(k.id)}
                                className="p-1.5 rounded-md hover:bg-destructive/10 text-destructive transition-colors"
                                title="Révoquer cette clé"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            )}
                          </td>
                        </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
