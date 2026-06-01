package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("wallet_transactions")
public class WalletTransactionEntity {
    @Id private UUID id;
    @Column("wallet_id") private UUID walletId;
    @Column("tenant_id") private UUID tenantId;
    @Column("type") private String type;
    @Column("amount") private BigDecimal amount;
    @Column("currency") private String currency;
    @Column("status") private String status;
    @Column("source") private String source;
    @Column("idempotency_key") private String idempotencyKey;
    @Column("balance_before") private BigDecimal balanceBefore;
    @Column("balance_after") private BigDecimal balanceAfter;
    @Column("related_transaction_id") private UUID relatedTransactionId;
    @Column("metadata") private String metadata;
    @Column("created_at") private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWalletId() { return walletId; }
    public void setWalletId(UUID walletId) { this.walletId = walletId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public UUID getRelatedTransactionId() { return relatedTransactionId; }
    public void setRelatedTransactionId(UUID relatedTransactionId) { this.relatedTransactionId = relatedTransactionId; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
