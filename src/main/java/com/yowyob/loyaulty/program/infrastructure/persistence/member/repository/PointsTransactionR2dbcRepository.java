package com.yowyob.loyaulty.program.infrastructure.persistence.member.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.member.entity.PointsTransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PointsTransactionR2dbcRepository extends R2dbcRepository<PointsTransactionEntity, UUID> {

    @Query("SELECT * FROM points_transactions WHERE member_id = :memberId AND tenant_id = :tenantId ORDER BY occurred_at DESC LIMIT :limit OFFSET :offset")
    Flux<PointsTransactionEntity> findByMemberIdAndTenantIdPaged(String memberId, UUID tenantId,
                                                                   int limit, long offset);

    @Query("SELECT COUNT(*) FROM points_transactions WHERE member_id = :memberId AND tenant_id = :tenantId")
    Mono<Long> countByMemberIdAndTenantId(String memberId, UUID tenantId);
}
