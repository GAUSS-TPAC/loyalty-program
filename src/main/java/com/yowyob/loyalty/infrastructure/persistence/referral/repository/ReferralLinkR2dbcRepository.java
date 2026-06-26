package com.yowyob.loyalty.infrastructure.persistence.referral.repository;

import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralLinkEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralLinkR2dbcRepository extends ReactiveCrudRepository<ReferralLinkEntity, UUID> {
    Mono<ReferralLinkEntity> findByCodeAndTenantId(String code, UUID tenantId);
    Mono<ReferralLinkEntity> findByReferrerIdAndTenantId(UUID referrerId, UUID tenantId);
}
