package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Cycle de vie d'une WalletTransaction.
 *
 * PENDING    → Transaction initiée, en attente de confirmation externe.
 * COMPLETED  → Transaction finalisée avec succès.
 * FAILED     → Transaction échouée (rejet provider, solde insuffisant…).
 * REVERSED   → Transaction annulée via un REVERSAL.
 * RESERVED   → Montant réservé (en attente de confirmation de retrait).
 */
public enum TransactionStatus {

    /**
     * Transaction créée et en attente d'une confirmation (ex. webhook provider).
     */
    PENDING,

    /**
     * Transaction finalisée avec succès. Le solde a été ajusté.
     */
    COMPLETED,

    /**
     * Transaction échouée. Aucun impact sur le solde.
     */
    FAILED,

    /**
     * Transaction annulée par un REVERSAL lié.
     * Le solde a été restitué.
     */
    REVERSED,

    /**
     * Montant réservé pendant le traitement asynchrone d'un retrait.
     * Le montant est déduit du solde disponible mais pas encore crédité au provider.
     */
    RESERVED
}
