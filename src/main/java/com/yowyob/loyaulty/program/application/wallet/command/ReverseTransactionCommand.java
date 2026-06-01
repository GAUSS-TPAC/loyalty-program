package com.yowyob.loyaulty.program.application.wallet.command;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

import java.util.UUID;

public record ReverseTransactionCommand(
        TenantId tenantId,
        String memberId,
        UUID transactionId,
        String idempotencyKey
) {}
