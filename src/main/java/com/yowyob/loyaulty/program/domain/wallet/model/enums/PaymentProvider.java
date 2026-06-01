package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Prestataire de paiement externe utilisé par une PaymentRequest.
 */
public enum PaymentProvider {

    /** MTN Mobile Money (Afrique subsaharienne). */
    MTN,

    /** Orange Money. */
    ORANGE,

    /**
     * Stripe (paiement par carte bancaire internationale).
     * Utilisé principalement pour les recharges en devise forte.
     */
    STRIPE,

    /**
     * Crédit/débit interne piloté par la plateforme.
     * Aucun provider externe n'est impliqué (ex. attribution de points loyalty).
     */
    INTERNAL
}
