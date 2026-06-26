package com.yowyob.loyalty.domain.promo.port.out;

import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoUsageRepository {
    Mono<PromoUsage> save(PromoUsage usage);
    Mono<Long> countByCampaignId(TenantId tenantId, UUID campaignId);
    Mono<Long> countByMemberAndCampaign(TenantId tenantId, UUID campaignId, UserId memberId);
    Mono<Boolean> existsByOrderId(TenantId tenantId, UUID campaignId, String orderId);
    Flux<PromoUsage> findByMember(TenantId tenantId, UserId memberId);
}
