package com.yowyob.loyalty.infrastructure.persistence.campaign.repository;

import com.yowyob.loyalty.infrastructure.persistence.campaign.entity.CampaignEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface CampaignR2dbcRepository extends R2dbcRepository<CampaignEntity, UUID> {

    Mono<CampaignEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Flux<CampaignEntity> findAllByTenantId(UUID tenantId);
    Flux<CampaignEntity> findAllByTenantIdAndStatus(UUID tenantId, String status);

    @Query("SELECT * FROM campaigns WHERE status = 'DRAFT' AND start_date <= :now AND (end_date IS NULL OR end_date > :now)")
    Flux<CampaignEntity> findDueForActivation(Instant now);

    @Query("SELECT * FROM campaigns WHERE status = 'ACTIVE' AND end_date IS NOT NULL AND end_date <= :now")
    Flux<CampaignEntity> findDueForCompletion(Instant now);
}
