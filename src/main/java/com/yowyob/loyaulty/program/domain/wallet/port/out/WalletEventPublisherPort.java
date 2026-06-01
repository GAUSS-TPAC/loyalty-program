package com.yowyob.loyaulty.program.domain.wallet.port.out;

import reactor.core.publisher.Mono;

public interface WalletEventPublisherPort {
    Mono<Void> publish(String topic, Object event);
    Mono<Void> publishAll(String topic, java.util.List<?> events);
}
