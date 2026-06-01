package com.yowyob.loyaulty.program.infrastructure.persistence.promo.adapter;

import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.promo.port.out.PromoCampaignRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.mapper.PromoMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.promo.repository.PromoCampaignR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PromoCampaignRepositoryAdapter implements PromoCampaignRepository {

    private final PromoCampaignR2dbcRepository repository;
    private final PromoMapper mapper;

    public PromoCampaignRepositoryAdapter(PromoCampaignR2dbcRepository repository,
                                           PromoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PromoCampaign> save(PromoCampaign campaign) {
        return repository.save(mapper.toEntity(campaign)).map(mapper::toDomain);
    }

    @Override
    public Mono<PromoCampaign> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<PromoCampaign> findByCode(String code, TenantId tenantId) {
        return repository.findByCodeAndTenantId(code, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<PromoCampaign> findAllByTenant(TenantId tenantId) {
        return repository.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }
}
