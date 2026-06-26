package com.yowyob.loyalty.domain.promo.port.in;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPromoCampaignUseCase {
    Mono<PromoCampaign> getById(TenantId tenantId, UUID campaignId);
    Mono<PromoCampaign> getByCode(TenantId tenantId, String code);
    Flux<PromoCampaign> listAll(TenantId tenantId);
    Flux<PromoCampaign> listActive(TenantId tenantId);
}
