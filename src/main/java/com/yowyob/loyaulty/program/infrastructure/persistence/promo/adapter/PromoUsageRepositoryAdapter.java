package com.yowyob.loyaulty.program.infrastructure.persistence.promo.adapter;

import com.yowyob.loyaulty.program.domain.promo.model.PromoUsage;
import com.yowyob.loyaulty.program.domain.promo.port.out.PromoUsageRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.mapper.PromoMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.repository.PromoUsageR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PromoUsageRepositoryAdapter implements PromoUsageRepository {

    private final PromoUsageR2dbcRepository repository;
    private final PromoMapper mapper;

    public PromoUsageRepositoryAdapter(PromoUsageR2dbcRepository repository, PromoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PromoUsage> save(PromoUsage usage) {
        return repository.save(mapper.toEntity(usage)).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByCampaign(UUID campaignId, TenantId tenantId) {
        return repository.countByCampaignIdAndTenantId(campaignId, tenantId.value());
    }

    @Override
    public Mono<Long> countByCampaignAndMember(UUID campaignId, String memberId, TenantId tenantId) {
        return repository.countByCampaignIdAndMemberIdAndTenantId(campaignId, memberId, tenantId.value());
    }
}
