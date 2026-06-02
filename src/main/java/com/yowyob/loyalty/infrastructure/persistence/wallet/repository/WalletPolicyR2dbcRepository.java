package com.yowyob.loyalty.infrastructure.persistence.wallet.repository;

import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletPolicyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface WalletPolicyR2dbcRepository extends ReactiveCrudRepository<WalletPolicyEntity, UUID> {
    Mono<WalletPolicyEntity> findByTenantId(UUID tenantId);
}
