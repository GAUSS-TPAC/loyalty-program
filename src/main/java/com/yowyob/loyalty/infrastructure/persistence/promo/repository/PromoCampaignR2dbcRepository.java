package com.yowyob.loyalty.infrastructure.persistence.promo.repository;

import com.yowyob.loyalty.infrastructure.persistence.promo.entity.PromoCampaignEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoCampaignR2dbcRepository extends R2dbcRepository<PromoCampaignEntity, UUID> {
    Mono<PromoCampaignEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Mono<PromoCampaignEntity> findByTenantIdAndCode(UUID tenantId, String code);
    Flux<PromoCampaignEntity> findAllByTenantId(UUID tenantId);
    Flux<PromoCampaignEntity> findAllByTenantIdAndActive(UUID tenantId, boolean active);
    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
