package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.event.PaymentConfirmedEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletClosedEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletCreditedEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletDebitedEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletFraudSuspectedEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletFrozenEvent;
import com.yowyob.loyaulty.program.domain.wallet.event.WalletUnfrozenEvent;
import reactor.core.publisher.Mono;

/**
 * Port de sortie (driven) : publication des événements de domaine sur Kafka.
 *
 * <p>Le domaine émet des events après chaque opération significative.
 * Ces events déclenchent des consumers dans d'autres bounded contexts
 * (notifications, analytics, fraud detection, AI engine…).</p>
 *
 * <p>Chaque méthode est fire-and-forget du point de vue du domaine,
 * mais retourne un {@code Mono<Void>} pour permettre la composition réactive
 * et la gestion d'erreurs à l'infrastructure.</p>
 *
 * <p>Topics Kafka correspondants :</p>
 * <ul>
 *   <li>{@code wallet.credited}</li>
 *   <li>{@code wallet.debited}</li>
 *   <li>{@code wallet.frozen}</li>
 *   <li>{@code wallet.unfrozen}</li>
 *   <li>{@code wallet.closed}</li>
 *   <li>{@code wallet.fraud_suspected}</li>
 *   <li>{@code payment.confirmed}</li>
 * </ul>
 */
public interface WalletEventPublisherPort {

    /**
     * Publie l'événement indiquant qu'un wallet a été crédité.
     *
     * @param event données de l'événement.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletCreditedEvent event);

    /**
     * Publie l'événement indiquant qu'un wallet a été débité.
     *
     * @param event données de l'événement.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletDebitedEvent event);

    /**
     * Publie l'événement indiquant qu'un wallet a été gelé.
     *
     * @param event données de l'événement.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletFrozenEvent event);

    /**
     * Publie l'événement indiquant qu'un wallet a été dégelé.
     *
     * @param event données de l'événement.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletUnfrozenEvent event);

    /**
     * Publie l'événement indiquant qu'un wallet a été clôturé.
     *
     * @param event données de l'événement.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletClosedEvent event);

    /**
     * Publie l'événement indiquant qu'une fraude a été suspectée sur un wallet.
     * Déclenche une notification admin et peut déclencher un gel automatique.
     *
     * @param event données de l'événement incluant le type de fraude détecté.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(WalletFraudSuspectedEvent event);

    /**
     * Publie l'événement indiquant qu'un paiement provider a été confirmé.
     * Déclenche la finalisation de la WalletTransaction associée.
     *
     * @param event données de l'événement incluant la référence externe provider.
     * @return {@code Mono<Void>} complété quand l'event est accepté par Kafka.
     */
    Mono<Void> publish(PaymentConfirmedEvent event);
}
