package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité immuable représentant une transaction sur un wallet.
 *
 * <p>Chaque opération de crédit, débit ou annulation crée une nouvelle
 * WalletTransaction. Elles ne sont jamais modifiées ni supprimées une fois
 * persistées (append-only), ce qui garantit une piste d'audit complète.</p>
 *
 * <p>Garanties d'idempotence : {@code idempotencyKey} est unique par tenant,
 * permettant de rejouer en toute sécurité une requête sans double-débit.</p>
 */
@Getter
@Builder
public class WalletTransaction {

    // ── Identité ─────────────────────────────────────────────────────────────

    /** Identifiant unique de la transaction. */
    private final UUID id;

    /** Identifiant du wallet concerné. */
    private final UUID walletId;

    /** Identifiant du tenant pour le partitionnement multi-tenant. */
    private final UUID tenantId;

    // ── Clé d'idempotence ────────────────────────────────────────────────────

    /**
     * Clé d'idempotence fournie par l'appelant.
     * Si une transaction avec cette clé existe déjà, l'opération est ignorée
     * et le résultat existant est retourné (pas de double-débit).
     */
    private final String idempotencyKey;

    // ── Nature de la transaction ──────────────────────────────────────────────

    /** Type : CREDIT, DEBIT ou REVERSAL. */
    private final TransactionType type;

    /** Origine de la transaction (canal de paiement ou source loyalty). */
    private final TransactionSource source;

    /** État courant de la transaction dans son cycle de vie. */
    private final TransactionStatus status;

    // ── Montant & devise ──────────────────────────────────────────────────────

    /**
     * Montant de la transaction, toujours positif.
     * Le signe est déterminé par {@link TransactionType}.
     */
    private final BigDecimal amount;

    /**
     * Code devise ISO 4217 (ex. "XAF", "EUR", "USD").
     * Héritée de la WalletPolicy du tenant.
     */
    private final String currency;

    /**
     * Solde disponible du wallet après application de cette transaction.
     * Permet une réconciliation rapide sans recalcul de toutes les transactions.
     */
    private final BigDecimal balanceAfter;

    // ── Liens & références ───────────────────────────────────────────────────

    /**
     * Référence à la PaymentRequest externe, si applicable.
     * Null pour les crédits internes (loyalty, bonus).
     */
    private final UUID paymentRequestId;

    /**
     * Pour une transaction de type REVERSAL : identifiant de la transaction
     * originale annulée.
     */
    private final UUID originalTransactionId;

    // ── Métadonnées ───────────────────────────────────────────────────────────

    /**
     * Description lisible de la transaction (générée par le système ou l'IA).
     * Ex. : "Cashback 5% sur votre achat du 20/05/2026".
     */
    private final String description;

    /**
     * Métadonnées libres en JSON (paramètres de campagne, règle IA appliquée…).
     * Stocké comme TEXT en base pour flexibilité maximale.
     */
    private final String metadata;

    // ── Horodatages ──────────────────────────────────────────────────────────

    /** Date/heure de création de la transaction (UTC, nanoseconde). */
    private final Instant createdAt;

    /**
     * Date/heure à laquelle la transaction a atteint un état terminal
     * (COMPLETED, FAILED, REVERSED).
     */
    private final Instant completedAt;

    // ── Méthodes de domaine ───────────────────────────────────────────────────

    /**
     * Indique si la transaction a été finalisée avec succès.
     *
     * @return true si status == COMPLETED.
     */
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(status);
    }

    /**
     * Indique si la transaction peut faire l'objet d'un reversal.
     * Seules les transactions COMPLETED et non déjà annulées (REVERSED) peuvent être annulées.
     *
     * @return true si la transaction est annulable.
     */
    public boolean isReversible() {
        return TransactionStatus.COMPLETED.equals(status)
                && originalTransactionId == null; // pas déjà un reversal
    }
}
