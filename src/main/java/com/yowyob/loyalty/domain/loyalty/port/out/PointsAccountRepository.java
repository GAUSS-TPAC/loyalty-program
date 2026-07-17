package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface PointsAccountRepository {
    PointsAccount save(PointsAccount account);
    Optional<PointsAccount> findById(UUID id);
    Optional<PointsAccount> findByMemberId(TenantId tenantId, UserId memberId);

    /** Somme des points générés à vie (lifetimeEarned) tous membres confondus -- usage console plateforme. */
    Mono<Long> sumLifetimeEarnedByTenant(TenantId tenantId);
}
