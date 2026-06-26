package com.yowyob.loyalty.domain.promo.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoUsageCounterPort {
    Mono<Long> increment(TenantId tenantId, UUID campaignId);
    Mono<Long> getCount(TenantId tenantId, UUID campaignId);
    Mono<Void> reset(TenantId tenantId, UUID campaignId);
}
