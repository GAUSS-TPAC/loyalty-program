"use client";

import { useState } from "react";
import {
    Gift,
    Send,
    RefreshCw,
    CheckCircle2,
    AlertTriangle,
    Wifi,
    WifiOff,
    ArrowUpCircle,
    ArrowDownCircle,
    Info,
} from "lucide-react";
import { bonificationApi, type BonificationTransactionResponse } from "@/lib/api";
import { useBonificationStatus } from "@/hooks/useBackend";

// ─── Page principale ──────────────────────────────────────────────────────────

export default function BonificationPage() {
    const {
        data: status,
        isLoading: statusLoading,
        error: statusError,
        refetch: refetchStatus,
    } = useBonificationStatus();

    // Formulaire
    const [clientLogin, setClientLogin] = useState("");
    const [amount, setAmount] = useState("");
    const [isDebit, setIsDebit] = useState(false);

    // UI
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [result, setResult] = useState<BonificationTransactionResponse | null>(null);
    const [txError, setTxError] = useState<string | null>(null);
    const [history, setHistory] = useState<BonificationTransactionResponse[]>([]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!clientLogin.trim() || !amount) return;
        setIsSubmitting(true);
        setResult(null);
        setTxError(null);

        try {
            const res = await bonificationApi.submitTransaction({
                amount: Number(amount),
                clientLogin,
                debit: isDebit,
            });
            setResult(res);
            setHistory((prev) => [res, ...prev.slice(0, 9)]);
        } catch (err) {
            setTxError(
                err instanceof Error ? err.message : "Erreur lors de la transaction"
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="space-y-1">
                <h1 className="text-3xl font-semibold tracking-tight">Bonification</h1>
                <p className="text-muted-foreground text-sm">
                    Intégration avec l&apos;API externe de bonification. Gérez les crédits et
                    débits vers la plateforme partenaire.
                </p>
            </div>

            {/* ── Statut de connexion ──────────────────────────────────────────────── */}
            <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
                <div className="bg-secondary px-6 py-4 border-b border-border flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                        {statusLoading ? (
                            <RefreshCw className="w-5 h-5 text-primary animate-spin" />
                        ) : status?.connected ? (
                            <Wifi className="w-5 h-5 text-green-600" />
                        ) : (
                            <WifiOff className="w-5 h-5 text-destructive" />
                        )}
                        <h3 className="font-semibold text-foreground">
                            Statut de l&apos;intégration
                        </h3>
                    </div>
                    <button
                        onClick={refetchStatus}
                        className="text-xs text-muted-foreground hover:text-foreground flex items-center gap-1.5 border border-border px-3 py-1.5 rounded-lg hover:bg-secondary transition-all"
                    >
                        <RefreshCw className="w-3 h-3" /> Tester
                    </button>
                </div>

                <div className="p-6">
                    {statusLoading && (
                        <div className="flex items-center gap-3 text-sm text-muted-foreground">
                            <RefreshCw className="w-4 h-4 animate-spin" />
                            Vérification de la connexion…
                        </div>
                    )}

                    {statusError && (
                        <div className="flex items-start gap-3 text-sm text-destructive">
                            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-semibold">Impossible de joindre le backend</p>
                                <p className="text-xs mt-0.5 text-destructive/80">{statusError}</p>
                            </div>
                        </div>
                    )}

                    {status && (
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                            {[
                                {
                                    label: "Intégration activée",
                                    val: status.enabled ? "Oui" : "Non",
                                    ok: status.enabled,
                                },
                                {
                                    label: "API joignable",
                                    val: status.connected ? "Connecté" : "Hors ligne",
                                    ok: status.connected,
                                },
                                {
                                    label: "URL de l'API",
                                    val: status.baseUrl,
                                    ok: true,
                                },
                            ].map(({ label, val, ok }) => (
                                <div
                                    key={label}
                                    className="p-4 rounded-lg border border-border bg-muted/20 space-y-1"
                                >
                                    <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">
                                        {label}
                                    </p>
                                    <p
                                        className={`text-sm font-semibold truncate ${ok ? "text-foreground" : "text-destructive"
                                            }`}
                                    >
                                        {val}
                                    </p>
                                </div>
                            ))}

                            {status.message && (
                                <div className="sm:col-span-3 flex items-start gap-2 text-xs text-muted-foreground bg-secondary border border-border rounded-lg p-3">
                                    <Info className="w-4 h-4 text-primary flex-shrink-0" />
                                    <span>{status.message}</span>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* ── Formulaire transaction ────────────────────────────────────────── */}
                <div className="lg:col-span-2 space-y-5">
                    <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
                        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
                            <Send className="w-5 h-5 text-primary" />
                            <h3 className="font-semibold text-foreground">
                                Soumettre une transaction
                            </h3>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-5">
                            {/* Type de transaction */}
                            <div className="space-y-2">
                                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                    Type d'opération *
                                </label>
                                <div className="flex gap-3">
                                    <button
                                        type="button"
                                        onClick={() => setIsDebit(false)}
                                        className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-lg border text-sm font-medium transition-all ${!isDebit
                                                ? "bg-primary text-primary-foreground border-primary shadow-sm"
                                                : "bg-background text-muted-foreground border-border hover:bg-secondary"
                                            }`}
                                    >
                                        <ArrowUpCircle className="w-4 h-4" />
                                        Crédit
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setIsDebit(true)}
                                        className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-lg border text-sm font-medium transition-all ${isDebit
                                                ? "bg-destructive text-white border-destructive shadow-sm"
                                                : "bg-background text-muted-foreground border-border hover:bg-secondary"
                                            }`}
                                    >
                                        <ArrowDownCircle className="w-4 h-4" />
                                        Débit
                                    </button>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {/* Login client */}
                                <div className="space-y-1.5">
                                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                        Login client *
                                    </label>
                                    <input
                                        type="text"
                                        required
                                        value={clientLogin}
                                        onChange={(e) => setClientLogin(e.target.value)}
                                        placeholder="Ex: client@email.com"
                                        className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                    />
                                </div>

                                {/* Montant */}
                                <div className="space-y-1.5">
                                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                        Montant *
                                    </label>
                                    <input
                                        type="number"
                                        min="1"
                                        step="0.01"
                                        required
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        placeholder="Ex: 1000"
                                        className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                    />
                                </div>
                            </div>

                            <div className="flex justify-end pt-2">
                                <button
                                    type="submit"
                                    disabled={isSubmitting || !clientLogin.trim() || !amount}
                                    className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
                                >
                                    {isSubmitting ? (
                                        <RefreshCw className="w-4 h-4 animate-spin" />
                                    ) : (
                                        <Send className="w-4 h-4" />
                                    )}
                                    {isSubmitting ? "Envoi…" : "Envoyer"}
                                </button>
                            </div>
                        </form>
                    </div>

                    {/* Résultat */}
                    {txError && (
                        <div className="p-4 flex items-start gap-3 text-sm text-destructive bg-destructive/5 border border-destructive/20 rounded-xl">
                            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-semibold">Erreur de transaction</p>
                                <p className="text-xs mt-0.5 text-destructive/80">{txError}</p>
                            </div>
                        </div>
                    )}

                    {result && (
                        <div
                            className={`rounded-xl border p-5 shadow-sm ${result.status === "SUCCESS" || result.status === "CREATED"
                                    ? "border-green-200 bg-green-50"
                                    : "border-destructive/20 bg-destructive/5"
                                }`}
                        >
                            <div className="flex items-center gap-2 mb-3">
                                <CheckCircle2 className="w-5 h-5 text-green-600" />
                                <h4 className="font-semibold text-sm text-green-800">
                                    Transaction soumise
                                </h4>
                            </div>
                            <div className="grid grid-cols-2 gap-3 text-xs">
                                <div>
                                    <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                                        Transaction ID
                                    </p>
                                    <p className="font-mono text-foreground">
                                        {result.transactionId}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                                        Statut
                                    </p>
                                    <p className="font-semibold text-green-700">{result.status}</p>
                                </div>
                                <div>
                                    <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                                        Montant
                                    </p>
                                    <p className="font-mono font-bold text-primary text-base">
                                        {result.debit ? "-" : "+"}
                                        {result.amount}
                                    </p>
                                </div>
                                {result.message && (
                                    <div className="col-span-2">
                                        <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                                            Message
                                        </p>
                                        <p className="text-foreground">{result.message}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>

                {/* ── Historique ───────────────────────────────────────────────────── */}
                <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden self-start">
                    <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2">
                        <Gift className="w-4 h-4 text-primary" />
                        <h3 className="font-semibold text-sm text-foreground">
                            Historique session
                        </h3>
                    </div>

                    {history.length === 0 ? (
                        <div className="p-6 text-center text-xs text-muted-foreground italic">
                            Aucune transaction cette session
                        </div>
                    ) : (
                        <div className="divide-y divide-border">
                            {history.map((h, i) => (
                                <div
                                    key={i}
                                    className="px-5 py-3 flex items-center justify-between hover:bg-muted/10 transition-colors"
                                >
                                    <div>
                                        <p className="text-xs font-medium text-foreground">
                                            {h.clientLogin}
                                        </p>
                                        <p className="text-xs text-muted-foreground font-mono">
                                            {h.transactionId?.substring(0, 8)}…
                                        </p>
                                    </div>
                                    <div className="text-right">
                                        <p
                                            className={`text-xs font-bold font-mono ${h.debit ? "text-destructive" : "text-green-600"
                                                }`}
                                        >
                                            {h.debit ? "-" : "+"}
                                            {h.amount}
                                        </p>
                                        <p className="text-xs text-muted-foreground">{h.status}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
