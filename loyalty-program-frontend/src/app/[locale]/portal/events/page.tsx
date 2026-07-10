"use client";

import { useEffect, useState } from "react";
import {
    Zap,
    Send,
    RefreshCw,
    CheckCircle2,
    AlertTriangle,
    Info,
    Clock,
    ChevronRight,
    WifiOff,
} from "lucide-react";
import { eventsApi, type EventProcessingResponse } from "@/lib/api";
import { enqueue, flushQueue, getQueue, type QueuedEvent } from "@/lib/offlineQueue";

// ─── Constantes ───────────────────────────────────────────────────────────────

const EVENT_TYPES = [
    { value: "purchase.completed", label: "Achat complété" },
    { value: "account.created", label: "Compte créé" },
    { value: "review.posted", label: "Avis publié" },
    { value: "referral.converted", label: "Parrainage converti" },
    { value: "topup.completed", label: "Recharge effectuée" },
];

// ─── Composant résultat ───────────────────────────────────────────────────────

function ResultCard({ result }: { result: EventProcessingResponse }) {
    return (
        <div
            className={`rounded-xl border p-5 shadow-sm ${result.processed
                    ? "border-green-200 bg-green-50"
                    : "border-destructive/20 bg-destructive/5"
                }`}
        >
            <div className="flex items-center gap-2 mb-3">
                {result.processed ? (
                    <CheckCircle2 className="w-5 h-5 text-green-600" />
                ) : (
                    <AlertTriangle className="w-5 h-5 text-destructive" />
                )}
                <h4
                    className={`font-semibold text-sm ${result.processed ? "text-green-800" : "text-destructive"
                        }`}
                >
                    {result.processed ? "Événement traité avec succès" : "Traitement échoué"}
                </h4>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
                <div>
                    <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                        Event ID
                    </p>
                    <p className="font-mono text-foreground">{result.eventId}</p>
                </div>
                <div>
                    <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-0.5">
                        Points attribués
                    </p>
                    <p className="font-mono font-bold text-primary text-base">
                        +{result.pointsAwarded}
                    </p>
                </div>
                {result.rulesApplied?.length > 0 && (
                    <div className="col-span-2">
                        <p className="text-muted-foreground uppercase tracking-wider font-semibold mb-1">
                            Règles appliquées
                        </p>
                        <div className="flex flex-wrap gap-1">
                            {result.rulesApplied.map((r) => (
                                <span
                                    key={r}
                                    className="text-xs font-mono bg-primary/10 text-primary px-2 py-0.5 rounded"
                                >
                                    {r}
                                </span>
                            ))}
                        </div>
                    </div>
                )}
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
    );
}

// ─── Page principale ──────────────────────────────────────────────────────────

