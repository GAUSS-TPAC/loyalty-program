package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentRequest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port d'entrée (use case) : Initier un retrait vers Mobile Money.
 *
 * <p>Pré-conditions vérifiées par le handler :</p>
 * <ol>
 *   <li>KYC du membre validé.</li>
 *   <li>Délai minimum post-crédit respecté (anti dépôt-retrait immédiat).</li>
 *   <li>Solde disponible suffisant.</li>
 *   <li>Plafond retrait par opération respecté.</li>
 *   <li>Wallet en statut {@code ACTIVE}.</li>
 * </ol>
 *
 * <p>Le montant est réservé (statut {@code RESERVED}) pendant le traitement
 * asynchrone. Il est libéré en cas d'échec ou définitivement débité à la
 * confirmation du provider.</p>
 */
public interface WithdrawWalletUseCase {

    /**
     * Initie un retrait vers un compte Mobile Money.
     *
     * @param walletId            identifiant du wallet source.
     * @param tenantId            identifiant du tenant.
     * @param amount              montant à retirer.
     * @param provider            provider cible ("MTN" ou "ORANGE").
     * @param mobileMoneyPhone    numéro de téléphone Mobile Money du bénéficiaire.
     * @param idempotencyKey      clé unique anti double-traitement.
     * @return la {@link PaymentRequest} en statut INITIATED.
     */
    Mono<PaymentRequest> initiateWithdrawal(
            UUID walletId,
            UUID tenantId,
            BigDecimal amount,
            String provider,
            String mobileMoneyPhone,
            String idempotencyKey
    );
}
