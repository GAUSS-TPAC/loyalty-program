package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de sortie (driven) : accès en lecture/écriture aux wallets.
 *
 * <p>Implémenté par l'adaptateur R2DBC de l'infrastructure.
 * Le domaine ne connaît pas la technologie de persistance.</p>
 */
public interface WalletRepository {

    /**
     * Cherche un wallet par son identifiant, en respectant le cloisonnement tenant.
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant (sécurité multi-tenant).
     * @return le wallet trouvé, ou {@code Mono.empty()} s'il n'existe pas.
     */
    Mono<Wallet> findById(UUID walletId, UUID tenantId);

    /**
     * Cherche le wallet d'un membre au sein d'un tenant.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @return le wallet du membre, ou {@code Mono.empty()}.
     */
    Mono<Wallet> findByMemberId(UUID memberId, UUID tenantId);

    /**
     * Persiste un wallet (création ou mise à jour complète).
     *
     * @param wallet le wallet à sauvegarder.
     * @return le wallet sauvegardé (avec éventuels champs générés).
     */
    Mono<Wallet> save(Wallet wallet);

    /**
     * Met à jour uniquement le statut et les métadonnées de gel d'un wallet.
     * Opération optimisée pour les changements d'état fréquents.
     *
     * @param walletId    identifiant du wallet.
     * @param tenantId    identifiant du tenant.
     * @param newStatus   nouveau statut à appliquer.
     * @param freezeReason motif du gel (null si pas de gel).
     * @return le wallet mis à jour.
     */
    Mono<Wallet> updateStatus(UUID walletId, UUID tenantId, WalletStatus newStatus, String freezeReason);

    /**
     * Vérifie qu'un wallet existe pour un membre et un tenant donnés.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @return {@code true} si un wallet existe, {@code false} sinon.
     */
    Mono<Boolean> existsByMemberId(UUID memberId, UUID tenantId);
}
