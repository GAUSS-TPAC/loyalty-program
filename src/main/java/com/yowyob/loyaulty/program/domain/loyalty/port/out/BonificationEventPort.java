package com.yowyob.loyaulty.program.domain.loyalty.port.out;

import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;
import reactor.core.publisher.Mono;

public interface BonificationEventPort {
    Mono<Void> publish(DomainEvent event);
}
