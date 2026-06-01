package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port d'entrée (use case) : Clore définitivement un wallet.
 *
 * <p>La clôture est une <strong>transition irréversible</strong> vers le statut
 * {@code CLOSED}. Elle ne peut intervenir que si :</p>
 * <ol>
 *   <li>Le solde total (disponible + réservé) est strictement nul.</li>
 *   <li>Il n'existe aucune {@code PaymentRequest} en cours (INITIATED ou PROCESSING).</li>
 * </ol>
 *
 * <p>L'action est tracée dans {@code WalletAuditLog}. L'event
 * {@code wallet.closed} est émis sur Kafka.</p>
 */
public interface CloseWalletUseCase {

    /**
     * Clôture un wallet de façon permanente.
     *
     * @param walletId  identifiant du wallet à clore.
     * @param tenantId  identifiant du tenant.
     * @param adminId   identifiant de l'administrateur initiant la clôture.
     * @param reason    motif de la clôture.
     * @return le {@link Wallet} en statut CLOSED.
     */
    Mono<Wallet> close(
            UUID walletId,
            UUID tenantId,
            String adminId,
            String reason
    );
}
