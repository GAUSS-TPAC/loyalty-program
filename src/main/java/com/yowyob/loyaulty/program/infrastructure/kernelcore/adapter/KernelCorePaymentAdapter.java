package com.yowyob.loyaulty.program.infrastructure.kernelcore.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentInitiationResult;
import com.yowyob.loyaulty.program.domain.wallet.model.PaymentStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.PaymentGatewayPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Placeholder pour l'adapter Kernel Core — Payment API.
 *
 * <p>ACTIF uniquement quand le profil {@code stub} N'EST PAS actif.
 * Ce fichier est un squelette indiquant le travail à faire quand
 * le Kernel Core sera disponible.</p>
 *
 * <h2>Pour implémenter ce adapter :</h2>
 * <ol>
 *   <li>Injecter {@code KernelCoreProperties} et un {@code WebClient}.</li>
 *   <li>Implémenter l'authentification OAuth2 client_credentials.</li>
 *   <li>Remplacer chaque {@code throw} par un appel WebClient vers le Payment API.</li>
 * </ol>
 */
@Component
@Profile("!stub")
public class KernelCorePaymentAdapter implements PaymentGatewayPort {

    @Override
    public Mono<PaymentInitiationResult> initiateTopUp(
            UUID tenantId, UUID memberId, BigDecimal amount,
            String currency, String provider, String idempotencyKey) {
        // TODO: POST {KERNEL_CORE_URL}/api/v1/payments/top-up
        //       Body: { tenantId, memberId, amount, currency, provider, idempotencyKey }
        //       Retourner PaymentInitiationResult depuis la réponse
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: POST /api/v1/payments/top-up");
    }

    @Override
    public Mono<PaymentStatus> getPaymentStatus(UUID tenantId, String externalRef) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/payments/{externalRef}/status
        //       Mapper le statut retourné vers PaymentStatus
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/payments/{externalRef}/status");
    }

    @Override
    public Mono<PaymentInitiationResult> initiateWithdrawal(
            UUID tenantId, UUID memberId, BigDecimal amount,
            String currency, String targetAccount, String provider, String idempotencyKey) {
        // TODO: POST {KERNEL_CORE_URL}/api/v1/payments/withdrawal
        //       Body: { tenantId, memberId, amount, currency, targetAccount, provider, idempotencyKey }
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: POST /api/v1/payments/withdrawal");
    }

    @Override
    public Mono<Void> cancelPayment(UUID tenantId, String externalRef) {
        // TODO: POST {KERNEL_CORE_URL}/api/v1/payments/{externalRef}/cancel
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: POST /api/v1/payments/{externalRef}/cancel");
    }
}
