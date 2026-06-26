package com.yowyob.loyalty.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_audit_logs")
public class WalletAuditLogEntity {

    @Id
    private UUID id;

    @Column("wallet_id")
    private UUID walletId;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("actor_id")
    private String actorId;

    @Column("actor_type")
    private String actorType;

    private String action;

    private String reason;

    @Column("previous_status")
    private String previousStatus;

    @Column("new_status")
    private String newStatus;

    @Column("related_transaction_id")
    private UUID relatedTransactionId;

    private String metadata;

    @Column("occurred_at")
    private Instant occurredAt;
}
