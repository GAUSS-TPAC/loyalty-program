package com.yowyob.loyaulty.program.domain.referral.port.out;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralEventRepository {
    Mono<ReferralEvent> save(ReferralEvent event);
    Mono<ReferralEvent> findById(UUID id, TenantId tenantId);
    Mono<ReferralEvent> findPendingByReferee(TenantId tenantId, String refereeId);
    Flux<ReferralEvent> findByReferrer(TenantId tenantId, String referrerId);
    Flux<ReferralEvent> findAllByTenant(TenantId tenantId);
}
