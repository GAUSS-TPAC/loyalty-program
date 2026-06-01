package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Repository Spring Data R2DBC pour la table {@code wallet_transactions}.
 *
 * <p>Toutes les requêtes de lecture complexes (filtre + pagination) sont
 * en SQL natif pour tirer parti des index partiels PostgreSQL définis en V005.</p>
 */
@Repository
public interface WalletTransactionR2dbcRepository
        extends ReactiveCrudRepository<WalletTransactionEntity, UUID> {

    /**
     * Cherche une transaction par id et tenant (sécurité multi-tenant).
     */
    Mono<WalletTransactionEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Cherche une transaction par clé d'idempotence et tenant.
     * Utilisé pour le contrôle d'idempotence avant tout traitement.
     */
    Mono<WalletTransactionEntity> findByIdempotencyKeyAndTenantId(
            String idempotencyKey, UUID tenantId);

    /**
     * Met à jour le statut d'une transaction existante.
     * Seule mutation permise sur une transaction (PENDING → COMPLETED/FAILED/REVERSED).
     */
    @Modifying
    @Query("""
            UPDATE wallet_transactions
            SET    status       = :status,
                   completed_at = :completedAt
            WHERE  id        = :id
              AND  tenant_id = :tenantId
            """)
    Mono<Integer> updateStatus(UUID id, UUID tenantId, String status, Instant completedAt);

    /**
     * Liste paginée des transactions d'un wallet avec tous les filtres optionnels.
     * Exploite l'index {@code idx_wt_wallet_created_at}.
     *
     * <p>Les paramètres {@code type}, {@code source}, {@code status} sont filtrés
     * uniquement si non {@code NULL} grâce aux conditions {@code (... IS NULL OR ...)}.
     * Cela permet une seule requête pour tous les cas de filtre.</p>
     */
    @Query("""
            SELECT *
            FROM   wallet_transactions
            WHERE  wallet_id  = :walletId
              AND  tenant_id  = :tenantId
              AND  (:type   IS NULL OR type   = :type)
              AND  (:source IS NULL OR source = :source)
              AND  (:status IS NULL OR status = :status)
              AND  (:dateFrom IS NULL OR created_at >= :dateFrom)
              AND  (:dateTo   IS NULL OR created_at <= :dateTo)
            ORDER BY created_at DESC
            LIMIT  :limit
            OFFSET :offset
            """)
    Flux<WalletTransactionEntity> findByFilters(
            UUID walletId, UUID tenantId,
            String type, String source, String status,
            Instant dateFrom, Instant dateTo,
            int limit, long offset
    );

    /**
     * Compte les transactions correspondant aux filtres (pour la pagination).
     */
    @Query("""
            SELECT COUNT(*)
            FROM   wallet_transactions
            WHERE  wallet_id  = :walletId
              AND  tenant_id  = :tenantId
              AND  (:type   IS NULL OR type   = :type)
              AND  (:source IS NULL OR source = :source)
              AND  (:status IS NULL OR status = :status)
              AND  (:dateFrom IS NULL OR created_at >= :dateFrom)
              AND  (:dateTo   IS NULL OR created_at <= :dateTo)
            """)
    Mono<Long> countByFilters(
            UUID walletId, UUID tenantId,
            String type, String source, String status,
            Instant dateFrom, Instant dateTo
    );

    /**
     * Calcule la somme des débits COMPLETED d'un wallet depuis une date donnée.
     * Utilisé pour vérifier le plafond journalier ({@code depense_max_journaliere}).
     * Exploite l'index partiel {@code idx_wt_debit_since}.
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @param since    début de la fenêtre glissante (ex. NOW() - INTERVAL '24 hours').
     * @return somme des montants débités sur la période, ou 0 si aucun.
     */
    @Query("""
            SELECT COALESCE(SUM(amount), 0)
            FROM   wallet_transactions
            WHERE  wallet_id   = :walletId
              AND  tenant_id   = :tenantId
              AND  type        = 'DEBIT'
              AND  status      = 'COMPLETED'
              AND  created_at >= :since
            """)
    Mono<BigDecimal> sumCompletedDebitsSince(UUID walletId, UUID tenantId, Instant since);

    /**
     * Calcule la somme de toutes les transactions COMPLETED pour réconciliation.
     * Résultat attendu = solde actuel du wallet (crédit - débit).
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return solde calculé depuis les transactions.
     */
    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN type IN ('CREDIT') THEN  amount
                     WHEN type IN ('DEBIT')  THEN -amount
                     WHEN type  = 'REVERSAL' THEN  amount
                     ELSE 0
                END
            ), 0)
            FROM   wallet_transactions
            WHERE  wallet_id = :walletId
              AND  tenant_id = :tenantId
              AND  status    = 'COMPLETED'
            """)
    Mono<BigDecimal> computeReconciledBalance(UUID walletId, UUID tenantId);
}
