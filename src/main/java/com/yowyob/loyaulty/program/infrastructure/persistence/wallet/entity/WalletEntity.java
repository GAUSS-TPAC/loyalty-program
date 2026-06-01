package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité R2DBC représentant la table {@code wallets}.
 *
 * <p>Mapping strict colonne-à-colonne avec la table SQL.
 * La conversion vers/depuis le modèle domaine est assurée par {@code WalletMapper}.</p>
 *
 * <p>{@code @Version} active l'optimistic locking R2DBC pour prévenir
 * les mises à jour concurrentes du solde sans verrou pesant.</p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("wallets")
public class WalletEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("member_id")
    private UUID memberId;

    @Column("tenant_id")
    private UUID tenantId;

    // ── Soldes ───────────────────────────────────────────────
    @Column("available_balance")
    private BigDecimal availableBalance;

    @Column("reserved_balance")
    private BigDecimal reservedBalance;

    @Column("expiring_balance")
    private BigDecimal expiringBalance;

    // ── État ─────────────────────────────────────────────────
    @Column("status")
    private String status;                  // WalletStatus.name()

    @Column("wallet_policy_id")
    private UUID walletPolicyId;

    // ── KYC ──────────────────────────────────────────────────
    @Column("kyc_validated")
    private boolean kycValidated;

    // ── Gel ───────────────────────────────────────────────────
    @Column("freeze_reason")
    private String freezeReason;

    @Column("frozen_at")
    private Instant frozenAt;

    // ── Clôture ───────────────────────────────────────────────
    @Column("closed_at")
    private Instant closedAt;

    // ── Horodatages ───────────────────────────────────────────
    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    /**
     * Version pour l'optimistic locking.
     * R2DBC incrémente automatiquement ce champ à chaque UPDATE.
     * Une mise à jour concurrente sur la même version lève une
     * {@code OptimisticLockingFailureException}.
     */
    @Version
    @Column("version")
    private Long version;
}
