package com.yowyob.loyalty.domain.bonification.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface BonificationTokenCachePort {

    Mono<String> getToken(TenantId tenantId);

    Mono<Void> saveToken(TenantId tenantId, String token, Duration ttl);
}
