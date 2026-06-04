package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.util.Map;
import java.util.UUID;

public class ResetCounterExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.RESET_COUNTER;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        String counterKey = effect.getParamAsString("counter_key").orElse(null);

        if (counterKey == null || counterKey.isBlank()) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Missing counter_key"));
        }

        executionContext.addCounterReset(counterKey);

        return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("counter_reset", counterKey));
    }
}
