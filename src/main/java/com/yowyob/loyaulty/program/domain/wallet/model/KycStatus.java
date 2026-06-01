package com.yowyob.loyaulty.program.domain.wallet.model;

/**
 * Statut KYC (Know Your Customer) d'un membre, fourni par Smart KYC via le Kernel Core.
 *
 * <p>Zéro annotation Spring — fichier du domaine pur.</p>
 */
public enum KycStatus {

    NOT_STARTED,
    PENDING_REVIEW,
    VERIFIED,
    REJECTED;

    /**
     * Indique si ce statut KYC autorise les retraits.
     *
     * @return true uniquement pour VERIFIED.
     */
    public boolean allowsWithdrawal() {
        return this == VERIFIED;
    }
}
