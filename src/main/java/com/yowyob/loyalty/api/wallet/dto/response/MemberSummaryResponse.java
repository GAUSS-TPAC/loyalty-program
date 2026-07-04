package com.yowyob.loyalty.api.wallet.dto.response;

import com.yowyob.loyalty.domain.wallet.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record MemberSummaryResponse(
        String memberId,
        BigDecimal balance,
        String currencyCode,
        String status,
        Instant createdAt
) {
    public static MemberSummaryResponse from(Wallet wallet) {
        return new MemberSummaryResponse(
                wallet.getMemberId().value().toString(),
                wallet.getBalance(),
                wallet.getCurrencyCode(),
                wallet.getStatus().name(),
                wallet.getCreatedAt()
        );
    }
}
