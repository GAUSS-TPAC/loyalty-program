package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Représente les états possibles d'un wallet dans le cycle de vie.
 *
 * PENDING_KYC  → Le wallet est créé mais en attente de validation KYC.
 * ACTIVE       → Le wallet est pleinement opérationnel.
 * FROZEN       → Le wallet est gelé (fraude, action admin). Aucune opération n'est permise.
 * CLOSED       → Le wallet est clôturé définitivement (irréversible).
 */
public enum WalletStatus {

    /**
     * Wallet créé, en attente de validation KYC (Know Your Customer).
     * Les crédits internes (loyalty) peuvent être acceptés selon la config tenant.
     */
    PENDING_KYC,

    /**
     * Wallet actif et pleinement opérationnel.
     * Toutes les opérations (crédit, débit, retrait) sont autorisées.
     */
    ACTIVE,

    /**
     * Wallet gelé. Déclenché manuellement par un admin ou automatiquement
     * par la détection de fraude. Aucune opération n'est permise.
     */
    FROZEN,

    /**
     * Wallet définitivement clôturé.
     * Transition irréversible. Le solde doit être nul avant clôture.
     */
    CLOSED
}
