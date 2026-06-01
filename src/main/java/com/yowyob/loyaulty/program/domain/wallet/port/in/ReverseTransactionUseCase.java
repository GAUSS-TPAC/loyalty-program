package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port d'entrée (use case) : Annuler une transaction (Reversal).
 *
 * <p>Un reversal crée une nouvelle {@link WalletTransaction} de type
 * {@code REVERSAL} liée à la transaction originale. La transaction originale
 * passe en statut {@code REVERSED} et le solde est restitué.</p>
 *
 * <p>Conditions préalables :</p>
 * <ol>
 *   <li>La transaction originale existe et est en statut {@code COMPLETED}.</li>
 *   <li>Elle n'a pas déjà été annulée (pas de REVERSAL lié existant).</li>
 *   <li>Le wallet est en statut {@code ACTIVE} ou {@code FROZEN}
 *       (un reversal peut être appliqué sur un wallet gelé pour corriger).</li>
 * </ol>
 */
public interface ReverseTransactionUseCase {

    /**
     * Annule une transaction complétée et restitue le solde.
     *
     * @param originalTransactionId identifiant de la transaction à annuler.
     * @param tenantId              identifiant du tenant.
     * @param actorId               identifiant de l'acteur demandant l'annulation.
     * @param reason                motif de l'annulation (obligatoire).
     * @param idempotencyKey        clé unique pour éviter un double-reversal.
     * @return la nouvelle {@link WalletTransaction} de type REVERSAL en statut COMPLETED.
     */
    Mono<WalletTransaction> reverse(
            UUID originalTransactionId,
            UUID tenantId,
            String actorId,
            String reason,
            String idempotencyKey
    );
}
