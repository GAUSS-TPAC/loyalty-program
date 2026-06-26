package com.yowyob.loyalty.domain.campaign.port.in;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetCampaignUseCase {
    Mono<Campaign> getById(TenantId tenantId, UUID campaignId);
    Flux<Campaign> listAll(TenantId tenantId);
    Flux<Campaign> listActive(TenantId tenantId);
}
