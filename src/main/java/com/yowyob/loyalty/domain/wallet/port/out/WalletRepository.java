package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface WalletRepository {
    Mono<Wallet> findByMemberAndTenant(UserId memberId, TenantId tenantId);
    Mono<Wallet> findById(UUID id);
    Mono<Wallet> save(Wallet wallet);
    Mono<Boolean> existsByMemberAndTenant(UserId memberId, TenantId tenantId);
    Flux<Wallet> findAllByTenant(TenantId tenantId, int page, int size);
}
