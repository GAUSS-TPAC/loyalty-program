package com.yowyob.loyaulty.program.infrastructure.persistence.member.adapter;

import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsTransactionRepository;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.mapper.MemberMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.repository.PointsTransactionR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PointsTransactionRepositoryAdapter implements PointsTransactionRepository {

    private final PointsTransactionR2dbcRepository repository;
    private final MemberMapper mapper;

    public PointsTransactionRepositoryAdapter(PointsTransactionR2dbcRepository repository,
                                               MemberMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PointsTransaction> save(PointsTransaction tx) {
        return repository.save(mapper.toEntity(tx)).map(mapper::toDomain);
    }

    @Override
    public Mono<PageResult<PointsTransaction>> findByMemberId(String memberId, TenantId tenantId,
                                                               PageRequest pageRequest) {
        long offset = (long) pageRequest.page() * pageRequest.size();
        Mono<Long> countMono = repository.countByMemberIdAndTenantId(memberId, tenantId.value());
        Mono<List<PointsTransaction>> itemsMono = repository
                .findByMemberIdAndTenantIdPaged(memberId, tenantId.value(),
                        pageRequest.size(), offset)
                .map(mapper::toDomain)
                .collectList();

        return Mono.zip(itemsMono, countMono)
                .map(t -> PageResult.of(t.getT1(), t.getT2(),
                        pageRequest.page(), pageRequest.size()));
    }
}
