package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletOperation;

import java.math.BigDecimal;
import java.util.Set;

public record WalletPolicy(
        BigDecimal maxBalance,
        BigDecimal maxTopupPerTransaction,
        BigDecimal dailySpendCap,
        BigDecimal minBalance,
        int withdrawDelayHours,
        boolean kycRequired,
        Set<WalletOperation> allowedOperations
) {
    public static WalletPolicy defaults() {
        return new WalletPolicy(
                new BigDecimal("10000000"),
                new BigDecimal("500000"),
                new BigDecimal("1000000"),
                BigDecimal.ZERO,
                24,
                false,
                Set.of(WalletOperation.TOPUP, WalletOperation.PURCHASE)
        );
    }

    public ValidationResult validateCredit(BigDecimal amount, BigDecimal currentBalance) {
        if (amount.compareTo(maxTopupPerTransaction) > 0) {
            return ValidationResult.fail(
                "Amount " + amount + " exceeds max topup per transaction " + maxTopupPerTransaction
            );
        }
        if (currentBalance.add(amount).compareTo(maxBalance) > 0) {
            return ValidationResult.fail(
                "Balance would exceed maximum allowed: " + maxBalance
            );
        }
        return ValidationResult.ok();
    }

    public ValidationResult validateDebit(BigDecimal amount, BigDecimal currentBalance,
                                           BigDecimal dailyTotalDebited) {
        if (currentBalance.subtract(amount).compareTo(minBalance) < 0) {
            return ValidationResult.fail(
                "Insufficient balance: " + currentBalance + ", required: " + amount
            );
        }
        if (dailyTotalDebited.add(amount).compareTo(dailySpendCap) > 0) {
            return ValidationResult.fail(
                "Daily spend cap exceeded: " + dailySpendCap
            );
        }
        return ValidationResult.ok();
    }

    public boolean allows(WalletOperation operation) {
        return allowedOperations.contains(operation);
    }

    public record ValidationResult(boolean valid, String reason) {
        static ValidationResult ok() { return new ValidationResult(true, null); }
        static ValidationResult fail(String reason) { return new ValidationResult(false, reason); }
    }
}
