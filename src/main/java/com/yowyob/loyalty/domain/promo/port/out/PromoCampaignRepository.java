package com.yowyob.loyalty.domain.promo.port.out;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoCampaignRepository {
    Mono<PromoCampaign> save(PromoCampaign campaign);
    Mono<PromoCampaign> findById(TenantId tenantId, UUID id);
    Mono<PromoCampaign> findByCode(TenantId tenantId, String code);
    Flux<PromoCampaign> findAll(TenantId tenantId);
    Flux<PromoCampaign> findActive(TenantId tenantId);
    Mono<Void> deleteById(TenantId tenantId, UUID id);
}
