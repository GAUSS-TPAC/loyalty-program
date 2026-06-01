package com.yowyob.loyaulty.program.infrastructure.persistence.referral.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.referral.entity.ReferralLinkEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralLinkR2dbcRepository extends ReactiveCrudRepository<ReferralLinkEntity, UUID> {
    Mono<ReferralLinkEntity> findByTenantIdAndReferrerId(UUID tenantId, String referrerId);
    Mono<ReferralLinkEntity> findByTenantIdAndCode(UUID tenantId, String code);
}
