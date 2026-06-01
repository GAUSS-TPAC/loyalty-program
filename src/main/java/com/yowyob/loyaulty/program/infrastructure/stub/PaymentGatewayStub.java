package com.yowyob.loyaulty.program.infrastructure.stub;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentInitiationResult;
import com.yowyob.loyaulty.program.domain.wallet.model.PaymentStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.PaymentGatewayPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Stub de développement pour {@link PaymentGatewayPort}.
 *
 * <p>Actif uniquement avec le profil Spring {@code stub}.
 * Simule un provider Mobile Money (code USSD MTN) sans appel réseau.
 * Les confirmations de paiement sont simulées avec un délai de 1 seconde.
 * À remplacer par {@code KernelCorePaymentAdapter} quand le Kernel Core sera disponible.</p>
 */
@Slf4j
@Component
@Profile("stub")
public class PaymentGatewayStub implements PaymentGatewayPort {

    @PostConstruct
    public void logWarning() {
        log.warn("[STUB] PaymentGatewayStub actif — toutes les opérations de paiement sont simulées. " +
                "Remplacer par KernelCorePaymentAdapter en production.");
    }

    @Override
    public Mono<PaymentInitiationResult> initiateTopUp(
            UUID tenantId,
            UUID memberId,
            BigDecimal amount,
            String currency,
            String provider,
            String idempotencyKey) {

        String externalRef = UUID.randomUUID().toString();
        String ussdCode = "*126*1*" + amount.toPlainString() + "#";

        log.debug("[STUB] TopUp simulé : tenantId={}, memberId={}, amount={} {}, ref={}",
                tenantId, memberId, amount, currency, externalRef);

        return Mono.just(new PaymentInitiationResult(
                externalRef,
                PaymentStatus.PENDING,
                null,
                ussdCode,
                Instant.now().plusSeconds(300)
        ));
    }

    @Override
    public Mono<PaymentStatus> getPaymentStatus(UUID tenantId, String externalRef) {
        log.debug("[STUB] Statut paiement simulé (COMPLETED après 1s) : ref={}", externalRef);
        // Simule une confirmation automatique après 1 seconde
        return Mono.delay(Duration.ofSeconds(1))
                   .thenReturn(PaymentStatus.COMPLETED);
    }

    @Override
    public Mono<PaymentInitiationResult> initiateWithdrawal(
            UUID tenantId,
            UUID memberId,
            BigDecimal amount,
            String currency,
            String targetAccount,
            String provider,
            String idempotencyKey) {

        String externalRef = UUID.randomUUID().toString();
        String ussdCode = "*126*2*" + amount.toPlainString() + "#";

        log.debug("[STUB] Retrait simulé : tenantId={}, memberId={}, amount={} {}, ref={}",
                tenantId, memberId, amount, currency, externalRef);

        return Mono.just(new PaymentInitiationResult(
                externalRef,
                PaymentStatus.PENDING,
                null,
                ussdCode,
                Instant.now().plusSeconds(300)
        ));
    }

    @Override
    public Mono<Void> cancelPayment(UUID tenantId, String externalRef) {
        log.debug("[STUB] Annulation paiement simulée : ref={}", externalRef);
        return Mono.empty();
    }
}
