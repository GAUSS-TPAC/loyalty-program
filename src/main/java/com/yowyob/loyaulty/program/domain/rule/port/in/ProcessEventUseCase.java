package com.yowyob.loyaulty.program.domain.rule.port.in;

import com.yowyob.loyaulty.program.domain.rule.model.EvaluationResult;
import com.yowyob.loyaulty.program.domain.rule.model.LoyaltyEvent;
import reactor.core.publisher.Mono;

public interface ProcessEventUseCase {
    Mono<EvaluationResult> process(LoyaltyEvent event);
}
