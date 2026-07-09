package com.yowyob.loyalty.infrastructure.persistence.promo.adapter;

import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.port.out.PromoCampaignRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.promo.entity.PromoCampaignEntity;
import com.yowyob.loyalty.infrastructure.persistence.promo.mapper.PromoMapper;
import com.yowyob.loyalty.infrastructure.persistence.promo.repository.PromoCampaignR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PromoCampaignRepositoryAdapter implements PromoCampaignRepository {

    private final PromoCampaignR2dbcRepository r2dbcRepo;
    private final PromoMapper mapper;
    private final R2dbcEntityTemplate template;

    public PromoCampaignRepositoryAdapter(PromoCampaignR2dbcRepository r2dbcRepo, PromoMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<PromoCampaign> save(PromoCampaign campaign) {
        PromoCampaignEntity entity = mapper.toEntity(campaign);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<PromoCampaign> findById(TenantId tenantId, UUID id) {
        return r2dbcRepo.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<PromoCampaign> findByCode(TenantId tenantId, String code) {
        return r2dbcRepo.findByTenantIdAndCode(tenantId.value(), code.toUpperCase().trim()).map(mapper::toDomain);
    }

    @Override
    public Flux<PromoCampaign> findAll(TenantId tenantId) {
        return r2dbcRepo.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<PromoCampaign> findActive(TenantId tenantId) {
        return r2dbcRepo.findAllByTenantIdAndActive(tenantId.value(), true).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(TenantId tenantId, UUID id) {
        return r2dbcRepo.deleteByIdAndTenantId(id, tenantId.value());
    }
}
