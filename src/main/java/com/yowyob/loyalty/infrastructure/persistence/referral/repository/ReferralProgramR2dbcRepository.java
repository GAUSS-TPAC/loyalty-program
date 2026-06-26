package com.yowyob.loyalty.infrastructure.persistence.referral.repository;

import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralProgramEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralProgramR2dbcRepository extends ReactiveCrudRepository<ReferralProgramEntity, UUID> {
    Mono<ReferralProgramEntity> findByTenantId(UUID tenantId);
    Mono<ReferralProgramEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Mono<ReferralProgramEntity> findByTenantIdAndActive(UUID tenantId, boolean active);
}
