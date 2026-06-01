package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité R2DBC représentant la table {@code wallet_audit_logs}.
 *
 * <p>Strictement append-only : aucune méthode de mise à jour exposée.
 * Pas d'{@code @Version} (immuable par design — même principe que
 * {@link WalletTransactionEntity}).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_audit_logs")
public class WalletAuditLogEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("wallet_id")
    private UUID walletId;

    @Column("tenant_id")
    private UUID tenantId;

    // ── Acteur ────────────────────────────────────────────────
    @Column("actor_id")
    private String actorId;

    @Column("actor_type")
    private String actorType;

    // ── Action ────────────────────────────────────────────────
    @Column("action")
    private String action;

    @Column("reason")
    private String reason;

    // ── Contexte de transition ────────────────────────────────
    @Column("previous_status")
    private String previousStatus;

    @Column("new_status")
    private String newStatus;

    @Column("related_transaction_id")
    private UUID relatedTransactionId;

    @Column("metadata")
    private String metadata;

    // ── Contexte réseau ───────────────────────────────────────
    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    // ── Horodatage ────────────────────────────────────────────
    @Column("occurred_at")
    private Instant occurredAt;
}
