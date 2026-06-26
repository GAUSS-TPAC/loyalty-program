package com.yowyob.loyalty.domain.reward.port.in;

import com.yowyob.loyalty.domain.reward.model.ConsumeGrantRequest;
import com.yowyob.loyalty.domain.reward.model.ConsumeGrantResult;
import reactor.core.publisher.Mono;

public interface ConsumeGrantUseCase {
    Mono<ConsumeGrantResult> consumeGrant(ConsumeGrantRequest request);
}
