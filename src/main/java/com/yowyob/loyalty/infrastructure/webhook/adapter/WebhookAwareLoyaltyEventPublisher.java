package com.yowyob.loyalty.infrastructure.webhook.adapter;

import com.yowyob.loyalty.application.webhook.WebhookDispatchService;
import com.yowyob.loyalty.domain.loyalty.model.event.AppliedEffect;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.port.out.LoyaltyEventPublisherPort;
import com.yowyob.loyalty.domain.webhook.model.WebhookEventType;
import com.yowyob.loyalty.infrastructure.kafka.producer.LoyaltyEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Primary
@Profile("!test")
public class WebhookAwareLoyaltyEventPublisher implements LoyaltyEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookAwareLoyaltyEventPublisher.class);

    private final LoyaltyEventProducer delegate;
    private final WebhookDispatchService webhookDispatchService;

    public WebhookAwareLoyaltyEventPublisher(LoyaltyEventProducer delegate, WebhookDispatchService webhookDispatchService) {
        this.delegate = delegate;
        this.webhookDispatchService = webhookDispatchService;
    }

    @Override
    public void publishProcessedEvent(EventProcessingResult result) {
        delegate.publishProcessedEvent(result);
        if (!result.hasEffects()) return;

        for (AppliedEffect effect : result.effectsApplied()) {
            WebhookEventType type = mapEffectType(effect.effectType());
            if (type == null) continue;
            webhookDispatchService.dispatch(result.tenantId(), type, Map.of(
                            "memberId", result.memberId().toString(),
                            "effectType", effect.effectType(),
                            "ruleId", effect.ruleId() != null ? effect.ruleId() : "",
                            "ruleName", effect.ruleName() != null ? effect.ruleName() : "",
                            "details", effect.details() != null ? effect.details() : Map.of()))
                    .doOnError(e -> log.error("Webhook dispatch error for event {}: {}", result.eventId(), e.getMessage()))
                    .subscribe();
        }
    }

    private static WebhookEventType mapEffectType(String effectType) {
        return switch (effectType) {
            case "CREDIT_POINTS" -> WebhookEventType.POINTS_EARNED;
            case "DEBIT_POINTS" -> WebhookEventType.POINTS_REDEEMED;
            case "UPDATE_TIER" -> WebhookEventType.TIER_CHANGED;
            default -> null;
        };
    }
}
