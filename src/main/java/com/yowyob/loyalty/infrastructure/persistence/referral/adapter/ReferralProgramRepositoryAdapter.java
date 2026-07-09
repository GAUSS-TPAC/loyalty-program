package com.yowyob.loyalty.infrastructure.persistence.referral.adapter;

import com.yowyob.loyalty.domain.referral.exception.ReferralProgramNotFoundException;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.referral.entity.ReferralProgramEntity;
import com.yowyob.loyalty.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyalty.infrastructure.persistence.referral.repository.ReferralProgramR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReferralProgramRepositoryAdapter implements ReferralProgramRepository {

    private final ReferralProgramR2dbcRepository r2dbcRepo;
    private final ReferralMapper mapper;
    private final R2dbcEntityTemplate template;

    public ReferralProgramRepositoryAdapter(ReferralProgramR2dbcRepository r2dbcRepo, ReferralMapper mapper, R2dbcEntityTemplate template) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<ReferralProgram> save(ReferralProgram program) {
        ReferralProgramEntity entity = mapper.toEntity(program);
        return r2dbcRepo.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbcRepo.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralProgram> findById(TenantId tenantId, UUID programId) {
        return r2dbcRepo.findByIdAndTenantId(programId, tenantId.value())
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.error(new ReferralProgramNotFoundException(programId)));
    }

    @Override
    public Mono<ReferralProgram> findActiveByTenantId(TenantId tenantId) {
        return r2dbcRepo.findByTenantIdAndActive(tenantId.value(), true)
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.error(new ReferralProgramNotFoundException(null)));
    }
}
