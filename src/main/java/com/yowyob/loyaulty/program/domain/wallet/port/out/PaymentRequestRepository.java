package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentRequest;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de sortie (driven) : accès aux PaymentRequests.
 *
 * <p>Permet de créer, suivre et mettre à jour les demandes de paiement
 * envoyées aux providers externes (MTN, Orange, Stripe).</p>
 */
public interface PaymentRequestRepository {

    /**
     * Cherche une PaymentRequest par son identifiant.
     *
     * @param paymentRequestId identifiant de la PaymentRequest.
     * @param tenantId         identifiant du tenant.
     * @return la PaymentRequest, ou {@code Mono.empty()}.
     */
    Mono<PaymentRequest> findById(UUID paymentRequestId, UUID tenantId);

    /**
     * Cherche une PaymentRequest par la référence externe retournée par le provider.
     * Utilisé pour rapprocher un webhook à la PaymentRequest correspondante.
     *
     * @param externalReference référence externe (id provider).
     * @param provider          provider concerné.
     * @return la PaymentRequest correspondante, ou {@code Mono.empty()}.
     */
    Mono<PaymentRequest> findByExternalReference(String externalReference, PaymentProvider provider);

    /**
     * Cherche une PaymentRequest par clé d'idempotence.
     *
     * @param walletTransactionId identifiant de la WalletTransaction liée.
     * @param tenantId            identifiant du tenant.
     * @return la PaymentRequest existante, ou {@code Mono.empty()}.
     */
    Mono<PaymentRequest> findByWalletTransactionId(UUID walletTransactionId, UUID tenantId);

    /**
     * Persiste une PaymentRequest (création ou mise à jour).
     *
     * @param paymentRequest la PaymentRequest à sauvegarder.
     * @return la PaymentRequest après sauvegarde.
     */
    Mono<PaymentRequest> save(PaymentRequest paymentRequest);

    /**
     * Retourne toutes les PaymentRequests en attente de retry
     * (status TIMEOUT et retryCount < maxRetries).
     * Utilisé par le job de retry planifié.
     *
     * @return flux des PaymentRequests retryables.
     */
    Flux<PaymentRequest> findPendingRetryRequests();

    /**
     * Retourne les PaymentRequests en statut INITIATED ou PROCESSING
     * pour un wallet donné (bloquant un retrait ou une clôture).
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return flux des PaymentRequests en cours.
     */
    Flux<PaymentRequest> findActiveByWalletId(UUID walletId, UUID tenantId);

    /**
     * Met à jour le statut d'une PaymentRequest.
     *
     * @param paymentRequestId  identifiant de la PaymentRequest.
     * @param tenantId          identifiant du tenant.
     * @param newStatus         nouveau statut.
     * @param externalReference référence externe à renseigner (peut être null).
     * @param providerError     message d'erreur provider (null si succès).
     * @return la PaymentRequest mise à jour.
     */
    Mono<PaymentRequest> updateStatus(
            UUID paymentRequestId,
            UUID tenantId,
            PaymentRequestStatus newStatus,
            String externalReference,
            String providerError
    );
}
