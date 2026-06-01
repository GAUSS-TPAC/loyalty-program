package com.yowyob.loyaulty.program.domain.wallet.port.out;

import reactor.core.publisher.Mono;

public interface IdempotencyPort {
    Mono<Boolean> isNew(String key);
    Mono<Void> markProcessing(String key);
    Mono<Void> storeResponse(String key, String responseJson);
    Mono<String> getStoredResponse(String key);
}
