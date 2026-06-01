package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port d'entrée (use case) : Dégeler un wallet.
 *
 * <p>Le dégel est une action <strong>exclusivement administrative</strong>.
 * Contrairement au gel, il ne peut pas être automatisé. L'action est
 * obligatoirement tracée dans {@code WalletAuditLog} avec l'identité
 * de l'administrateur et le motif.</p>
 *
 * <p>Pré-condition : le wallet doit être en statut {@code FROZEN}.</p>
 */
public interface UnfreezeWalletUseCase {

    /**
     * Dégèle un wallet gelé.
     *
     * @param walletId  identifiant du wallet à dégeler.
     * @param tenantId  identifiant du tenant.
     * @param adminId   identifiant de l'administrateur réalisant le dégel.
     * @param reason    motif du dégel (ex. "Enquête clôturée, fraude non confirmée").
     * @return le {@link Wallet} remis en statut ACTIVE.
     */
    Mono<Wallet> unfreeze(
            UUID walletId,
            UUID tenantId,
            String adminId,
            String reason
    );
}
