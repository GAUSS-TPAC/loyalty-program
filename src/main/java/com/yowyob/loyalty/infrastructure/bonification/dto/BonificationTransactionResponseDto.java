package com.yowyob.loyalty.infrastructure.bonification.dto;

public record BonificationTransactionResponseDto(
        String id,
        double amount,
        String statuts,
        String clientLogin,
        Boolean isDebit,
        String message
) {}
