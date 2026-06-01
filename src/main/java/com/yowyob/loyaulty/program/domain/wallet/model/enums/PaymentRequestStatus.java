package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Cycle de vie d'une PaymentRequest (interaction avec un provider externe).
 *
 * INITIATED  → La requête a été créée et envoyée au provider.
 * PROCESSING → Le provider a accusé réception et traite la demande.
 * CONFIRMED  → Le provider a confirmé le succès de l'opération (webhook reçu).
 * FAILED     → Le provider a signalé un échec.
 * TIMEOUT    → Aucune réponse provider reçue dans le délai configuré.
 * CANCELLED  → La requête a été annulée avant confirmation.
 */
public enum PaymentRequestStatus {

    /**
     * Requête créée et envoyée au provider de paiement.
     * En attente de la prochaine étape (processing ou réponse directe).
     */
    INITIATED,

    /**
     * Le provider traite la demande de façon asynchrone.
     * Ex. : l'utilisateur doit valider sur son téléphone MTN/Orange.
     */
    PROCESSING,

    /**
     * Le provider a confirmé le succès via webhook.
     * La WalletTransaction liée passe en COMPLETED.
     */
    CONFIRMED,

    /**
     * Le provider a rejeté ou signalé un échec de l'opération.
     * La WalletTransaction liée passe en FAILED.
     */
    FAILED,

    /**
     * Délai d'attente dépassé sans réponse du provider.
     * Le système peut déclencher un retry ou annuler la transaction.
     */
    TIMEOUT,

    /**
     * La requête a été explicitement annulée (par l'utilisateur ou le système).
     */
    CANCELLED
}
