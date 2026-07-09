package com.yowyob.loyalty.infrastructure.persistence.webhook.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.webhook.model.DeliveryStatus;
import com.yowyob.loyalty.domain.webhook.model.WebhookDelivery;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookDeliveryRepository;
import com.yowyob.loyalty.infrastructure.persistence.webhook.entity.WebhookDeliveryEntity;
import com.yowyob.loyalty.infrastructure.persistence.webhook.repository.WebhookDeliveryR2dbcRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class WebhookDeliveryRepositoryAdapter implements WebhookDeliveryRepository {

    private final WebhookDeliveryR2dbcRepository r2dbc;
    private final R2dbcEntityTemplate template;

    public WebhookDeliveryRepositoryAdapter(WebhookDeliveryR2dbcRepository r2dbc, R2dbcEntityTemplate template) {
        this.r2dbc = r2dbc;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<WebhookDelivery> save(WebhookDelivery delivery) {
        WebhookDeliveryEntity entity = toEntity(delivery);
        return r2dbc.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbc.save(entity) : template.insert(entity))
                .map(this::toDomain);
    }

    @Override
    public Flux<WebhookDelivery> findAllByTenantId(TenantId tenantId, int page, int size) {
        return r2dbc.findByTenantIdOrderByCreatedAtDesc(tenantId.value(), PageRequest.of(page, size)).map(this::toDomain);
    }

    @Override
    public Flux<WebhookDelivery> findDueForRetry(DeliveryStatus status, Instant now) {
        return r2dbc.findByStatusAndNextAttemptAtBefore(status.name(), now).map(this::toDomain);
    }

    private WebhookDelivery toDomain(WebhookDeliveryEntity e) {
        return new WebhookDelivery(e.getId(), TenantId.of(e.getTenantId()), e.getEndpointId(), e.getEventType(),
                e.getPayload(), DeliveryStatus.valueOf(e.getStatus()), e.getHttpStatusCode(), e.getResponseSnippet(),
                e.getAttemptCount(), e.getNextAttemptAt(), e.getCreatedAt(), e.getDeliveredAt());
    }

    private WebhookDeliveryEntity toEntity(WebhookDelivery d) {
        WebhookDeliveryEntity e = new WebhookDeliveryEntity();
        e.setId(d.id());
        e.setTenantId(d.tenantId().value());
        e.setEndpointId(d.endpointId());
        e.setEventType(d.eventType());
        e.setPayload(d.payload());
        e.setStatus(d.status().name());
        e.setHttpStatusCode(d.httpStatusCode());
        e.setResponseSnippet(d.responseSnippet());
        e.setAttemptCount(d.attemptCount());
        e.setNextAttemptAt(d.nextAttemptAt());
        e.setCreatedAt(d.createdAt());
        e.setDeliveredAt(d.deliveredAt());
        return e;
    }
}
