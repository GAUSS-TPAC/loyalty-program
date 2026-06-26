package com.yowyob.loyalty.infrastructure.persistence.referral.adapter;

import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralStatus;
import com.yowyob.loyalty.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyalty.infrastructure.persistence.referral.repository.ReferralEventR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReferralEventRepositoryAdapter implements ReferralEventRepository {

    private final ReferralEventR2dbcRepository r2dbcRepo;
    private final ReferralMapper mapper;

    public ReferralEventRepositoryAdapter(ReferralEventR2dbcRepository r2dbcRepo, ReferralMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralEvent> save(ReferralEvent event) {
        return r2dbcRepo.save(mapper.toEntity(event)).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralEvent> findById(TenantId tenantId, UUID eventId) {
        return r2dbcRepo.findByIdAndTenantId(eventId, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralEvent> findPendingByRefereeId(TenantId tenantId, UserId refereeId) {
        return r2dbcRepo.findPendingByRefereeIdAndTenantId(tenantId.value(), refereeId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<ReferralEvent> findByReferrerId(TenantId tenantId, UserId referrerId) {
        return r2dbcRepo.findByReferrerIdAndTenantId(referrerId.value(), tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByReferrerIdAndStatus(TenantId tenantId, UserId referrerId, ReferralStatus status) {
        return r2dbcRepo.countByReferrerIdAndTenantIdAndStatus(referrerId.value(), tenantId.value(), status.name());
    }
}
