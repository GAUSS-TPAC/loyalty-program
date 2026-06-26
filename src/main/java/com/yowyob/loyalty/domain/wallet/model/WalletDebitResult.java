package com.yowyob.loyalty.domain.wallet.model;

import java.math.BigDecimal;

public record WalletDebitResult(
    Wallet updatedWallet,
    BigDecimal amountDebited,
    BigDecimal newBalance,
    boolean otpRequired,
    String challengeId
) {}
