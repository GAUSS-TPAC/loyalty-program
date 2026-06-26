package com.yowyob.loyalty.infrastructure.persistence.referral.adapter;

import com.yowyob.loyalty.domain.referral.exception.ReferralProgramNotFoundException;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyalty.infrastructure.persistence.referral.repository.ReferralProgramR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReferralProgramRepositoryAdapter implements ReferralProgramRepository {

    private final ReferralProgramR2dbcRepository r2dbcRepo;
    private final ReferralMapper mapper;

    public ReferralProgramRepositoryAdapter(ReferralProgramR2dbcRepository r2dbcRepo, ReferralMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralProgram> save(ReferralProgram program) {
        return r2dbcRepo.save(mapper.toEntity(program)).map(mapper::toDomain);
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
