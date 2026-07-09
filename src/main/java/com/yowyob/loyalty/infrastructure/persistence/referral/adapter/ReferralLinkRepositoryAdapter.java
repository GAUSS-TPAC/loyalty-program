package com.yowyob.loyalty.infrastructure.persistence.referral.adapter;

import com.yowyob.loyalty.domain.referral.exception.ReferralLinkNotFoundException;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralLinkEntity;
import com.yowyob.loyalty.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyalty.infrastructure.persistence.referral.repository.ReferralLinkR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReferralLinkRepositoryAdapter implements ReferralLinkRepository {

    private final ReferralLinkR2dbcRepository r2dbcRepo;
    private final ReferralMapper mapper;
    private final R2dbcEntityTemplate template;

    public ReferralLinkRepositoryAdapter(ReferralLinkR2dbcRepository r2dbcRepo, ReferralMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<ReferralLink> save(ReferralLink link) {
        ReferralLinkEntity entity = mapper.toEntity(link);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
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
