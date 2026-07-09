package com.yowyob.loyalty.infrastructure.persistence.campaign.adapter;

import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.port.out.CampaignRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.campaign.entity.CampaignEntity;
import com.yowyob.loyalty.infrastructure.persistence.campaign.mapper.CampaignMapper;
import com.yowyob.loyalty.infrastructure.persistence.campaign.repository.CampaignR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class CampaignRepositoryAdapter implements CampaignRepository {

    private final CampaignR2dbcRepository r2dbcRepo;
    private final CampaignMapper mapper;
    private final R2dbcEntityTemplate template;

    public CampaignRepositoryAdapter(CampaignR2dbcRepository r2dbcRepo, CampaignMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<Campaign> save(Campaign campaign) {
        CampaignEntity entity = mapper.toEntity(campaign);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Campaign> findById(TenantId tenantId, UUID id) {
        return r2dbcRepo.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<Campaign> findAll(TenantId tenantId) {
        return r2dbcRepo.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<Campaign> findByStatus(TenantId tenantId, CampaignStatus status) {
        return r2dbcRepo.findAllByTenantIdAndStatus(tenantId.value(), status.name()).map(mapper::toDomain);
    }

    @Override
    public Flux<Campaign> findDueForActivation(Instant now) {
        return r2dbcRepo.findDueForActivation(now).map(mapper::toDomain);
    }

    @Override
    public Flux<Campaign> findDueForCompletion(Instant now) {
        return r2dbcRepo.findDueForCompletion(now).map(mapper::toDomain);
    }
}
