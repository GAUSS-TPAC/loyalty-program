package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreditPointsExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.CREDIT_POINTS || type == EffectType.MULTIPLY_POINTS
                || type == EffectType.DEBIT_POINTS;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        if (effect.type() == EffectType.CREDIT_POINTS) {
            Long amount = effect.getParamAsLong("amount").orElse(0L);
            if (amount <= 0) {
                return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Invalid or missing amount"));
            }

            BigDecimal multiplier = BigDecimal.ONE;
            if (context.memberTier() != null && context.memberTier().pointsMultiplier() != null) {
                multiplier = context.memberTier().pointsMultiplier();
            }

            long finalAmount = BigDecimal.valueOf(amount).multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValue();

            executionContext.addPointsCredit(context.event().memberId(), finalAmount, "CREDIT", ruleId);

            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, 
                    Map.of("points_credited", finalAmount, "multiplier_applied", multiplier));
        } else if (effect.type() == EffectType.DEBIT_POINTS) {
            Long amount = effect.getParamAsLong("amount").orElse(0L);
            if (amount <= 0) {
                return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Invalid or missing amount"));
            }

            if (context.pointsAccount() == null || !context.pointsAccount().hasEnoughPoints(amount)) {
                return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName,
                        Map.of("error", "Insufficient points balance"));
            }

            executionContext.addPointsCredit(context.event().memberId(), amount, "DEBIT", ruleId);

            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName,
                    Map.of("points_debited", amount));
        } else if (effect.type() == EffectType.MULTIPLY_POINTS) {
            BigDecimal multiplier = effect.getParamAsBigDecimal("multiplier").orElse(BigDecimal.ONE);
            
            List<EffectExecutionContext.PointsOperation> pointsOps = executionContext.getPendingPointsOperations();
            for (int i = 0; i < pointsOps.size(); i++) {
                EffectExecutionContext.PointsOperation op = pointsOps.get(i);
                if ("CREDIT".equals(op.type())) {
                    long newAmount = BigDecimal.valueOf(op.amount()).multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValue();
                    pointsOps.set(i, new EffectExecutionContext.PointsOperation(op.memberId(), newAmount, op.type(), op.ruleId()));
                }
            }

            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("multiplier", multiplier));
        }

        return null;
    }
}
