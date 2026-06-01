package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port d'entrée (use case) : Geler un wallet.
 *
 * <p>Le gel peut être déclenché :</p>
 * <ul>
 *   <li>Manuellement par un administrateur du tenant.</li>
 *   <li>Automatiquement par le service de détection de fraude.</li>
 * </ul>
 *
 * <p>Un wallet gelé (statut {@code FROZEN}) ne peut plus ni être crédité,
 * ni être débité. Seul un administrateur peut le dégeler via
 * {@link UnfreezeWalletUseCase}. L'action est tracée dans {@code WalletAuditLog}.</p>
 */
public interface FreezeWalletUseCase {

    /**
     * Gèle un wallet.
     *
     * @param walletId  identifiant du wallet à geler.
     * @param tenantId  identifiant du tenant (cloisonnement multi-tenant).
     * @param actorId   identifiant de l'acteur déclenchant le gel (admin ou "SYSTEM").
     * @param actorType type d'acteur : "ADMIN", "SYSTEM" ou "AI_SERVICE".
     * @param reason    motif obligatoire du gel (ex. "Fraude suspectée : 5 débits/2min").
     * @return le {@link Wallet} mis à jour en statut FROZEN.
     */
    Mono<Wallet> freeze(
            UUID walletId,
            UUID tenantId,
            String actorId,
            String actorType,
            String reason
    );
}
