package com.yowyob.loyalty.infrastructure.persistence.promo.repository;

import com.yowyob.loyalty.infrastructure.persistence.promo.entity.PromoUsageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoUsageR2dbcRepository extends R2dbcRepository<PromoUsageEntity, UUID> {
    Mono<Long> countByTenantIdAndCampaignId(UUID tenantId, UUID campaignId);
    Mono<Long> countByTenantIdAndCampaignIdAndMemberId(UUID tenantId, UUID campaignId, UUID memberId);
    Mono<Boolean> existsByTenantIdAndCampaignIdAndOrderId(UUID tenantId, UUID campaignId, String orderId);
    Flux<PromoUsageEntity> findAllByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
