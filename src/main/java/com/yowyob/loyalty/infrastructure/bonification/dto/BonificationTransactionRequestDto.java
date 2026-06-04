package com.yowyob.loyalty.infrastructure.bonification.dto;

public record BonificationTransactionRequestDto(
        Double amount,
        String status,
        String clientLogin,
        Boolean isDebit
) {}
