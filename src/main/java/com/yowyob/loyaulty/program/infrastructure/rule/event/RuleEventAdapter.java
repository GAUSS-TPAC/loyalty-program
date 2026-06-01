package com.yowyob.loyaulty.program.infrastructure.rule.event;

import com.yowyob.loyaulty.program.domain.rule.model.AppliedEffect;
import com.yowyob.loyaulty.program.domain.rule.model.LoyaltyEvent;
import com.yowyob.loyaulty.program.domain.rule.port.out.RuleEventPublisherPort;
import com.yowyob.loyaulty.program.infrastructure.kafka.producer.WalletEventProducer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class RuleEventAdapter implements RuleEventPublisherPort {

    private static final String TOPIC = "loyalty.effects";

    private final WalletEventProducer eventProducer;

    public RuleEventAdapter(WalletEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @Override
    public Mono<Void> publishEffects(LoyaltyEvent event, List<AppliedEffect> effects) {
        Map<String, Object> payload = Map.of(
                "eventId",   event.eventId().toString(),
                "tenantId",  event.tenantId().value().toString(),
                "memberId",  event.memberId(),
                "eventType", event.eventType(),
                "effects",   effects
        );
        return eventProducer.publish(TOPIC, payload);
    }
}
