package com.yowyob.loyaulty.program.api.wallet.dto.response;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        String source,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Instant createdAt
) {
    public static TransactionResponse from(WalletTransaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getWalletId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus().name(),
                tx.getSource().name(),
                tx.getBalanceBefore(),
                tx.getBalanceAfter(),
                tx.getCreatedAt()
        );
    }
}
