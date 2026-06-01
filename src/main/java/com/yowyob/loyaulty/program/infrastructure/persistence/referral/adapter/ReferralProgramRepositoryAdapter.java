package com.yowyob.loyaulty.program.infrastructure.persistence.referral.adapter;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralProgram;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.mapper.ReferralMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.referral.repository.ReferralProgramR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReferralProgramRepositoryAdapter implements ReferralProgramRepository {

    private final ReferralProgramR2dbcRepository repository;
    private final ReferralMapper mapper;

    public ReferralProgramRepositoryAdapter(ReferralProgramR2dbcRepository repository, ReferralMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<ReferralProgram> findByTenant(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<ReferralProgram> save(ReferralProgram program) {
        return repository.save(mapper.toEntity(program)).map(mapper::toDomain);
    }
}
