package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Agrégat racine du domain Wallet.
 *
 * <p>Un Wallet représente le porte-monnaie électronique d'un membre au sein
 * d'un tenant (entreprise cliente). Il est immuable après création (Lombok @With
 * pour les mutations fonctionnelles) afin de garantir la traçabilité.</p>
 *
 * <p>Invariants :</p>
 * <ul>
 *   <li>Le solde disponible ≥ 0 à tout moment.</li>
 *   <li>Seul un wallet ACTIVE peut être débité ou faire une demande de retrait.</li>
 *   <li>Un wallet FROZEN ne peut être opéré que par un administrateur (unfreeze).</li>
 *   <li>La transition vers CLOSED est irréversible et exige un solde à zéro.</li>
 * </ul>
 */
@Getter
@Builder(toBuilder = true)
@With
public class Wallet {

    // ── Identité ─────────────────────────────────────────────────────────────

    /** Identifiant unique du wallet (généré à la création). */
    private final UUID id;

    /** Identifiant du membre propriétaire du wallet. */
    private final UUID memberId;

    /**
     * Identifiant du tenant (entreprise cliente) auquel appartient ce wallet.
     * Clé de partitionnement principale pour le multi-tenancy.
     */
    private final UUID tenantId;

    // ── Soldes ───────────────────────────────────────────────────────────────

    /**
     * Solde disponible : montant immédiatement utilisable.
     * Calculé comme : total crédité - total débité - total réservé.
     */
    private final BigDecimal availableBalance;

    /**
     * Solde réservé : montant immobilisé pendant un traitement asynchrone
     * (ex. retrait en cours de confirmation provider).
     */
    private final BigDecimal reservedBalance;

    /**
     * Solde expirant prochainement (points loyalty) — indicatif,
     * calculé à la volée selon la WalletPolicy du tenant.
     */
    private final BigDecimal expiringBalance;

    // ── État & politique ─────────────────────────────────────────────────────

    /** État courant du wallet dans son cycle de vie. */
    private final WalletStatus status;

    /**
     * Identifiant de la WalletPolicy du tenant appliquée à ce wallet.
     * Peut être surchargée pour les membres VIP.
     */
    private final UUID walletPolicyId;

    // ── Contexte KYC ─────────────────────────────────────────────────────────

    /**
     * Indique si le KYC (Know Your Customer) a été validé.
     * Requis pour les retraits vers Mobile Money.
     */
    private final boolean kycValidated;

    // ── Horodatages ──────────────────────────────────────────────────────────

    /** Date/heure de création du wallet (UTC). */
    private final Instant createdAt;

    /** Date/heure de la dernière modification (UTC). */
    private final Instant updatedAt;

    /**
     * Date/heure à laquelle le wallet a été gelé, si applicable.
     * Null si le wallet n'est pas FROZEN.
     */
    private final Instant frozenAt;

    /**
     * Date/heure de clôture définitive.
     * Null tant que le wallet n'est pas CLOSED.
     */
    private final Instant closedAt;

    // ── Informations de gel ───────────────────────────────────────────────────

    /**
     * Motif du gel, renseigné lors du gel manuel ou automatique.
     * Null si le wallet n'est pas FROZEN.
     */
    private final String freezeReason;

    // ── Méthodes de domaine ───────────────────────────────────────────────────

    /**
     * Retourne le solde total (disponible + réservé).
     * Correspondance attendue avec la somme des WalletTransactions COMPLETED.
     *
     * @return solde total du wallet.
     */
    public BigDecimal getTotalBalance() {
        return availableBalance.add(reservedBalance);
    }

    /**
     * Vérifie si le wallet peut accepter des opérations de débit.
     *
     * @return true si le wallet est ACTIVE.
     */
    public boolean isOperational() {
        return WalletStatus.ACTIVE.equals(status);
    }

    /**
     * Vérifie si le solde disponible est suffisant pour un montant donné.
     *
     * @param amount montant à vérifier.
     * @return true si availableBalance ≥ amount.
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }
}
