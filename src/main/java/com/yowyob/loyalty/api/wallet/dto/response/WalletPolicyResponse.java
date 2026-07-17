package com.yowyob.loyalty.api.wallet.dto.response;

import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import java.math.BigDecimal;

public record WalletPolicyResponse(
    String currencyName,
    String currencySymbol,
    BigDecimal exchangeRate,
    BigDecimal dailySpendCap,
    BigDecimal maxBalance,
    BigDecimal maxTopupPerTransaction,
    BigDecimal minWithdrawal,
    int withdrawalDelayHours,
    BigDecimal otpThreshold,
    boolean kycRequiredForWithdrawal,
    Integer expiryDays
) {
    public static WalletPolicyResponse from(WalletPolicy policy) {
        return new WalletPolicyResponse(
            policy.currencyName(),
            policy.currencySymbol(),
            policy.exchangeRate(),
            policy.dailySpendCap(),
            policy.maxBalance(),
            policy.maxTopupPerTransaction(),
            policy.minWithdrawal(),
            policy.withdrawalDelayHours(),
            policy.otpThreshold(),
            policy.kycRequiredForWithdrawal(),
            policy.expiryDays()
        );
    }
}
