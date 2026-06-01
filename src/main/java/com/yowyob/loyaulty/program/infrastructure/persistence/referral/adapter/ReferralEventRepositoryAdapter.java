package com.yowyob.loyaulty.program.infrastructure.persistence.referral.adapter;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.repository.ReferralEventR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReferralEventRepositoryAdapter implements ReferralEventRepository {

    private final ReferralEventR2dbcRepository repository;
    private final ReferralMapper mapper;

    public ReferralEventRepositoryAdapter(ReferralEventR2dbcRepository repository, ReferralMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralEvent> save(ReferralEvent event) {
        return repository.save(mapper.toEntity(event)).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralEvent> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralEvent> findPendingByReferee(TenantId tenantId, String refereeId) {
        return repository.findPendingByRefereeId(tenantId.value(), refereeId).map(mapper::toDomain);
    }

    @Override
    public Flux<ReferralEvent> findByReferrer(TenantId tenantId, String referrerId) {
        return repository.findByTenantIdAndReferrerId(tenantId.value(), referrerId).map(mapper::toDomain);
    }

    @Override
    public Flux<ReferralEvent> findAllByTenant(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value()).map(mapper::toDomain);
    }
}
