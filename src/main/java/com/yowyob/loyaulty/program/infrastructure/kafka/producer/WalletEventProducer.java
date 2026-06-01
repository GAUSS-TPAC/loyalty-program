package com.yowyob.loyaulty.program.infrastructure.kafka.producer;

import com.yowyob.loyaulty.program.domain.wallet.event.*;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Producteur Kafka implémentant {@link WalletEventPublisherPort}.
 *
 * <p>Chaque event de domaine est sérialisé en JSON et publié sur son topic dédié.
 * L'envoi est idempotent côté Kafka car le producteur est configuré avec
 * {@code enable.idempotence=true} (voir {@code application.yml}).</p>
 *
 * <p>L'adaptateur wrapp le {@code KafkaTemplate} synchrone dans un {@code Mono}
 * via {@code Schedulers.boundedElastic()} pour ne pas bloquer le thread réactif.</p>
 *
 * <p>La clé de partitionnement est le {@code walletId} pour garantir l'ordre
 * des events d'un même wallet dans la même partition Kafka.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventProducer implements WalletEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.wallet-credited:wallet.credited}")
    private String topicWalletCredited;

    @Value("${spring.kafka.topics.wallet-debited:wallet.debited}")
    private String topicWalletDebited;

    @Value("${spring.kafka.topics.wallet-frozen:wallet.frozen}")
    private String topicWalletFrozen;

    @Value("${spring.kafka.topics.wallet-unfrozen:wallet.unfrozen}")
    private String topicWalletUnfrozen;

    @Value("${spring.kafka.topics.wallet-closed:wallet.closed}")
    private String topicWalletClosed;

    @Value("${spring.kafka.topics.wallet-fraud-suspected:wallet.fraud_suspected}")
    private String topicWalletFraudSuspected;

    @Value("${spring.kafka.topics.payment-confirmed:payment.confirmed}")
    private String topicPaymentConfirmed;

    // ── Implémentation du port ────────────────────────────────────────────────

    @Override
    public Mono<Void> publish(WalletCreditedEvent event) {
        return send(topicWalletCredited, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(WalletDebitedEvent event) {
        return send(topicWalletDebited, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(WalletFrozenEvent event) {
        return send(topicWalletFrozen, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(WalletUnfrozenEvent event) {
        return send(topicWalletUnfrozen, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(WalletClosedEvent event) {
        return send(topicWalletClosed, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(WalletFraudSuspectedEvent event) {
        return send(topicWalletFraudSuspected, event.walletId().toString(), event);
    }

    @Override
    public Mono<Void> publish(PaymentConfirmedEvent event) {
        return send(topicPaymentConfirmed, event.walletId().toString(), event);
    }

    // ── Helper générique ─────────────────────────────────────────────────────

    /**
     * Envoie un message sur un topic Kafka de façon réactive.
     *
     * <p>La clé de partitionnement ({@code partitionKey}) garantit que tous les
     * events d'un même wallet arrivent dans la même partition, préservant l'ordre.</p>
     *
     * @param topic        nom du topic Kafka cible.
     * @param partitionKey clé de partitionnement (walletId).
     * @param payload      objet à sérialiser en JSON.
     * @return {@code Mono<Void>} complété quand le broker Kafka accuse réception.
     */
    private Mono<Void> send(String topic, String partitionKey, Object payload) {
        return Mono.fromFuture(() -> kafkaTemplate.send(topic, partitionKey, payload).toCompletableFuture())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.debug(
                        "Event publié : topic={}, key={}, offset={}",
                        topic, partitionKey,
                        result.getRecordMetadata().offset()))
                .doOnError(err -> log.error(
                        "Échec publication event Kafka : topic={}, key={}, erreur={}",
                        topic, partitionKey, err.getMessage()))
                .then();
    }
}
