package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import reactor.core.publisher.Mono;

public interface WalletPolicyRepository {
    Mono<WalletPolicy> findByTenant(TenantId tenantId);

    Mono<WalletPolicy> save(TenantId tenantId, WalletPolicy policy);
}
