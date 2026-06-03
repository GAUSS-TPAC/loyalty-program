package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectType;

import java.util.Map;
import java.util.UUID;

public class SendNotificationExecutor implements EffectExecutor {

    @Override
    public boolean supports(EffectType type) {
        return type == EffectType.SEND_NOTIFICATION;
    }

    @Override
    public AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext, UUID ruleId, String ruleName) {
        String template = effect.getParamAsString("template").orElse(null);

        if (template == null || template.isBlank()) {
            return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("error", "Missing template"));
        }

        executionContext.addNotification(context.event().memberId(), template, context.event().payload());

        return new AppliedEffect(effect.type().name(), ruleId.toString(), ruleName, Map.of("notification_queued", template));
    }
}
