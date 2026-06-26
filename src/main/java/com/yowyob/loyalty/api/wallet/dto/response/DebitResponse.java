package com.yowyob.loyalty.api.wallet.dto.response;

import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;

import java.math.BigDecimal;
import java.util.UUID;

public record DebitResponse(
    UUID walletId,
    BigDecimal newBalance,
    String currencyCode,
    String walletStatus,
    boolean otpRequired,
    String challengeId
) {
    public static DebitResponse from(WalletDebitResult result, WalletPolicy policy) {
        return new DebitResponse(
            result.updatedWallet().getId(),
            result.newBalance(),
            result.updatedWallet().getCurrencyCode(),
            result.updatedWallet().getStatus().name(),
            result.otpRequired(),
            result.challengeId()
        );
    }
}
