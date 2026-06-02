package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.wallet.event.WalletDomainEvent;
import reactor.core.publisher.Mono;

public interface WalletEventPublisherPort {
    Mono<Void> publish(WalletDomainEvent event);
}
