package com.yowyob.loyaulty.program.application.wallet.command;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;

import java.math.BigDecimal;

public record DebitWalletCommand(
        TenantId tenantId,
        String memberId,
        BigDecimal amount,
        TransactionSource source,
        String idempotencyKey
) {}
