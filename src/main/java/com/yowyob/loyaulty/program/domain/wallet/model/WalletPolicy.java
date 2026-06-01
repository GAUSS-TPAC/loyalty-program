package com.yowyob.loyaulty.program.domain.wallet.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Value Object représentant la politique du wallet d'un tenant.
 *
 * <p>La WalletPolicy définit l'ensemble des règles et limites applicables
 * à tous les wallets du tenant. Elle peut être surchargée au niveau d'un
 * membre individuel (ex. membres VIP).</p>
 *
 * <p>Immuable par construction (Lombok @Builder + @With pour les mises à jour).</p>
 */
@Getter
@Builder(toBuilder = true)
@With
public class WalletPolicy {

    // ── Identité ─────────────────────────────────────────────────────────────

    /** Identifiant unique de cette politique. */
    private final UUID id;

    /** Tenant auquel cette politique s'applique. */
    private final UUID tenantId;

    /** Nom lisible de la politique (ex. "Standard", "VIP", "Beta"). */
    private final String name;

    // ── Limites de recharge ───────────────────────────────────────────────────

    /**
     * Montant maximum autorisé par opération de recharge.
     * Null = pas de limite par opération.
     */
    private final BigDecimal rechargeMaxParOperation;

    /**
     * Solde maximum autorisé sur le wallet à tout moment.
     * Null = pas de limite de solde.
     */
    private final BigDecimal soldeMaximum;

    // ── Limites de débit ──────────────────────────────────────────────────────

    /**
     * Montant maximum de dépenses autorisé sur une période de 24h glissantes.
     * Null = pas de limite journalière.
     */
    private final BigDecimal depenseMaxJournaliere;

    /**
     * Montant à partir duquel un challenge OTP est requis avant débit.
     * Null = jamais de challenge OTP.
     */
    private final BigDecimal seuilChallengeotp;

    // ── Paramètres de retrait ─────────────────────────────────────────────────

    /**
     * Durée minimale à attendre entre un crédit et un retrait.
     * Prévient les schémas de dépôt-retrait immédiat (fraude).
     * Ex. : Duration.ofHours(24).
     */
    private final Duration delaiMinimumAvantRetrait;

    /**
     * Montant maximum par opération de retrait vers Mobile Money.
     * Null = pas de limite par retrait.
     */
    private final BigDecimal retraitMaxParOperation;

    // ── Configuration du statut initial ──────────────────────────────────────

    /**
     * Si true, les wallets créés commencent en PENDING_KYC.
     * Si false, ils passent directement en ACTIVE.
     */
    private final boolean kycRequiredAtEnrollment;

    // ── Fidélité & expiration ─────────────────────────────────────────────────

    /**
     * Code devise ISO 4217 utilisé pour ce tenant (ex. "XAF", "EUR").
     */
    private final String currency;

    /**
     * Durée de validité des points / crédits loyalty attribués.
     * Null = les points n'expirent pas.
     */
    private final Duration loyaltyPointsExpirationDelay;

    // ── Horodatages ──────────────────────────────────────────────────────────

    /** Date/heure de création de la politique (UTC). */
    private final Instant createdAt;

    /** Date/heure de la dernière mise à jour (UTC). */
    private final Instant updatedAt;

    // ── Méthodes de domaine ───────────────────────────────────────────────────

    /**
     * Vérifie si un montant de recharge respecte le plafond par opération.
     *
     * @param amount montant de la recharge.
     * @return true si le montant est dans les limites.
     */
    public boolean isRechargeAmountAllowed(BigDecimal amount) {
        if (rechargeMaxParOperation == null) return true;
        return amount.compareTo(rechargeMaxParOperation) <= 0;
    }

    /**
     * Vérifie si un nouveau solde ne dépasse pas le plafond maximum.
     *
     * @param newBalance nouveau solde après crédit.
     * @return true si le solde respecte le plafond.
     */
    public boolean isBalanceWithinLimit(BigDecimal newBalance) {
        if (soldeMaximum == null) return true;
        return newBalance.compareTo(soldeMaximum) <= 0;
    }

    /**
     * Indique si un challenge OTP est requis pour un montant de débit donné.
     *
     * @param amount montant du débit.
     * @return true si un OTP doit être vérifié avant de procéder.
     */
    public boolean requiresOtpChallenge(BigDecimal amount) {
        if (seuilChallengeotp == null) return false;
        return amount.compareTo(seuilChallengeotp) >= 0;
    }

    /**
     * Vérifie qu'un crédit est survenu suffisamment tôt pour permettre un retrait.
     *
     * @param lastCreditAt horodatage du dernier crédit.
     * @return true si le délai minimum est écoulé.
     */
    public boolean isWithdrawalDelayRespected(Instant lastCreditAt) {
        if (delaiMinimumAvantRetrait == null) return true;
        return Instant.now().isAfter(lastCreditAt.plus(delaiMinimumAvantRetrait));
    }
}
