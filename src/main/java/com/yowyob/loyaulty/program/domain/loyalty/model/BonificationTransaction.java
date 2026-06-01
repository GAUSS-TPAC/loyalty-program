package com.yowyob.loyaulty.program.domain.loyalty.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BonificationTransaction(
        String transactionId,
        String externalUserId,
        BigDecimal amount,
        String description,
        Integer pointsEarned,
        String status,
        Instant occurredAt
) {}
