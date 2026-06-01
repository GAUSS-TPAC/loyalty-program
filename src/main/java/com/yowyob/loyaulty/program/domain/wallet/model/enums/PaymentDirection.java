package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Direction du flux d'argent d'une PaymentRequest.
 *
 * INBOUND  → L'argent entre dans le wallet (recharge, crédit).
 * OUTBOUND → L'argent sort du wallet (retrait vers Mobile Money).
 */
public enum PaymentDirection {

    /**
     * Flux entrant : le provider transfère des fonds vers le wallet.
     * Ex. : recharge MTN, paiement Stripe.
     */
    INBOUND,

    /**
     * Flux sortant : le wallet transfère des fonds vers le provider.
     * Ex. : retrait vers MTN Mobile Money / Orange Money.
     */
    OUTBOUND
}
