package com.yowyob.loyalty.domain.campaign.port.out;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface CampaignRepository {
    Mono<Campaign> save(Campaign campaign);
    Mono<Campaign> findById(TenantId tenantId, UUID id);
    Flux<Campaign> findAll(TenantId tenantId);
    Flux<Campaign> findByStatus(TenantId tenantId, CampaignStatus status);
    Flux<Campaign> findDueForActivation(Instant now);
    Flux<Campaign> findDueForCompletion(Instant now);
}
