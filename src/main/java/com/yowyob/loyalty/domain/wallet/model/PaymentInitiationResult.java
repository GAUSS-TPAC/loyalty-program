package com.yowyob.loyalty.domain.wallet.model;

import java.time.Instant;

public record PaymentInitiationResult(
    String externalRef,
    String status,
    String ussdCode,
    String redirectUrl,
    Instant expiresAt,
    boolean requiresUserAction
) {}
