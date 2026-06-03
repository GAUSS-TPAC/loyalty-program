package com.yowyob.loyalty.api.wallet.dto.response;

import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
    UUID id, 
    String memberId, 
    BigDecimal balance, 
    String currencyCode, 
    String currencyName, 
    String status, 
    Instant createdAt
) {
    public static WalletResponse from(Wallet wallet, WalletPolicy policy) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getMemberId().value().toString(),
            wallet.getBalance(),
            wallet.getCurrencyCode(),
            policy.currencyName(),
            wallet.getStatus().name(),
            wallet.getCreatedAt()
        );
    }
}
