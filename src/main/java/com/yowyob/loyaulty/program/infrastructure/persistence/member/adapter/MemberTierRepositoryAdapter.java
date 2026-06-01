package com.yowyob.loyaulty.program.infrastructure.persistence.member.adapter;

import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberTierRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.mapper.MemberMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.repository.MemberTierR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MemberTierRepositoryAdapter implements MemberTierRepository {

    private final MemberTierR2dbcRepository repository;
    private final MemberMapper mapper;

    public MemberTierRepositoryAdapter(MemberTierR2dbcRepository repository, MemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<MemberTier> save(MemberTier tier) {
        return repository.save(mapper.toEntity(tier)).map(mapper::toDomain);
    }

    @Override
    public Mono<MemberTier> findByMemberId(String memberId, TenantId tenantId) {
        return repository.findByMemberIdAndTenantId(memberId, tenantId.value())
                .map(mapper::toDomain);
    }
}
