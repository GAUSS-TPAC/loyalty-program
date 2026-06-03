package com.yowyob.loyalty.domain.wallet.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRequest(
    UUID id,
    UUID walletTransactionId,
    String externalRef,
    String provider,
    PaymentDirection direction,
    BigDecimal realAmount,
    String realCurrency,
    BigDecimal virtualAmount,
    BigDecimal exchangeRate,
    PaymentStatus status,
    Instant initiatedAt,
    Instant confirmedAt,
    Instant expiresAt
) {}
