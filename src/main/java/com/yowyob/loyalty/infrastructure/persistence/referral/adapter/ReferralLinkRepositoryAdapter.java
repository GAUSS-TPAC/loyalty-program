package com.yowyob.loyalty.infrastructure.persistence.referral.adapter;

import com.yowyob.loyalty.domain.referral.exception.ReferralLinkNotFoundException;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyalty.infrastructure.persistence.referral.repository.ReferralLinkR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReferralLinkRepositoryAdapter implements ReferralLinkRepository {

    private final ReferralLinkR2dbcRepository r2dbcRepo;
    private final ReferralMapper mapper;

    public ReferralLinkRepositoryAdapter(ReferralLinkR2dbcRepository r2dbcRepo, ReferralMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralLink> save(ReferralLink link) {
        return r2dbcRepo.save(mapper.toEntity(link)).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralLink> findByCode(TenantId tenantId, String code) {
        return r2dbcRepo.findByCodeAndTenantId(code, tenantId.value())
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.error(new ReferralLinkNotFoundException(code)));
    }

    @Override
    public Mono<ReferralLink> findByReferrerId(TenantId tenantId, UserId referrerId) {
        return r2dbcRepo.findByReferrerIdAndTenantId(referrerId.value(), tenantId.value())
                .map(mapper::toDomain);
    }
}
