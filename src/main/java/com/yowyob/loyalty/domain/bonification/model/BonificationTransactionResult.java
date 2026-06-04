package com.yowyob.loyalty.domain.bonification.model;

public record BonificationTransactionResult(
        String transactionId,
        double amount,
        String clientLogin,
        boolean debit,
        String status,
        String message
) {}
