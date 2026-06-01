package com.yowyob.loyaulty.program.infrastructure.notification;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.List;
import java.util.Map;

/**
 * Consomme les événements Kafka qui nécessitent l'envoi de notifications aux membres.
 * Topics écoutés : wallet.events.*, loyalty.events.*
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final String bootstrapServers;
    private Disposable subscription;

    public NotificationEventConsumer(
            NotificationService notificationService,
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        this.notificationService = notificationService;
        this.bootstrapServers = bootstrapServers;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "loyalty-notification-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        );

        ReceiverOptions<String, Map<String, Object>> options = ReceiverOptions
                .<String, Map<String, Object>>create(consumerProps)
                .subscription(List.of("wallet.events", "loyalty.events"));

        subscription = KafkaReceiver.create(options)
                .receive()
                .flatMap(record -> {
                    Map<String, Object> event = record.value();
                    return processEvent(event)
                            .doOnSuccess(v -> record.receiverOffset().acknowledge())
                            .onErrorResume(e -> {
                                log.error("Failed to process notification event: {}", e.getMessage());
                                record.receiverOffset().acknowledge();
                                return Mono.empty();
                            });
                })
                .subscribe(
                        null,
                        err -> log.error("Notification consumer error", err),
                        () -> log.info("Notification consumer completed")
                );

        log.info("Notification event consumer started on topics: wallet.events, loyalty.events");
    }

    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Notification event consumer stopped");
        }
    }

    private Mono<Void> processEvent(Map<String, Object> event) {
        String eventType = String.valueOf(event.getOrDefault("eventType", "UNKNOWN"));
        String memberId  = String.valueOf(event.getOrDefault("memberId", ""));
        String tenantId  = String.valueOf(event.getOrDefault("tenantId", ""));

        String message = buildMessage(eventType, event);
        if (message == null) return Mono.empty();

        return notificationService.sendToMember(tenantId, memberId, eventType, message);
    }

    private String buildMessage(String eventType, Map<String, Object> event) {
        return switch (eventType) {
            case "wallet.credited"  -> "Votre wallet a été crédité de " + event.get("amount");
            case "wallet.debited"   -> "Paiement de " + event.get("amount") + " effectué";
            case "wallet.frozen"    -> "Votre wallet a été gelé : " + event.get("reason");
            case "points.earned"    -> "Vous avez gagné " + event.get("amount") + " points !";
            case "points.spent"     -> event.get("amount") + " points utilisés pour une récompense";
            case "member.enrolled"  -> "Bienvenue dans le programme de fidélité !";
            case "member.tier.upgraded" ->
                    "Félicitations ! Vous êtes passé au niveau " + event.get("newLevel");
            default -> null;
        };
    }
}
