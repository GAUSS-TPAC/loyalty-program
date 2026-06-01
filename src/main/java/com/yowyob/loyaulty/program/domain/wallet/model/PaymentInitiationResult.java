package com.yowyob.loyaulty.program.domain.wallet.model;

import java.time.Instant;

/**
 * Résultat d'une initiation de paiement retourné par le Kernel Core Payment API.
 *
 * <p>Ce record encapsule toutes les informations nécessaires au client pour
 * finaliser l'opération (validation USSD, redirection Stripe, etc.).</p>
 *
 * <p>Zéro annotation Spring — fichier du domaine pur.</p>
 *
 * @param externalRef  référence de la transaction chez le provider (pour le suivi).
 * @param status       statut initial de la demande de paiement.
 * @param redirectUrl  URL de paiement à présenter à l'utilisateur (Stripe uniquement, null sinon).
 * @param ussdCode     code USSD à composer (MTN/Orange uniquement, null sinon).
 * @param expiresAt    date d'expiration de la demande de paiement.
 */
public record PaymentInitiationResult(
        String externalRef,
        PaymentStatus status,
        String redirectUrl,
        String ussdCode,
        Instant expiresAt
) {

    /**
     * Indique si l'utilisateur doit effectuer une action supplémentaire
     * pour valider le paiement (ex. composer un code USSD ou suivre un lien).
     *
     * @return true si une action utilisateur est requise.
     */
    public boolean requiresUserAction() {
        return ussdCode != null || redirectUrl != null;
    }
}
