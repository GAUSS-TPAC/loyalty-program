package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyaulty.program.domain.wallet.model.PaymentRequest;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentDirection;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentRequestStatus;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.PaymentRequestEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper bidirectionnel entre {@link PaymentRequest} (domaine)
 * et {@link PaymentRequestEntity} (infrastructure).
 */
@Component
public class PaymentRequestMapper {

    /**
     * Convertit une entité en modèle domaine.
     *
     * @param entity l'entité lue depuis {@code payment_requests}.
     * @return le modèle domaine {@link PaymentRequest}.
     */
    public PaymentRequest toDomain(PaymentRequestEntity entity) {
        if (entity == null) return null;

        return PaymentRequest.builder()
                .id(entity.getId())
                .walletId(entity.getWalletId())
                .tenantId(entity.getTenantId())
                .walletTransactionId(entity.getWalletTransactionId())
                .provider(PaymentProvider.valueOf(entity.getProvider()))
                .direction(PaymentDirection.valueOf(entity.getDirection()))
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .externalReference(entity.getExternalReference())
                .mobileMoneyPhoneNumber(entity.getMobileMoneyPhoneNumber())
                .status(PaymentRequestStatus.valueOf(entity.getStatus()))
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .createdAt(entity.getCreatedAt())
                .nextRetryAt(entity.getNextRetryAt())
                .expiresAt(entity.getExpiresAt())
                .resolvedAt(entity.getResolvedAt())
                .providerErrorMessage(entity.getProviderErrorMessage())
                .webhookPayload(entity.getWebhookPayload())
                .build();
    }

    /**
     * Convertit un modèle domaine en entité à persister.
     *
     * @param paymentRequest le modèle domaine.
     * @return l'entité R2DBC {@link PaymentRequestEntity}.
     */
    public PaymentRequestEntity toEntity(PaymentRequest paymentRequest) {
        if (paymentRequest == null) return null;

        return PaymentRequestEntity.builder()
                .id(paymentRequest.getId())
                .walletId(paymentRequest.getWalletId())
                .tenantId(paymentRequest.getTenantId())
                .walletTransactionId(paymentRequest.getWalletTransactionId())
                .provider(paymentRequest.getProvider().name())
                .direction(paymentRequest.getDirection().name())
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .externalReference(paymentRequest.getExternalReference())
                .mobileMoneyPhoneNumber(paymentRequest.getMobileMoneyPhoneNumber())
                .status(paymentRequest.getStatus().name())
                .retryCount(paymentRequest.getRetryCount())
                .maxRetries(paymentRequest.getMaxRetries())
                .createdAt(paymentRequest.getCreatedAt() != null ? paymentRequest.getCreatedAt() : Instant.now())
                .nextRetryAt(paymentRequest.getNextRetryAt())
                .expiresAt(paymentRequest.getExpiresAt())
                .resolvedAt(paymentRequest.getResolvedAt())
                .providerErrorMessage(paymentRequest.getProviderErrorMessage())
                .webhookPayload(paymentRequest.getWebhookPayload())
                .build();
    }
}
