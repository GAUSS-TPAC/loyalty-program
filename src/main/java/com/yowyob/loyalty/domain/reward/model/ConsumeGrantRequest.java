package com.yowyob.loyalty.domain.reward.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumeGrantRequest(
        TenantId tenantId,
        UserId memberId,
        UUID grantId,
        String orderReference,
        BigDecimal orderAmount,
        String idempotencyKey
) {}
