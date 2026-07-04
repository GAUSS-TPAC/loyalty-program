package com.yowyob.loyalty.infrastructure.persistence.webhook.repository;

import com.yowyob.loyalty.infrastructure.persistence.webhook.entity.WebhookDeliveryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

public interface WebhookDeliveryR2dbcRepository extends R2dbcRepository<WebhookDeliveryEntity, UUID> {
    Flux<WebhookDeliveryEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
    Flux<WebhookDeliveryEntity> findByStatusAndNextAttemptAtBefore(String status, Instant now);
}
