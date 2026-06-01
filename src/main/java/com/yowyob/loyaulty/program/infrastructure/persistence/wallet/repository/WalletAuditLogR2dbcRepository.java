package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletAuditLogEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository Spring Data R2DBC pour la table {@code wallet_audit_logs}.
 *
 * <p>Strictement en lecture après création : aucune méthode {@code update}
 * ni {@code delete} n'est exposée. L'idempotence est naturelle
 * (chaque entrée est unique par {@code id}).</p>
 */
@Repository
public interface WalletAuditLogR2dbcRepository
        extends ReactiveCrudRepository<WalletAuditLogEntity, UUID> {

    /**
     * Historique d'audit paginé d'un wallet, trié par date décroissante.
     * Exploite l'index {@code idx_wal_wallet_occurred_at}.
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @param dateFrom  borne inférieure (null = depuis le début).
     * @param dateTo    borne supérieure (null = maintenant).
     * @param limit     taille de la page.
     * @param offset    offset de pagination.
     * @return flux d'entrées d'audit.
     */
    @Query("""
            SELECT *
            FROM   wallet_audit_logs
            WHERE  wallet_id  = :walletId
              AND  tenant_id  = :tenantId
              AND  (:dateFrom IS NULL OR occurred_at >= :dateFrom)
              AND  (:dateTo   IS NULL OR occurred_at <= :dateTo)
            ORDER BY occurred_at DESC
            LIMIT  :limit
            OFFSET :offset
            """)
    Flux<WalletAuditLogEntity> findByWalletId(
            UUID walletId, UUID tenantId,
            Instant dateFrom, Instant dateTo,
            int limit, long offset
    );

    /**
     * Compte les entrées d'audit d'un wallet (pour la pagination admin).
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return nombre total d'entrées.
     */
    @Query("""
            SELECT COUNT(*)
            FROM   wallet_audit_logs
            WHERE  wallet_id = :walletId
              AND  tenant_id = :tenantId
            """)
    Mono<Long> countByWalletId(UUID walletId, UUID tenantId);
}
