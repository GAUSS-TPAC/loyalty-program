package com.yowyob.loyalty.domain.wallet.port.out;

import reactor.core.publisher.Mono;
import java.time.Duration;

public interface IdempotencyPort {
    Mono<Boolean> exists(String idempotencyKey, String tenantId);
    Mono<Boolean> registerIfAbsent(String idempotencyKey, String tenantId, Duration ttl, String resultPayload);
    Mono<String> getResult(String idempotencyKey, String tenantId);
}
