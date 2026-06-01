package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Port d'entrée (use case) : Consulter l'historique des transactions d'un wallet.
 *
 * <p>Supporte la pagination (page/size) et les filtres combinables :</p>
 * <ul>
 *   <li>Type ({@link TransactionType}) : CREDIT, DEBIT, REVERSAL</li>
 *   <li>Source ({@link TransactionSource}) : TOPUP_MTN, PURCHASE, LOYALTY_REWARD…</li>
 *   <li>Statut ({@link TransactionStatus}) : PENDING, COMPLETED, FAILED…</li>
 *   <li>Période : dateFrom / dateTo</li>
 * </ul>
 */
public interface GetTransactionHistoryUseCase {

    /**
     * Filtre de recherche pour l'historique des transactions.
     *
     * @param walletId  identifiant du wallet (obligatoire).
     * @param tenantId  identifiant du tenant (obligatoire).
     * @param type      filtre par type (null = tous les types).
     * @param source    filtre par source (null = toutes les sources).
     * @param status    filtre par statut (null = tous les statuts).
     * @param dateFrom  borne inférieure de la période (null = pas de limite).
     * @param dateTo    borne supérieure de la période (null = maintenant).
     * @param page      numéro de page (0-indexed).
     * @param size      taille de la page.
     */
    record TransactionFilter(
            UUID walletId,
            UUID tenantId,
            TransactionType type,
            TransactionSource source,
            TransactionStatus status,
            Instant dateFrom,
            Instant dateTo,
            int page,
            int size
    ) {}

    /**
     * Retourne les transactions correspondant aux critères de filtre.
     *
     * @param filter critères de recherche et pagination.
     * @return flux de {@link WalletTransaction} triées par date décroissante.
     */
    Flux<WalletTransaction> getHistory(TransactionFilter filter);

    /**
     * Compte le nombre total de transactions correspondant aux critères
     * (utile pour calculer le nombre de pages en front).
     *
     * @param filter critères de recherche (page et size ignorés).
     * @return nombre total de transactions correspondantes.
     */
    Mono<Long> countHistory(TransactionFilter filter);
}
