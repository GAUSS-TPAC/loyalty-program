package com.yowyob.loyaulty.program.domain.wallet.model;

/**
 * Statut d'un paiement géré par le Kernel Core Payment API.
 *
 * <p>Zéro annotation Spring — fichier du domaine pur.</p>
 */
public enum PaymentStatus {

    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED;

    /**
     * Indique si le statut est terminal (plus aucune évolution possible).
     *
     * @return true si le paiement est dans un état final.
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == EXPIRED;
    }

    /**
     * Indique si le paiement s'est terminé avec succès.
     *
     * @return true uniquement pour COMPLETED.
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
}
