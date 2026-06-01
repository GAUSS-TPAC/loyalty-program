package com.yowyob.loyaulty.program.infrastructure.persistence.promo.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.promo.entity.PromoCampaignEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromoCampaignR2dbcRepository extends R2dbcRepository<PromoCampaignEntity, UUID> {

    Mono<PromoCampaignEntity> findByCodeAndTenantId(String code, UUID tenantId);

    Mono<PromoCampaignEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<PromoCampaignEntity> findAllByTenantId(UUID tenantId);
}
