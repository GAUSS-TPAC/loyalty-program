package com.yowyob.loyaulty.program.infrastructure.persistence.referral.adapter;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralLink;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.repository.ReferralLinkR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReferralLinkRepositoryAdapter implements ReferralLinkRepository {

    private final ReferralLinkR2dbcRepository repository;
    private final ReferralMapper mapper;

    public ReferralLinkRepositoryAdapter(ReferralLinkR2dbcRepository repository, ReferralMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralLink> findByReferrerId(TenantId tenantId, String referrerId) {
        return repository.findByTenantIdAndReferrerId(tenantId.value(), referrerId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralLink> findByCode(TenantId tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId.value(), code)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralLink> save(ReferralLink link) {
        return repository.save(mapper.toEntity(link)).map(mapper::toDomain);
    }
}
