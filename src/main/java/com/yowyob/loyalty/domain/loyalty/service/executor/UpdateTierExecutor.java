package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;

import java.util.Map;
import java.util.UUID;

public class UpdateTierExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.UPDATE_TIER;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        String tierLevelStr = effect.getParamAsString("tier_level").orElse(null);

        if (tierLevelStr == null || tierLevelStr.isBlank()) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Missing tier_level"));
        }

        try {
            TierLevel tierLevel = TierLevel.valueOf(tierLevelStr.toUpperCase());
            executionContext.addTierUpdate(context.event().memberId(), tierLevel);
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("new_tier", tierLevelStr));
        } catch (IllegalArgumentException e) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Unknown tier level: " + tierLevelStr));
        }
    }
}
