package com.yowyob.loyalty.domain.bonification.model;

public record BonificationTransactionRequest(
        double amount,
        String clientLogin,
        boolean debit,
        String status
) {
    public static BonificationTransactionRequest credit(double amount, String clientLogin) {
        return new BonificationTransactionRequest(amount, clientLogin, false, "COMPLETE");
    }

    public static BonificationTransactionRequest debit(double amount, String clientLogin) {
        return new BonificationTransactionRequest(amount, clientLogin, true, "COMPLETE");
    }
}
