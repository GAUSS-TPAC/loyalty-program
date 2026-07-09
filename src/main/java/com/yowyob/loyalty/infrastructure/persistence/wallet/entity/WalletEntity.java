package com.yowyob.loyalty.infrastructure.persistence.wallet.entity;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallets")
public class WalletEntity {
    @Id
    private UUID id;
    
    @Column("member_id")
    private UUID memberId;
    
    @Column("tenant_id")
    private UUID tenantId;

    @Column("available_balance")
    private BigDecimal balance;

    @Column("currency_code")
    private String currencyCode;

    private String status;

    @Version
    private Long version;

    @Column("frozen_at")
    private Instant frozenAt;

    @Column("freeze_reason")
    private String frozenReason;

    @Column("closed_at")
    private Instant closedAt;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
