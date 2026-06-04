package com.yowyob.loyalty.api.bonification.dto;

public record BonificationTransactionResponse(
        String transactionId,
        double amount,
        String clientLogin,
        boolean debit,
        String status,
        String message
) {}
