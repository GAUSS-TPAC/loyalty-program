package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port d'entrée (use case) : Créditer un wallet.
 *
 * <p>Couvre les crédits externes (MTN, Orange, Stripe) et les crédits internes
 * (loyalty reward, cashback, bonus). Le handler vérifie l'idempotence,
 * applique la WalletPolicy du tenant, puis émet l'event {@code wallet.credited}.</p>
 */
public interface CreditWalletUseCase {

    /**
     * Commande de crédit d'un wallet.
     *
     * @param walletId       identifiant du wallet cible.
     * @param tenantId       identifiant du tenant (multi-tenancy).
     * @param amount         montant à créditer (strictement positif).
     * @param source         origine du crédit (ex. "TOPUP_MTN", "LOYALTY_REWARD").
     * @param idempotencyKey clé unique fournie par l'appelant (anti double-crédit).
     * @param description    description lisible (peut être null).
     * @return la {@link WalletTransaction} COMPLETED résultante.
     */
    Mono<WalletTransaction> credit(
            UUID walletId,
            UUID tenantId,
            BigDecimal amount,
            String source,
            String idempotencyKey,
            String description
    );
}
