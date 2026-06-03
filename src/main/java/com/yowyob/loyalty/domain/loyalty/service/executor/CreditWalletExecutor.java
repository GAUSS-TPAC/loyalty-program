package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class CreditWalletExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.CREDIT_WALLET;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        BigDecimal amount = effect.getParamAsBigDecimal("amount").orElse(BigDecimal.ZERO);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Invalid or missing amount"));
        }

        executionContext.addWalletCredit(context.event().memberId(), amount, "CASHBACK");

        return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("wallet_credited", amount));
    }
}
