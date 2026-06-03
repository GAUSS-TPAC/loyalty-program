package com.yowyob.loyalty.infrastructure.persistence.wallet.repository;

import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface WalletR2dbcRepository extends ReactiveCrudRepository<WalletEntity, UUID> {
    Mono<WalletEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);
    Mono<Boolean> existsByMemberIdAndTenantId(UUID memberId, UUID tenantId);
}
