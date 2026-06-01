package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper bidirectionnel entre {@link WalletTransaction} (domaine)
 * et {@link WalletTransactionEntity} (infrastructure).
 */
@Component
public class WalletTransactionMapper {

    /**
     * Convertit une entité en modèle domaine.
     *
     * @param entity l'entité lue depuis {@code wallet_transactions}.
     * @return le modèle domaine {@link WalletTransaction}.
     */
    public WalletTransaction toDomain(WalletTransactionEntity entity) {
        if (entity == null) return null;

        return WalletTransaction.builder()
                .id(entity.getId())
                .walletId(entity.getWalletId())
                .tenantId(entity.getTenantId())
                .idempotencyKey(entity.getIdempotencyKey())
                .type(TransactionType.valueOf(entity.getType()))
                .source(TransactionSource.valueOf(entity.getSource()))
                .status(TransactionStatus.valueOf(entity.getStatus()))
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .balanceAfter(entity.getBalanceAfter())
                .paymentRequestId(entity.getPaymentRequestId())
                .originalTransactionId(entity.getOriginalTransactionId())
                .description(entity.getDescription())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    /**
     * Convertit un modèle domaine en entité à persister.
     *
     * <p>Les transactions sont toujours créées avec un {@code created_at} géré
     * côté application pour cohérence avec l'horodatage des événements Kafka.</p>
     *
     * @param transaction le modèle domaine.
     * @return l'entité R2DBC prête à être sauvegardée.
     */
    public WalletTransactionEntity toEntity(WalletTransaction transaction) {
        if (transaction == null) return null;

        return WalletTransactionEntity.builder()
                .id(transaction.getId())
                .walletId(transaction.getWalletId())
                .tenantId(transaction.getTenantId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .type(transaction.getType().name())
                .source(transaction.getSource().name())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceAfter(transaction.getBalanceAfter())
                .paymentRequestId(transaction.getPaymentRequestId())
                .originalTransactionId(transaction.getOriginalTransactionId())
                .description(transaction.getDescription())
                .metadata(transaction.getMetadata())
                .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt() : Instant.now())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
