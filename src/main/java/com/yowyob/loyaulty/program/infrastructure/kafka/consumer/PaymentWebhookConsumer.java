package com.yowyob.loyaulty.program.infrastructure.kafka.consumer;

import com.yowyob.loyaulty.program.domain.wallet.event.PaymentConfirmedEvent;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentDirection;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.PaymentRequestRepository;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Consommateur Kafka pour les confirmations de paiement provider.
 *
 * <p>Ce consumer écoute le topic {@code payment.confirmed} et finalise le cycle
 * de vie d'une PaymentRequest + WalletTransaction après confirmation d'un webhook.</p>
 *
 * <p>La logique est la suivante :</p>
 * <ol>
 *   <li>Lecture du {@link PaymentConfirmedEvent} depuis Kafka.</li>
 *   <li>Mise à jour de la {@code PaymentRequest} → CONFIRMED.</li>
 *   <li>Mise à jour de la {@code WalletTransaction} → COMPLETED.</li>
 *   <li>Pour une direction INBOUND : crédit effectif du solde du wallet.</li>
 *   <li>Pour une direction OUTBOUND : libération du montant réservé.</li>
 *   <li>Acknowledgment manuel après succès pour éviter la perte de message.</li>
 * </ol>
 *
 * <p>En cas d'échec de traitement, le message n'est pas acquitté
 * (retry automatique selon la config Kafka du consumer group).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWebhookConsumer {

    private final PaymentRequestRepository paymentRequestRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletEventPublisherPort eventPublisher;

    /**
     * Handler principal : traite la confirmation d'un paiement provider.
     *
     * <p>L'acknowledgment manuel ({@code ack.acknowledge()}) est requis pour
     * guarantir le traitement at-least-once combiné à l'idempotence applicative.</p>
     *
     * @param event l'event de confirmation envoyé par le WebhookController après validation.
     * @param key   clé de partitionnement Kafka (= walletId).
     * @param ack   acknowledgment manuel.
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.payment-confirmed:payment.confirmed}",
            groupId = "${spring.kafka.consumer.group-id:loyalty-wallet-group}",
            containerFactory = "walletKafkaListenerContainerFactory"
    )
    public void handlePaymentConfirmed(
            @Payload PaymentConfirmedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        log.info("Confirmation paiement reçue : paymentRequestId={}, provider={}, direction={}",
                event.paymentRequestId(), event.provider(), event.direction());

        processConfirmation(event)
                .doOnSuccess(v -> {
                    ack.acknowledge();
                    log.info("PaymentRequest traitée et acquittée : id={}", event.paymentRequestId());
                })
                .doOnError(err -> log.error(
                        "Échec traitement confirmation paiement : paymentRequestId={}, erreur={}",
                        event.paymentRequestId(), err.getMessage()))
                .subscribe();
    }

    // ── Logique de traitement ─────────────────────────────────────────────────

    private Mono<Void> processConfirmation(PaymentConfirmedEvent event) {
        return paymentRequestRepository
                .findById(event.paymentRequestId(), event.tenantId())
                .flatMap(paymentRequest -> {

                    // 1. Mettre à jour la PaymentRequest → CONFIRMED
                    return paymentRequestRepository.updateStatus(
                                    event.paymentRequestId(),
                                    event.tenantId(),
                                    PaymentRequestStatus.CONFIRMED,
                                    event.externalReference(),
                                    null
                            )

                            // 2. Mettre à jour la WalletTransaction → COMPLETED
                            .flatMap(pr -> walletTransactionRepository.updateStatus(
                                    pr.getWalletTransactionId(),
                                    event.tenantId(),
                                    TransactionStatus.COMPLETED,
                                    Instant.now()
                            ))

                            // 3. Ajuster le solde du wallet selon la direction
                            .flatMap(tx -> adjustWalletBalance(event))

                            // 4. Émettre l'event de domaine final (wallet.credited ou wallet.debited)
                            .flatMap(v -> emitDomainEvent(event));
                })
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "PaymentRequest introuvable : id=" + event.paymentRequestId())))
                .then();
    }

    private Mono<Void> adjustWalletBalance(PaymentConfirmedEvent event) {
        return walletRepository.findById(event.walletId(), event.tenantId())
                .flatMap(wallet -> {
                    BigDecimal newAvailable;
                    BigDecimal newReserved;

                    if (PaymentDirection.INBOUND.equals(event.direction())) {
                        // Recharge confirmée : on crédite le solde disponible
                        newAvailable = wallet.getAvailableBalance().add(event.amount());
                        newReserved  = wallet.getReservedBalance();
                    } else {
                        // Retrait confirmé : on libère le montant réservé (déjà déduit du disponible)
                        newAvailable = wallet.getAvailableBalance();
                        newReserved  = wallet.getReservedBalance().subtract(event.amount())
                                .max(BigDecimal.ZERO);
                    }

                    return walletRepository.save(
                            wallet.withAvailableBalance(newAvailable)
                                  .withReservedBalance(newReserved)
                    );
                })
                .then();
    }

    private Mono<Void> emitDomainEvent(PaymentConfirmedEvent event) {
        if (PaymentDirection.INBOUND.equals(event.direction())) {
            var creditedEvent = com.yowyob.loyaulty.program.domain.wallet.event.WalletCreditedEvent.of(
                    event.walletId(), null, event.tenantId(),
                    event.amount(), event.currency(),
                    "TOPUP_" + event.provider().name(),
                    BigDecimal.ZERO,  // balanceAfter sera recalculé par le handler
                    null
            );
            return eventPublisher.publish(creditedEvent);
        }
        return Mono.empty();
    }
}
