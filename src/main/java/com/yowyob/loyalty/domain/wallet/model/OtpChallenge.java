package com.yowyob.loyalty.domain.wallet.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OtpChallenge(
    String challengeId,
    UUID walletId,
    TenantId tenantId,
    UserId memberId,
    BigDecimal amount,
    String description,
    String orderReference,
    String idempotencyKey,
    String otpCode,
    Instant expiresAt
) {}
