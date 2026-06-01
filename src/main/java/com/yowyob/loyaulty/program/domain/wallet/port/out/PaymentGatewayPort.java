package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentInitiationResult;
import com.yowyob.loyaulty.program.domain.wallet.model.PaymentStatus;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port de sortie vers le Payment API partagé du Kernel Core.
 *
 * <p>Les paiements (MTN, Orange, Stripe…) sont délégués au Kernel Core.
 * Notre domaine ne connaît plus les détails de chaque provider — il passe
 * par ce port unique qui sera implémenté par le Kernel Core Payment Adapter.</p>
 *
 * <p>Zéro annotation Spring — interface du domaine pur.</p>
 */
public interface PaymentGatewayPort {

    /**
     * Initie une recharge (crédit entrant) via le Kernel Core Payment API.
     *
     * @param tenantId       identifiant du tenant.
     * @param memberId       identifiant du membre effectuant la recharge.
     * @param amount         montant de la recharge, toujours positif.
     * @param currency       code devise ISO 4217 (ex. "XAF").
     * @param provider       nom du provider cible ("MTN", "ORANGE", "STRIPE"…).
     * @param idempotencyKey clé d'idempotence pour éviter les doubles débits.
     * @return résultat d'initiation contenant la référence externe et les instructions.
     */
    Mono<PaymentInitiationResult> initiateTopUp(
            UUID tenantId,
            UUID memberId,
            BigDecimal amount,
            String currency,
            String provider,
            String idempotencyKey
    );

    /**
     * Récupère le statut courant d'un paiement auprès du Kernel Core.
     * Utilisé en cas de timeout ou de perte de webhook.
     *
     * @param tenantId    identifiant du tenant.
     * @param externalRef référence externe retournée lors de l'initiation.
     * @return statut courant du paiement.
     */
    Mono<PaymentStatus> getPaymentStatus(UUID tenantId, String externalRef);

    /**
     * Initie un retrait (débit sortant) vers le compte du membre via le Kernel Core.
     *
     * @param tenantId       identifiant du tenant.
     * @param memberId       identifiant du membre effectuant le retrait.
     * @param amount         montant du retrait, toujours positif.
     * @param currency       code devise ISO 4217.
     * @param targetAccount  compte cible (numéro Mobile Money ou IBAN).
     * @param provider       nom du provider cible.
     * @param idempotencyKey clé d'idempotence.
     * @return résultat d'initiation.
     */
    Mono<PaymentInitiationResult> initiateWithdrawal(
            UUID tenantId,
            UUID memberId,
            BigDecimal amount,
            String currency,
            String targetAccount,
            String provider,
            String idempotencyKey
    );

    /**
     * Annule un paiement en cours auprès du Kernel Core.
     *
     * @param tenantId    identifiant du tenant.
     * @param externalRef référence externe du paiement à annuler.
     * @return {@code Mono<Void>} complété quand l'annulation est enregistrée.
     */
    Mono<Void> cancelPayment(UUID tenantId, String externalRef);
}
