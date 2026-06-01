package com.yowyob.loyaulty.program.api.wallet.dto.response;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletBalanceResponse(
        UUID walletId,
        String memberId,
        BigDecimal balance,
        String currency,
        String status
) {
    public static WalletBalanceResponse from(Wallet wallet) {
        return new WalletBalanceResponse(
                wallet.getId(),
                wallet.getMemberId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus().name()
        );
    }
}
