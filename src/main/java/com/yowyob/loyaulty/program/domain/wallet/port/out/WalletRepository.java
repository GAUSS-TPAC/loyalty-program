package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletRepository {
    Mono<Wallet> findById(UUID walletId);
    Mono<Wallet> findByMemberAndTenant(String memberId, TenantId tenantId);
    Mono<Wallet> save(Wallet wallet);
    Mono<Boolean> existsByMemberAndTenant(String memberId, TenantId tenantId);
}
