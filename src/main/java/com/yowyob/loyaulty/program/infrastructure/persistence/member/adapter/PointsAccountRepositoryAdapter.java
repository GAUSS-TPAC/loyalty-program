package com.yowyob.loyaulty.program.infrastructure.persistence.member.adapter;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsAccountRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.mapper.MemberMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.repository.PointsAccountR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PointsAccountRepositoryAdapter implements PointsAccountRepository {

    private final PointsAccountR2dbcRepository repository;
    private final MemberMapper mapper;

    public PointsAccountRepositoryAdapter(PointsAccountR2dbcRepository repository,
                                           MemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PointsAccount> save(PointsAccount account) {
        return repository.save(mapper.toEntity(account)).map(mapper::toDomain);
    }

    @Override
    public Mono<PointsAccount> findByMemberId(String memberId, TenantId tenantId) {
        return repository.findByMemberIdAndTenantId(memberId, tenantId.value())
                .map(mapper::toDomain);
    }
}
