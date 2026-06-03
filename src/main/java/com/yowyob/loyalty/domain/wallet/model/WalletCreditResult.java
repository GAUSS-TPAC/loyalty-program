package com.yowyob.loyalty.domain.wallet.model;

import java.math.BigDecimal;

public record WalletCreditResult(
    Wallet updatedWallet,
    BigDecimal amountCredited,
    BigDecimal newBalance
) {}
