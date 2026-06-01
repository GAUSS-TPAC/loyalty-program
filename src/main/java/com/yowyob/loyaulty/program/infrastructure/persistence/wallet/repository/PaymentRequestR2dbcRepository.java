package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.PaymentRequestEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository Spring Data R2DBC pour la table {@code payment_requests}.
 */
@Repository
public interface PaymentRequestR2dbcRepository
        extends ReactiveCrudRepository<PaymentRequestEntity, UUID> {

    /**
     * Cherche par id et tenant (sécurité multi-tenant).
     */
    Mono<PaymentRequestEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Lookup par référence externe provider pour rapprocher les webhooks entrants.
     * Exploite l'index partiel {@code idx_pr_external_reference}.
     */
    @Query("""
            SELECT *
            FROM   payment_requests
            WHERE  external_reference = :externalReference
              AND  provider           = :provider
            LIMIT 1
            """)
    Mono<PaymentRequestEntity> findByExternalReferenceAndProvider(
            String externalReference, String provider);

    /**
     * Cherche par la WalletTransaction liée (utilisé pour l'idempotence de création).
     */
    Mono<PaymentRequestEntity> findByWalletTransactionIdAndTenantId(
            UUID walletTransactionId, UUID tenantId);

    /**
     * Retourne les PaymentRequests actives (INITIATED ou PROCESSING) d'un wallet.
     * Utilisé avant clôture pour vérifier qu'aucune opération n'est en cours.
     * Exploite l'index partiel {@code idx_pr_wallet_active}.
     */
    @Query("""
            SELECT *
            FROM   payment_requests
            WHERE  wallet_id = :walletId
              AND  tenant_id = :tenantId
              AND  status IN ('INITIATED', 'PROCESSING')
            """)
    Flux<PaymentRequestEntity> findActiveByWalletId(UUID walletId, UUID tenantId);

    /**
     * Retourne les PaymentRequests en attente de retry (status=TIMEOUT, retry < max).
     * Exploite l'index {@code idx_pr_retry_pending}.
     */
    @Query("""
            SELECT *
            FROM   payment_requests
            WHERE  status      = 'TIMEOUT'
              AND  retry_count < max_retries
              AND  (next_retry_at IS NULL OR next_retry_at <= :now)
            ORDER BY next_retry_at ASC NULLS FIRST
            LIMIT 100
            """)
    Flux<PaymentRequestEntity> findPendingRetryRequests(Instant now);

    /**
     * Met à jour le statut et les informations de résolution d'une PaymentRequest.
     */
    @Modifying
    @Query("""
            UPDATE payment_requests
            SET    status                = :status,
                   external_reference   = COALESCE(:externalReference, external_reference),
                   provider_error_message = :providerErrorMessage,
                   resolved_at          = CASE
                                            WHEN :status IN ('CONFIRMED','FAILED','CANCELLED')
                                            THEN NOW()
                                            ELSE resolved_at
                                          END
            WHERE  id        = :id
              AND  tenant_id = :tenantId
            """)
    Mono<Integer> updateStatus(
            UUID id, UUID tenantId,
            String status, String externalReference,
            String providerErrorMessage
    );

    /**
     * Incrémente le compteur de retry et planifie la prochaine tentative.
     */
    @Modifying
    @Query("""
            UPDATE payment_requests
            SET    retry_count   = retry_count + 1,
                   status        = 'TIMEOUT',
                   next_retry_at = :nextRetryAt
            WHERE  id = :id
            """)
    Mono<Integer> incrementRetryCount(UUID id, Instant nextRetryAt);
}
