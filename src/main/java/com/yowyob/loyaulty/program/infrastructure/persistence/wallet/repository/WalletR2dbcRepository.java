package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletR2dbcRepository extends ReactiveCrudRepository<WalletEntity, UUID> {

    @Query("SELECT * FROM wallets WHERE member_id = :memberId AND tenant_id = :tenantId")
    Mono<WalletEntity> findByMemberIdAndTenantId(String memberId, UUID tenantId);

    @Query("SELECT COUNT(*) > 0 FROM wallets WHERE member_id = :memberId AND tenant_id = :tenantId")
    Mono<Boolean> existsByMemberIdAndTenantId(String memberId, UUID tenantId);
}