export default function EventsPage() {
    // Form
    const [eventType, setEventType] = useState("purchase.completed");
    const [memberId, setMemberId] = useState("");
    const [amount, setAmount] = useState("");
    const [idempotencyKey, setIdempotencyKey] = useState("");
    const [useIdempotency, setUseIdempotency] = useState(false);

    // UI
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [result, setResult] = useState<EventProcessingResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [justQueued, setJustQueued] = useState(false);
    const [queuedEvents, setQueuedEvents] = useState<QueuedEvent[]>([]);
    const [history, setHistory] = useState<
        Array<{
            ts: string;
            eventType: string;
            memberId: string;
            result: EventProcessingResponse;
        }>
    >([]);

    const refreshQueue = () => setQueuedEvents(getQueue());

    useEffect(() => {
        refreshQueue();
        const handleOnline = () => {
            flushQueue().then(refreshQueue);
        };
        window.addEventListener("online", handleOnline);
        return () => window.removeEventListener("online", handleOnline);
    }, []);

    const generateKey = () => {
        setIdempotencyKey(
            `idem-${Date.now()}-${Math.random().toString(36).substring(2, 8)}`
        );
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!memberId.trim()) return;
        setIsSubmitting(true);
        setResult(null);
        setError(null);
        setJustQueued(false);

        const payload = {
            eventType,
            memberId,
            occurredAt: new Date().toISOString(),
            payload: amount ? { amount: Number(amount) } : undefined,
        };
        const key = useIdempotency && idempotencyKey ? idempotencyKey : undefined;

        // Offline: don't even attempt the fetch, queue straight away.
        if (!navigator.onLine) {
            enqueue(payload, key);
            refreshQueue();
            setJustQueued(true);
            setIsSubmitting(false);
            return;
        }

        try {
            const res = await eventsApi.processEvent(payload, key);
            setResult(res);
            setHistory((prev) => [
                { ts: new Date().toISOString(), eventType, memberId, result: res },
                ...prev.slice(0, 9),
            ]);
        } catch (err) {
            // A dropped connection surfaces as a raw TypeError ("Failed to fetch"),
            // unlike a real HTTP error which requestWithHeaders wraps as Error("[status] ...").
            if (!navigator.onLine || err instanceof TypeError) {
                enqueue(payload, key);
                refreshQueue();
                setJustQueued(true);
            } else {
                setError(err instanceof Error ? err.message : "Erreur inconnue");
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="space-y-1">
                <h1 className="text-3xl font-semibold tracking-tight">
                    Déclencheur d&apos;événements
                </h1>
                <p className="text-muted-foreground text-sm">
                    Envoyez manuellement un événement loyalty pour tester le moteur de
                    règles en temps réel.
                </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* ── Formulaire ───────────────────────────────────────────────────── */}
                <div className="lg:col-span-2 space-y-5">
                    <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
                        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
                            <Zap className="w-5 h-5 text-primary" />
                            <h3 className="font-semibold text-foreground">
                                Soumettre un événement
                            </h3>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-5">
                            {/* Type d'événement */}
                            <div className="space-y-1.5">
                                <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                    Type d&apos;événement *
                                </label>
                                <select
                                    value={eventType}
                                    onChange={(e) => setEventType(e.target.value)}
                                    className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                >
                                    {EVENT_TYPES.map((et) => (
                                        <option key={et.value} value={et.value}>
                                            {et.label} ({et.value})
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {/* Membre */}
                                <div className="space-y-1.5">
                                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                        Member ID *
                                    </label>
                                    <input
                                        type="text"
                                        required
                                        value={memberId}
                                        onChange={(e) => setMemberId(e.target.value)}
                                        placeholder="uuid du membre"
                                        className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                    />
                                </div>

                                {/* Montant */}
                                <div className="space-y-1.5">
                                    <label className="text-xs font-semibold uppercase text-muted-foreground tracking-wider ml-1">
                                        Montant (optionnel)
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        placeholder="Ex: 5000"
                                        className="w-full bg-background border border-border rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                    />
                                </div>
                            </div>

                            {/* Idempotency Key */}
                            <div className="space-y-3 pt-1 border-t border-border">
                                <div className="flex items-center gap-2.5">
                                    <input
                                        id="useIdempotency"
                                        type="checkbox"
                                        checked={useIdempotency}
                                        onChange={(e) => setUseIdempotency(e.target.checked)}
                                        className="w-4 h-4 text-primary focus:ring-primary border-border rounded cursor-pointer"
                                    />
                                    <label
                                        htmlFor="useIdempotency"
                                        className="text-sm font-medium text-foreground select-none cursor-pointer"
                                    >
                                        Utiliser une clé d&apos;idempotence
                                    </label>
                                </div>

                                {useIdempotency && (
                                    <div className="flex gap-2">
                                        <input
                                            type="text"
                                            value={idempotencyKey}
                                            onChange={(e) => setIdempotencyKey(e.target.value)}
                                            placeholder="Idempotency-Key..."
                                            className="flex-1 bg-background border border-border rounded-lg px-4 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm"
                                        />
                                        <button
                                            type="button"
                                            onClick={generateKey}
                                            className="px-3 py-2 text-xs font-medium border border-border rounded-lg hover:bg-secondary text-muted-foreground hover:text-foreground transition-all"
                                        >
                                            Générer
                                        </button>
                                    </div>
                                )}
                            </div>

                            {/* Bouton submit */}
                            <div className="flex justify-end pt-2">
                                <button
                                    type="submit"
                                    disabled={isSubmitting || !memberId.trim()}
                                    className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary/90 transition-all shadow-md active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
                                >
                                    {isSubmitting ? (
                                        <RefreshCw className="w-4 h-4 animate-spin" />
                                    ) : (
                                        <Send className="w-4 h-4" />
                                    )}
                                    {isSubmitting ? "Envoi en cours…" : "Envoyer l'événement"}
                                </button>
                            </div>
                        </form>
                    </div>

                    {/* Résultat */}
                    {justQueued && (
                        <div className="p-4 flex items-start gap-3 text-sm text-amber-800 bg-amber-50 border border-amber-200 rounded-xl">
                            <WifiOff className="w-4 h-4 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-semibold">Mis en file d&apos;attente</p>
                                <p className="text-xs mt-0.5 text-amber-700">
                                    Vous êtes hors ligne — cet événement sera envoyé automatiquement
                                    dès le retour de la connexion.
                                </p>
                            </div>
                        </div>
                    )}

                    {error && (
                        <div className="p-4 flex items-start gap-3 text-sm text-destructive bg-destructive/5 border border-destructive/20 rounded-xl">
                            <AlertTriangle className="w-4 h-4 mt-0.5 flex-shrink-0" />
                            <div>
                                <p className="font-semibold">Erreur lors du traitement</p>
                                <p className="text-xs mt-0.5 text-destructive/80">{error}</p>
                            </div>
                        </div>
                    )}

                    {result && <ResultCard result={result} />}
                </div>

                {/* ── Colonne droite ───────────────────────────────────────────────── */}
                <div className="space-y-6">
                    {/* Info panel */}
                    <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
                        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2">
                            <Info className="w-4 h-4 text-primary" />
                            <h3 className="font-semibold text-sm text-foreground">
                                Comment ça marche
                            </h3>
                        </div>
                        <div className="p-5 space-y-3 text-xs text-muted-foreground">
                            {[
                                "L'événement est envoyé à POST /api/v1/events",
                                "Le moteur de règles évalue les règles actives dans l'ordre de priorité",
                                "Les points sont crédités au membre si une règle correspond",
                                "Une clé d'idempotence évite les doubles traitements",
                            ].map((step, i) => (
                                <div key={i} className="flex items-start gap-2">
                                    <ChevronRight className="w-3.5 h-3.5 text-primary mt-0.5 flex-shrink-0" />
                                    <span>{step}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* File d'attente offline */}
                    {queuedEvents.length > 0 && (
                        <div className="border border-amber-200 bg-amber-50 rounded-xl shadow-sm overflow-hidden">
                            <div className="bg-amber-100 px-6 py-4 border-b border-amber-200 flex items-center gap-2">
                                <WifiOff className="w-4 h-4 text-amber-700" />
                                <h3 className="font-semibold text-sm text-amber-900">
                                    En attente de synchronisation ({queuedEvents.length})
                                </h3>
                            </div>
                            <div className="divide-y divide-amber-200">
                                {queuedEvents.map((q) => (
                                    <div key={q.id} className="px-5 py-3">
                                        <p className="text-xs font-mono text-amber-800">
                                            {q.payload.eventType}
                                        </p>
                                        <p className="text-xs text-amber-700">
                                            {q.payload.memberId.substring(0, 8)}… — mis en file à{" "}
                                            {new Date(q.queuedAt).toLocaleTimeString("fr-FR")}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Historique local */}
                    {history.length > 0 && (
                        <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden">
                            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2">
                                <Clock className="w-4 h-4 text-primary" />
                                <h3 className="font-semibold text-sm text-foreground">
                                    Historique session
                                </h3>
                            </div>
                            <div className="divide-y divide-border">
                                {history.map((h, i) => (
                                    <div
                                        key={i}
                                        className="px-5 py-3 flex items-center justify-between hover:bg-muted/10 transition-colors"
                                    >
                                        <div>
                                            <p className="text-xs font-mono text-primary">
                                                {h.eventType}
                                            </p>
                                            <p className="text-xs text-muted-foreground">
                                                {h.memberId.substring(0, 8)}…
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <p
                                                className={`text-xs font-bold ${h.result.processed
                                                        ? "text-green-600"
                                                        : "text-destructive"
                                                    }`}
                                            >
                                                +{h.result.pointsAwarded} pts
                                            </p>
                                            <p className="text-xs text-muted-foreground font-mono">
                                                {new Date(h.ts).toLocaleTimeString("fr-FR")}
                                            </p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
