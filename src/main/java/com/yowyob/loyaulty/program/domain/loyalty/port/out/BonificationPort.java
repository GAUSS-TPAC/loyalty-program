package com.yowyob.loyaulty.program.domain.loyalty.port.out;

import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationReward;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface BonificationPort {

    Mono<PointsResult> recordTransaction(
            TenantId tenantId,
            String externalUserId,
            BigDecimal amount,
            String description
    );

    Flux<BonificationTransaction> getTransactionHistory(
            TenantId tenantId,
            String externalUserId
    );

    Mono<Void> createBeneficiary(
            TenantId tenantId,
            String externalUserId,
            String email,
            String name
    );

    Flux<BonificationReward> getAvailableRewards(
            TenantId tenantId,
            String externalUserId
    );
}
