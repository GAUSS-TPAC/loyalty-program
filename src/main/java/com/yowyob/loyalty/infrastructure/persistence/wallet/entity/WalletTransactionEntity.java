package com.yowyob.loyalty.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_transactions")
public class WalletTransactionEntity {
    @Id
    private UUID id;
    
    @Column("wallet_id")
    private UUID walletId;
    
    @Column("tenant_id")
    private UUID tenantId;
    
    private String type;
    private BigDecimal amount;
    
    @Column("balance_before")
    private BigDecimal balanceBefore;
    
    @Column("balance_after")
    private BigDecimal balanceAfter;
    
    private String status;
    private String source;
    
    @Column("idempotency_key")
    private String idempotencyKey;
    
    @Column("reference_id")
    private UUID referenceId;
    
    @Column("reversal_of")
    private UUID reversalOf;
    
    private String metadata; // Stocké en JSON
    
    @Column("created_at")
    private Instant createdAt;
}
