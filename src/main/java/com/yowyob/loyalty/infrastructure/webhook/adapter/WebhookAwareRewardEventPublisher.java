package com.yowyob.loyalty.infrastructure.webhook.adapter;

import com.yowyob.loyalty.application.webhook.WebhookDispatchService;
import com.yowyob.loyalty.domain.reward.event.RewardDomainEvent;
import com.yowyob.loyalty.domain.reward.port.out.RewardEventPublisherPort;
import com.yowyob.loyalty.domain.webhook.model.WebhookEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Primary
@Profile("!test")
public class WebhookAwareRewardEventPublisher implements RewardEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookAwareRewardEventPublisher.class);

    private final RewardEventPublisherPort delegate;
    private final WebhookDispatchService webhookDispatchService;

    public WebhookAwareRewardEventPublisher(RewardEventPublisherPort delegate, WebhookDispatchService webhookDispatchService) {
        this.delegate = delegate;
        this.webhookDispatchService = webhookDispatchService;
    }

    @Override
    public Mono<Void> publish(RewardDomainEvent event) {
        return delegate.publish(event).then(dispatchIfRelevant(event));
    }

    private Mono<Void> dispatchIfRelevant(RewardDomainEvent event) {
        WebhookEventType type = mapEventType(event.eventType());
        if (type == null) return Mono.empty();

        return webhookDispatchService.dispatch(event.tenantId(), type, Map.of(
                        "eventId", event.eventId().toString(),
                        "eventType", event.eventType(),
                        "occurredAt", event.occurredAt().toString()))
                .doOnError(e -> log.error("Webhook dispatch error for reward event {}: {}", event.eventId(), e.getMessage()));
    }

    private static WebhookEventType mapEventType(String eventType) {
        return switch (eventType) {
            case "reward.granted" -> WebhookEventType.REWARD_GRANTED;
            case "reward.consumed", "reward.redeemed" -> WebhookEventType.REWARD_REDEEMED;
            default -> null;
        };
    }
}
