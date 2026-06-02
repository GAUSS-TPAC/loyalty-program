package com.yowyob.loyalty.domain.wallet.model;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Record Java 21 immuable représentant les limites configurées par le tenant pour les wallets de ses membres.
 */
public record WalletPolicy(
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
    public static WalletPolicy defaults() {
        return new WalletPolicy(
            "Credits",
            "CR",
            BigDecimal.valueOf(1.0),
            null,
            null,
            null,
            null,
            24,
            null,
            true,
            null
        );
    }

    public Optional<String> validateCredit(BigDecimal amount, BigDecimal currentBalance) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Le montant du crédit doit être supérieur à zéro");
        }
        
        if (maxBalance != null && currentBalance.add(amount).compareTo(maxBalance) > 0) {
            return Optional.of("Le solde maximum autorisé (" + maxBalance + ") serait dépassé");
        }
        
        if (maxTopupPerTransaction != null && amount.compareTo(maxTopupPerTransaction) > 0) {
            return Optional.of("Le montant dépasse la limite par recharge (" + maxTopupPerTransaction + ")");
        }
        
        return Optional.empty();
    }

    public Optional<String> validateDebit(BigDecimal amount, BigDecimal currentBalance, BigDecimal todaySpent) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of("Le montant du débit doit être supérieur à zéro");
        }
        
        if (currentBalance.compareTo(amount) < 0) {
            return Optional.of("Solde insuffisant");
        }
        
        if (dailySpendCap != null && todaySpent.add(amount).compareTo(dailySpendCap) > 0) {
            return Optional.of("La limite de dépense quotidienne (" + dailySpendCap + ") serait dépassée");
        }
        
        return Optional.empty();
    }

    public boolean requiresOtp(BigDecimal amount) {
        return otpThreshold != null && amount.compareTo(otpThreshold) >= 0;
    }
}
