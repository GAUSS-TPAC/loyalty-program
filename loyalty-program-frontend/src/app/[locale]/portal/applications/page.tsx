"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import {
  Boxes,
  Plus,
  Trash2,
  Copy,
  Check,
  AlertTriangle,
  RefreshCw,
  KeyRound,
  Globe,
  Webhook,
} from "lucide-react";
import { useApplications } from "@/hooks/useBackend";
import { applicationApi, type ApiKeyMode, type ApplicationResponse } from "@/lib/api";

const EVENT_TYPES = [
  "points.earned",
  "points.redeemed",
  "reward.granted",
  "reward.redeemed",
  "tier.changed",
];

interface RevealedSecrets {
  publicKey: string;
  privateKey?: string | null;
  webhookSecret?: string | null;
}

function SecretRow({ label, value }: { label: string; value: string }) {
  const [copied, setCopied] = useState(false);
  const handleCopy = () => {
    navigator.clipboard.writeText(value);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  return (
    <div className="space-y-1">
      <span className="text-xs font-medium text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2">
        <code className="flex-1 bg-card border border-border rounded-md px-3 py-2 text-xs font-mono overflow-x-auto">
          {value}
        </code>
        <button
          onClick={handleCopy}
          className="p-2 rounded-md border border-border hover:bg-secondary transition-colors"
        >
          {copied ? <Check className="w-4 h-4 text-green-600" /> : <Copy className="w-4 h-4" />}
        </button>
      </div>
    </div>
  );
}

export default function ApplicationsPage() {
  const t = useTranslations("Developer");
  const { data: apps, isLoading, error, refetch } = useApplications();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [websiteUrl, setWebsiteUrl] = useState("");
  const [callbackUrl, setCallbackUrl] = useState("");
  const [mode, setMode] = useState<ApiKeyMode>("TEST");
  const [selectedEvents, setSelectedEvents] = useState<string[]>([...EVENT_TYPES]);
  const [creating, setCreating] = useState(false);
  const [revealed, setRevealed] = useState<RevealedSecrets | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  const toggleEvent = (code: string) =>
    setSelectedEvents((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code]
    );

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setCreating(true);
    try {
      const created = await applicationApi.create({
        name: name.trim(),
        description: description.trim() || undefined,
        websiteUrl: websiteUrl.trim() || undefined,
        mode,
        callbackUrl: callbackUrl.trim() || undefined,
        eventTypes: callbackUrl.trim() ? selectedEvents : undefined,
      });
      setRevealed({
        publicKey: created.publicKey,
        privateKey: created.privateKey,
        webhookSecret: created.webhookSecret,
      });
      setName("");
      setDescription("");
      setWebsiteUrl("");
      setCallbackUrl("");
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setCreating(false);
    }
  };

  const handleRotatePrivateKey = async (app: ApplicationResponse) => {
    if (!confirm(t("appsRotateKeyConfirm"))) return;
    setBusyId(app.id);
    try {
      const rotated = await applicationApi.rotatePrivateKey(app.id);
      setRevealed({ publicKey: app.publicKey, privateKey: rotated.privateKey });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setBusyId(null);
    }
  };

  const handleRotateWebhookSecret = async (app: ApplicationResponse) => {
    if (!confirm(t("appsRotateSecretConfirm"))) return;
    setBusyId(app.id);
    try {
      const rotated = await applicationApi.rotateWebhookSecret(app.id);
      setRevealed({ publicKey: app.publicKey, webhookSecret: rotated.webhookSecret });
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async (app: ApplicationResponse) => {
    if (!confirm(t("appsDeleteConfirm"))) return;
    setBusyId(app.id);
    try {
      await applicationApi.remove(app.id);
      refetch();
    } catch (err) {
      console.error(err);
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("applicationsTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("applicationsDescription")}</p>
      </div>

      {revealed && (
        <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-5 space-y-4">
          <div className="flex items-center gap-2 text-yellow-700 text-sm font-medium">
            <AlertTriangle className="w-4 h-4" />
            {t("appsRevealWarning")}
          </div>
          <SecretRow label={t("appsPublicKey")} value={revealed.publicKey} />
          {revealed.privateKey && <SecretRow label={t("appsPrivateKey")} value={revealed.privateKey} />}
          {revealed.webhookSecret && (
            <SecretRow label={t("appsWebhookSecret")} value={revealed.webhookSecret} />
          )}
          <button
            onClick={() => setRevealed(null)}
            className="text-xs text-muted-foreground hover:text-foreground"
          >
            {t("appsDismiss")}
          </button>
        </div>
      )}

      <form onSubmit={handleCreate} className="bg-card border border-border rounded-xl p-5 space-y-4">
        <h2 className="text-sm font-semibold text-foreground flex items-center gap-2">
          <Plus className="w-4 h-4" />
          {t("appsCreateTitle")}
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">{t("appsNameLabel")} *</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Boutique e-commerce"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Mode</label>
            <select
              value={mode}
              onChange={(e) => setMode(e.target.value as ApiKeyMode)}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            >
              <option value="TEST">Test</option>
              <option value="LIVE">Live</option>
            </select>
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">{t("appsWebsiteLabel")}</label>
            <input
              value={websiteUrl}
              onChange={(e) => setWebsiteUrl(e.target.value)}
              placeholder="https://shop.example.com"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">{t("appsDescriptionLabel")}</label>
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <div className="space-y-1.5 sm:col-span-2">
            <label className="text-xs font-medium text-muted-foreground">{t("appsCallbackLabel")}</label>
            <input
              value={callbackUrl}
              onChange={(e) => setCallbackUrl(e.target.value)}
              placeholder="https://shop.example.com/loyalty/callback"
              className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
            <p className="text-xs text-muted-foreground">{t("appsCallbackHint")}</p>
          </div>
        </div>
        {callbackUrl.trim() && (
          <div className="space-y-2">
            <span className="text-xs font-medium text-muted-foreground">{t("appsEventsLabel")}</span>
            <div className="flex flex-wrap gap-2">
              {EVENT_TYPES.map((code) => (
                <button
                  key={code}
                  type="button"
                  onClick={() => toggleEvent(code)}
                  className={`text-xs px-2.5 py-1 rounded-full border transition-colors ${
                    selectedEvents.includes(code)
                      ? "bg-primary/10 text-primary border-primary/30"
                      : "bg-background text-muted-foreground border-border"
                  }`}
                >
                  {code}
                </button>
              ))}
            </div>
          </div>
        )}
        <button
          type="submit"
          disabled={creating || !name.trim()}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          {t("appsCreateButton")}
        </button>
      </form>

      <div className="space-y-4">
        {isLoading ? (
          <div className="bg-card border border-border rounded-xl p-6 space-y-3">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="h-16 bg-muted animate-pulse rounded-md" />
            ))}
          </div>
        ) : error ? (
          <div className="bg-card border border-border rounded-xl p-6 text-sm text-destructive flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </div>
        ) : !apps || apps.length === 0 ? (
          <div className="bg-card border border-border rounded-xl p-8 text-center text-sm text-muted-foreground">
            <Boxes className="w-6 h-6 mx-auto mb-2 opacity-50" />
            {t("appsEmpty")}
          </div>
        ) : (
          apps.map((app) => (
            <div key={app.id} className="bg-card border border-border rounded-xl p-5 space-y-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <h3 className="text-sm font-semibold text-foreground">{app.name}</h3>
                    <span
                      className={`text-xs px-2 py-0.5 rounded-full border ${
                        app.mode === "TEST"
                          ? "bg-blue-500/10 text-blue-600 border-blue-500/20"
                          : "bg-green-500/10 text-green-600 border-green-500/20"
                      }`}
                    >
                      {app.mode}
                    </span>
                    {!app.active && (
                      <span className="text-xs px-2 py-0.5 rounded-full border bg-muted text-muted-foreground border-border">
                        {t("appsInactive")}
                      </span>
                    )}
                  </div>
                  {app.description && (
                    <p className="text-xs text-muted-foreground">{app.description}</p>
                  )}
                  {app.websiteUrl && (
                    <p className="text-xs text-muted-foreground flex items-center gap-1">
                      <Globe className="w-3 h-3" />
                      {app.websiteUrl}
                    </p>
                  )}
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleRotatePrivateKey(app)}
                    disabled={busyId === app.id}
                    title={t("appsRotateKey")}
                    className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs rounded-md border border-border hover:bg-secondary transition-colors disabled:opacity-50"
                  >
                    <KeyRound className="w-3.5 h-3.5" />
                    {t("appsRotateKey")}
                  </button>
                  {app.webhookEndpointId && (
                    <button
                      onClick={() => handleRotateWebhookSecret(app)}
                      disabled={busyId === app.id}
                      title={t("appsRotateSecret")}
                      className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs rounded-md border border-border hover:bg-secondary transition-colors disabled:opacity-50"
                    >
                      <RefreshCw className="w-3.5 h-3.5" />
                      {t("appsRotateSecret")}
                    </button>
                  )}
                  <button
                    onClick={() => handleDelete(app)}
                    disabled={busyId === app.id}
                    className="p-1.5 rounded-md hover:bg-destructive/10 text-destructive transition-colors disabled:opacity-50"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                <div className="space-y-1">
                  <span className="font-medium text-muted-foreground">{t("appsPublicKey")}</span>
                  <div className="flex items-center gap-2">
                    <code className="flex-1 bg-background border border-border rounded-md px-2.5 py-1.5 font-mono overflow-x-auto">
                      {app.publicKey}
                    </code>
                    <button
                      onClick={() => navigator.clipboard.writeText(app.publicKey)}
                      className="p-1.5 rounded-md border border-border hover:bg-secondary transition-colors"
                    >
                      <Copy className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
                <div className="space-y-1">
                  <span className="font-medium text-muted-foreground">{t("appsPrivateKey")}</span>
                  <code className="block bg-background border border-border rounded-md px-2.5 py-1.5 font-mono text-muted-foreground">
                    {app.privateKeyPrefix ?? "sk_..."}
                  </code>
                </div>
              </div>
              {app.callbackUrl && (
                <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <Webhook className="w-3.5 h-3.5" />
                  {app.callbackUrl}
                </p>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
