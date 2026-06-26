package com.yowyob.loyalty.infrastructure.persistence.wallet.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.port.out.WalletAuditLogRepository;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletAuditLogEntity;
import com.yowyob.loyalty.infrastructure.persistence.wallet.repository.WalletAuditLogR2dbcRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("!test & !stub & !dev")
public class WalletAuditLogRepositoryAdapter implements WalletAuditLogRepository {

    private final WalletAuditLogR2dbcRepository repository;
    private final ObjectMapper objectMapper;

    public WalletAuditLogRepositoryAdapter(WalletAuditLogR2dbcRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> log(UUID walletId, TenantId tenantId, String action, String actor, String reason, Map<String, Object> metadata) {
        String actorType = resolveActorType(actor);
        WalletAuditLogEntity entity = WalletAuditLogEntity.builder()
            .id(UUID.randomUUID())
            .walletId(walletId)
            .tenantId(tenantId.value())
            .actorId(actor != null ? actor : "SYSTEM")
            .actorType(actorType)
            .action(action)
            .reason(reason != null ? reason : action)
            .metadata(serializeMetadata(metadata))
            .occurredAt(Instant.now())
            .build();
        return repository.save(entity).then();
    }

    private String resolveActorType(String actor) {
        if (actor == null || "SYSTEM".equalsIgnoreCase(actor)) {
            return "SYSTEM";
        }
        return "ADMIN";
    }

    private String serializeMetadata(Map<String, Object> metadata) {
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
