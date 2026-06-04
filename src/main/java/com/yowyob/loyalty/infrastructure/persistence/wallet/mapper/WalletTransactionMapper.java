package com.yowyob.loyalty.infrastructure.persistence.wallet.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionStatus;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WalletTransactionMapper {

    private final ObjectMapper objectMapper;

    public WalletTransactionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public WalletTransaction toDomain(WalletTransactionEntity entity) {
        return new WalletTransaction(
                entity.getId(),
                entity.getWalletId(),
                TenantId.of(entity.getTenantId()),
                TransactionType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getBalanceBefore(),
                entity.getBalanceAfter(),
                TransactionStatus.valueOf(entity.getStatus()),
                TransactionSource.valueOf(entity.getSource()),
                entity.getIdempotencyKey(),
                entity.getReferenceId(),
                entity.getReversalOf(),
                readMetadata(entity.getMetadata()),
                entity.getCreatedAt()
        );
    }

    public WalletTransactionEntity toEntity(WalletTransaction domain) {
        return WalletTransactionEntity.builder()
                .id(domain.id())
                .walletId(domain.walletId())
                .tenantId(domain.tenantId().value())
                .type(domain.type().name())
                .amount(domain.amount())
                .balanceBefore(domain.balanceBefore())
                .balanceAfter(domain.balanceAfter())
                .status(domain.status().name())
                .source(domain.source().name())
                .idempotencyKey(domain.idempotencyKey())
                .referenceId(domain.referenceId())
                .reversalOf(domain.reversalOf())
                .metadata(writeMetadata(domain.metadata()))
                .createdAt(domain.createdAt())
                .build();
    }

    private Map<String, Object> readMetadata(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
