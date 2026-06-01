package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port d'entrée (use case) : Consulter le solde d'un wallet.
 *
 * <p>Deux niveaux de détail sont proposés :</p>
 * <ul>
 *   <li>{@link #getBalance(UUID, UUID)} — solde disponible uniquement (lecture rapide via cache Redis).</li>
 *   <li>{@link #getDetailedBalance(UUID, UUID)} — solde complet avec détail disponible / réservé / expirant.</li>
 * </ul>
 */
public interface GetWalletBalanceUseCase {

    /**
     * Retourne le solde disponible d'un wallet (lecture optimisée, peut venir du cache).
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @return le {@link Wallet} avec au minimum {@code availableBalance} renseigné.
     */
    Mono<Wallet> getBalance(UUID walletId, UUID tenantId);

    /**
     * Retourne le wallet avec le détail complet des soldes :
     * disponible, réservé et expirant prochainement.
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @return le {@link Wallet} complet.
     */
    Mono<Wallet> getDetailedBalance(UUID walletId, UUID tenantId);
}
