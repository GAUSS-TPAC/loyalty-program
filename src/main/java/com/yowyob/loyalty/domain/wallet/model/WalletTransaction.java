package com.yowyob.loyalty.domain.wallet.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WalletTransaction(
    UUID id,
    UUID walletId,
    TenantId tenantId,
    TransactionType type,
    BigDecimal amount,
    String currency,
    BigDecimal balanceBefore,
    BigDecimal balanceAfter,
    TransactionStatus status,
    TransactionSource source,
    String idempotencyKey,
    UUID referenceId,
    UUID reversalOf,
    Map<String, Object> metadata,
    Instant createdAt,
    Instant completedAt
) {
    public static WalletTransaction create(
        UUID walletId,
        TenantId tenantId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        TransactionSource source,
        String idempotencyKey
    ) {
        Instant now = Instant.now();
        return new WalletTransaction(
            UUID.randomUUID(),
            walletId,
            tenantId,
            type,
            amount,
            currency,
            balanceBefore,
            balanceAfter,
            TransactionStatus.COMPLETED,
            source,
            idempotencyKey,
            null,
            null,
            Map.of(),
            now,
            now
        );
    }

    public static WalletTransaction createPending(
        UUID walletId,
        TenantId tenantId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        TransactionSource source,
        String idempotencyKey
    ) {
        return new WalletTransaction(
            UUID.randomUUID(),
            walletId,
            tenantId,
            type,
            amount,
            currency,
            balanceBefore,
            balanceAfter,
            TransactionStatus.PENDING,
            source,
            idempotencyKey,
            null,
            null,
            Map.of(),
            Instant.now(),
            null
        );
    }
}
