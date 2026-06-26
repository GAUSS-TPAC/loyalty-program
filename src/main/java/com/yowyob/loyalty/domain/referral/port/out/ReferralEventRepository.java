package com.yowyob.loyalty.domain.referral.port.out;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralStatus;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReferralEventRepository {
    Mono<ReferralEvent> save(ReferralEvent event);
    Mono<ReferralEvent> findById(TenantId tenantId, UUID eventId);
    Mono<ReferralEvent> findPendingByRefereeId(TenantId tenantId, UserId refereeId);
    Flux<ReferralEvent> findByReferrerId(TenantId tenantId, UserId referrerId);
    Mono<Long> countByReferrerIdAndStatus(TenantId tenantId, UserId referrerId, ReferralStatus status);
}
