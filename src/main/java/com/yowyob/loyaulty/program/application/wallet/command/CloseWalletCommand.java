package com.yowyob.loyaulty.program.application.wallet.command;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;

public record CloseWalletCommand(
        TenantId tenantId,
        String memberId
) {}
