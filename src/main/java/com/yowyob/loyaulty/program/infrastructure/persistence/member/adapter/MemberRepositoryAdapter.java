package com.yowyob.loyaulty.program.infrastructure.persistence.member.adapter;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.mapper.MemberMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.repository.MemberR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class MemberRepositoryAdapter implements MemberRepository {

    private final MemberR2dbcRepository repository;
    private final MemberMapper mapper;

    public MemberRepositoryAdapter(MemberR2dbcRepository repository, MemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Member> save(Member member) {
        return repository.save(mapper.toEntity(member)).map(mapper::toDomain);
    }

    @Override
    public Mono<Member> findById(UUID id, TenantId tenantId) {
        return repository.findByIdAndTenantId(id, tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<Member> findByExternalId(String externalId, TenantId tenantId) {
        return repository.findByExternalIdAndTenantId(externalId, tenantId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByExternalId(String externalId, TenantId tenantId) {
        return repository.existsByExternalIdAndTenantId(externalId, tenantId.value());
    }

    @Override
    public Flux<Member> findAllByTenant(TenantId tenantId) {
        return repository.findAllByTenantId(tenantId.value()).map(mapper::toDomain);
    }
}
