package com.yowyob.loyalty.api.wallet.dto.response;

import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        String type,
        String source,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String status,
        Instant createdAt
) {
    public static WalletTransactionResponse from(WalletTransaction tx) {
        return new WalletTransactionResponse(
                tx.id(), tx.type().name(), tx.source().name(),
                tx.amount(), tx.balanceBefore(), tx.balanceAfter(),
                tx.status().name(), tx.createdAt()
        );
    }
}
