package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class WalletTransaction {

    private final UUID id;
    private final UUID walletId;
    private final TenantId tenantId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String currency;
    private TransactionStatus status;
    private final TransactionSource source;
    private final String idempotencyKey;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;
    private final UUID relatedTransactionId;
    private final String metadata;
    private final Instant createdAt;

    private WalletTransaction(UUID id, UUID walletId, TenantId tenantId,
                               TransactionType type, BigDecimal amount, String currency,
                               TransactionStatus status, TransactionSource source,
                               String idempotencyKey, BigDecimal balanceBefore,
                               BigDecimal balanceAfter, UUID relatedTransactionId,
                               String metadata, Instant createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.tenantId = tenantId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.source = source;
        this.idempotencyKey = idempotencyKey;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.relatedTransactionId = relatedTransactionId;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public static WalletTransaction createCredit(UUID walletId, TenantId tenantId,
                                                  BigDecimal amount, String currency,
                                                  TransactionSource source, String idempotencyKey,
                                                  BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new WalletTransaction(
                UUID.randomUUID(), walletId, tenantId,
                TransactionType.CREDIT, amount, currency,
                TransactionStatus.COMPLETED, source,
                idempotencyKey, balanceBefore, balanceAfter,
                null, null, Instant.now()
        );
    }

    public static WalletTransaction createPendingCredit(UUID walletId, TenantId tenantId,
                                                         BigDecimal amount, String currency,
                                                         TransactionSource source, String idempotencyKey,
                                                         BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new WalletTransaction(
                UUID.randomUUID(), walletId, tenantId,
                TransactionType.CREDIT, amount, currency,
                TransactionStatus.PENDING, source,
                idempotencyKey, balanceBefore, balanceAfter,
                null, null, Instant.now()
        );
    }

    public static WalletTransaction createDebit(UUID walletId, TenantId tenantId,
                                                 BigDecimal amount, String currency,
                                                 TransactionSource source, String idempotencyKey,
                                                 BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new WalletTransaction(
                UUID.randomUUID(), walletId, tenantId,
                TransactionType.DEBIT, amount, currency,
                TransactionStatus.COMPLETED, source,
                idempotencyKey, balanceBefore, balanceAfter,
                null, null, Instant.now()
        );
    }

    public static WalletTransaction reconstitute(UUID id, UUID walletId, TenantId tenantId,
                                                  TransactionType type, BigDecimal amount,
                                                  String currency, TransactionStatus status,
                                                  TransactionSource source, String idempotencyKey,
                                                  BigDecimal balanceBefore, BigDecimal balanceAfter,
                                                  UUID relatedTransactionId, String metadata,
                                                  Instant createdAt) {
        return new WalletTransaction(id, walletId, tenantId, type, amount, currency,
                status, source, idempotencyKey, balanceBefore, balanceAfter,
                relatedTransactionId, metadata, createdAt);
    }

    public static WalletTransaction createReversal(UUID walletId, TenantId tenantId,
                                                    BigDecimal amount, String currency,
                                                    UUID originalTransactionId,
                                                    BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return new WalletTransaction(
                UUID.randomUUID(), walletId, tenantId,
                TransactionType.REVERSAL, amount, currency,
                TransactionStatus.COMPLETED, TransactionSource.REVERSAL,
                null, balanceBefore, balanceAfter,
                originalTransactionId, null, Instant.now()
        );
    }

    public void complete() { this.status = TransactionStatus.COMPLETED; }
    public void fail() { this.status = TransactionStatus.FAILED; }
    public void reverse() { this.status = TransactionStatus.REVERSED; }

    public UUID getId() { return id; }
    public UUID getWalletId() { return walletId; }
    public TenantId getTenantId() { return tenantId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionStatus getStatus() { return status; }
    public TransactionSource getSource() { return source; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public UUID getRelatedTransactionId() { return relatedTransactionId; }
    public String getMetadata() { return metadata; }
    public Instant getCreatedAt() { return createdAt; }
}
