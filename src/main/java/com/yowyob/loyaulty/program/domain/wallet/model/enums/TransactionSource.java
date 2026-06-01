package com.yowyob.loyaulty.program.domain.wallet.model.enums;

/**
 * Origine / canal d'une WalletTransaction.
 * Permet de filtrer l'historique par source de fonds.
 */
public enum TransactionSource {

    // ── Crédits externes (recharges) ──────────────────────────────────────
    /** Recharge via MTN Mobile Money. */
    TOPUP_MTN,

    /** Recharge via Orange Money. */
    TOPUP_ORANGE,

    /** Recharge via Stripe (carte bancaire). */
    TOPUP_STRIPE,

    // ── Crédits internes (programme de fidélité) ──────────────────────────
    /** Récompense de fidélité attribuée par la plateforme (loyalty reward). */
    LOYALTY_REWARD,

    /** Cashback calculé sur un achat. */
    CASHBACK,

    /** Bonus accordé manuellement ou automatiquement par l'IA. */
    BONUS,

    // ── Débits ────────────────────────────────────────────────────────────
    /** Paiement d'un service ou d'un produit via le wallet. */
    PURCHASE,

    /** Retrait vers MTN Mobile Money. */
    WITHDRAWAL_MTN,

    /** Retrait vers Orange Money. */
    WITHDRAWAL_ORANGE,

    // ── Corrections ───────────────────────────────────────────────────────
    /** Ajustement manuel réalisé par un administrateur. */
    MANUAL_ADJUSTMENT,

    /** Remboursement suite à l'annulation d'un achat. */
    REFUND
}
