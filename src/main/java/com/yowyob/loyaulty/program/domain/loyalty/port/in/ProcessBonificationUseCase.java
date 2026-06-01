package com.yowyob.loyaulty.program.domain.loyalty.port.in;

import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ProcessBonificationUseCase {

    Mono<PointsResult> processTransaction(
            TenantId tenantId,
            String memberId,
            BigDecimal amount,
            String description,
            String idempotencyKey
    );

    Flux<BonificationTransaction> getMemberTransactionHistory(
            TenantId tenantId,
            String memberId
    );
}
