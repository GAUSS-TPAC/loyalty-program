package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.util.UUID;

public interface EffectExecutor {
    boolean supports(EffectType type);
    AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName);
}
