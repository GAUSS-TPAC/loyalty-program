package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Catégorie principale d'une WalletTransaction.
 *
 * CREDIT   → Entrée d'argent / de points dans le wallet.
 * DEBIT    → Sortie d'argent / de points du wallet.
 * REVERSAL → Annulation d'une transaction précédente (remboursement).
 */
public enum TransactionType {

    /**
     * Crédit : le solde du wallet augmente.
     * Ex. : recharge Mobile Money, cashback, bonus loyalty.
     */
    CREDIT,

    /**
     * Débit : le solde du wallet diminue.
     * Ex. : paiement d'un service, retrait vers Mobile Money.
     */
    DEBIT,

    /**
     * Reversal : annulation d'une transaction COMPLETED.
     * Restaure le solde à son état antérieur et lie la transaction originale.
     */
    REVERSAL
}
