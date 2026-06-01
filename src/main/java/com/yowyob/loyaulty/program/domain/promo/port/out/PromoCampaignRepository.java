package com.yowyob.loyaulty.program.domain.promo.port.out;

import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoCampaignRepository {

    Mono<PromoCampaign> save(PromoCampaign campaign);

    Mono<PromoCampaign> findById(UUID id, TenantId tenantId);

    Mono<PromoCampaign> findByCode(String code, TenantId tenantId);

    Flux<PromoCampaign> findAllByTenant(TenantId tenantId);
}
