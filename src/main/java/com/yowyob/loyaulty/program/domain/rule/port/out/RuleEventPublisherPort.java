package com.yowyob.loyaulty.program.domain.rule.port.out;

import com.yowyob.loyaulty.program.domain.rule.model.AppliedEffect;
import com.yowyob.loyaulty.program.domain.rule.model.LoyaltyEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RuleEventPublisherPort {
    Mono<Void> publishEffects(LoyaltyEvent event, List<AppliedEffect> effects);
}
