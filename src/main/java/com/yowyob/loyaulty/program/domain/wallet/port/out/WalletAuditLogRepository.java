package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletAuditLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Port de sortie (driven) : persistance des entrées d'audit.
 *
 * <p>Les AuditLogs sont <strong>uniquement en écriture</strong> depuis le domaine ;
 * la lecture est réservée aux endpoints admin. Aucune méthode de mise à jour
 * ni de suppression n'est exposée.</p>
 */
public interface WalletAuditLogRepository {

    /**
     * Persiste une nouvelle entrée d'audit (création uniquement, immuable).
     *
     * @param auditLog l'entrée d'audit à créer.
     * @return l'entrée après persistance.
     */
    Mono<WalletAuditLog> save(WalletAuditLog auditLog);

    /**
     * Retourne toutes les entrées d'audit d'un wallet, triées par date décroissante.
     * Réservé aux administrateurs.
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @param dateFrom  borne inférieure (null = depuis le début).
     * @param dateTo    borne supérieure (null = maintenant).
     * @param offset    offset de pagination.
     * @param limit     taille de la page.
     * @return flux d'entrées d'audit.
     */
    Flux<WalletAuditLog> findByWalletId(
            UUID walletId,
            UUID tenantId,
            Instant dateFrom,
            Instant dateTo,
            long offset,
            int limit
    );

    /**
     * Compte les entrées d'audit d'un wallet (pour la pagination admin).
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return nombre total d'entrées.
     */
    Mono<Long> countByWalletId(UUID walletId, UUID tenantId);
}
