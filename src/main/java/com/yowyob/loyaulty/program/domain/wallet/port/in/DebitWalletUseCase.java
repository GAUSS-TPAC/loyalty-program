package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port d'entrée (use case) : Débiter un wallet.
 *
 * <p>Le handler s'assure que :</p>
 * <ol>
 *   <li>L'idempotence est respectée (pas de double-débit).</li>
 *   <li>Le wallet est en statut {@code ACTIVE}.</li>
 *   <li>Le solde disponible est suffisant.</li>
 *   <li>Le plafond journalier ({@code depense_max_journaliere}) n'est pas dépassé.</li>
 *   <li>Si le montant dépasse le seuil OTP, le challenge a été validé.</li>
 * </ol>
 * <p>L'event {@code wallet.debited} est émis en cas de succès.</p>
 */
public interface DebitWalletUseCase {

    /**
     * Effectue un débit sur le wallet d'un membre.
     *
     * @param walletId        identifiant du wallet à débiter.
     * @param tenantId        identifiant du tenant.
     * @param amount          montant à débiter (strictement positif).
     * @param source          origine du débit (ex. "PURCHASE").
     * @param idempotencyKey  clé unique anti double-débit.
     * @param otpToken        token OTP si applicable (peut être null si non requis).
     * @param description     description lisible de la transaction.
     * @return la {@link WalletTransaction} COMPLETED résultante.
     */
    Mono<WalletTransaction> debit(
            UUID walletId,
            UUID tenantId,
            BigDecimal amount,
            String source,
            String idempotencyKey,
            String otpToken,
            String description
    );
}
