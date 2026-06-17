package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.util.Map;
import java.util.UUID;

public class GrantRewardExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.GRANT_REWARD;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        String rewardId = effect.getParamAsString("reward_id").orElse(null);

        if (rewardId == null || rewardId.isBlank()) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Missing reward_id"));
        }

        double amount = effect.getParamAsBigDecimal("amount")
                .map(java.math.BigDecimal::doubleValue)
                .orElse(0.0);

        executionContext.addRewardGrant(context.event().memberId(), rewardId, ruleId, amount);

        return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("reward_id", rewardId));
    }
}
