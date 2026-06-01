package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Port de sortie (driven) : accès en lecture/écriture aux transactions de wallet.
 *
 * <p>Les transactions sont <strong>append-only</strong> : pas de méthode de mise à jour
 * ni de suppression. Seule la mise à jour du statut est autorisée pour les transitions
 * PENDING → COMPLETED/FAILED.</p>
 */
public interface WalletTransactionRepository {

    /**
     * Cherche une transaction par son identifiant.
     *
     * @param transactionId identifiant de la transaction.
     * @param tenantId      identifiant du tenant.
     * @return la transaction, ou {@code Mono.empty()}.
     */
    Mono<WalletTransaction> findById(UUID transactionId, UUID tenantId);

    /**
     * Cherche une transaction par sa clé d'idempotence.
     * Utilisé pour détecter les doublons avant tout traitement.
     *
     * @param idempotencyKey clé d'idempotence.
     * @param tenantId       identifiant du tenant.
     * @return la transaction existante, ou {@code Mono.empty()} si nouvelle.
     */
    Mono<WalletTransaction> findByIdempotencyKey(String idempotencyKey, UUID tenantId);

    /**
     * Persiste une nouvelle transaction (création uniquement).
     *
     * @param transaction la transaction à créer.
     * @return la transaction créée.
     */
    Mono<WalletTransaction> save(WalletTransaction transaction);

    /**
     * Met à jour le statut d'une transaction existante.
     * Seule opération de mutation autorisée sur une transaction.
     *
     * @param transactionId identifiant de la transaction.
     * @param tenantId      identifiant du tenant.
     * @param newStatus     nouveau statut.
     * @param completedAt   horodatage de finalisation (null si pas encore terminal).
     * @return la transaction mise à jour.
     */
    Mono<WalletTransaction> updateStatus(
            UUID transactionId,
            UUID tenantId,
            TransactionStatus newStatus,
            Instant completedAt
    );

    /**
     * Liste les transactions d'un wallet selon des critères de filtre, avec pagination.
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @param type      filtre par type (null = tous).
     * @param source    filtre par source (null = toutes).
     * @param status    filtre par statut (null = tous).
     * @param dateFrom  borne inférieure (null = depuis le début).
     * @param dateTo    borne supérieure (null = maintenant).
     * @param offset    offset de pagination.
     * @param limit     taille de la page.
     * @return flux de transactions triées par date décroissante.
     */
    Flux<WalletTransaction> findByWalletId(
            UUID walletId,
            UUID tenantId,
            TransactionType type,
            TransactionSource source,
            TransactionStatus status,
            Instant dateFrom,
            Instant dateTo,
            long offset,
            int limit
    );

    /**
     * Compte les transactions d'un wallet selon les mêmes critères (pour la pagination).
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @param type     filtre par type (null = tous).
     * @param source   filtre par source (null = toutes).
     * @param status   filtre par statut (null = tous).
     * @param dateFrom borne inférieure.
     * @param dateTo   borne supérieure.
     * @return nombre total de transactions correspondantes.
     */
    Mono<Long> countByWalletId(
            UUID walletId,
            UUID tenantId,
            TransactionType type,
            TransactionSource source,
            TransactionStatus status,
            Instant dateFrom,
            Instant dateTo
    );

    /**
     * Calcule la somme des débits d'un wallet depuis une date donnée.
     * Utilisé pour la vérification du plafond journalier.
     *
     * @param walletId  identifiant du wallet.
     * @param tenantId  identifiant du tenant.
     * @param since     date de début de la fenêtre glissante (ex. il y a 24h).
     * @return somme des montants débités sur la période.
     */
    Mono<BigDecimal> sumDebitsSince(UUID walletId, UUID tenantId, Instant since);
}
