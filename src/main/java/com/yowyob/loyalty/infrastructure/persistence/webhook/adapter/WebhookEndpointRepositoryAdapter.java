package com.yowyob.loyalty.infrastructure.persistence.webhook.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookEndpointRepository;
import com.yowyob.loyalty.infrastructure.persistence.webhook.entity.WebhookEndpointEntity;
import com.yowyob.loyalty.infrastructure.persistence.webhook.repository.WebhookEndpointR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class WebhookEndpointRepositoryAdapter implements WebhookEndpointRepository {

    private final WebhookEndpointR2dbcRepository r2dbc;
    private final R2dbcEntityTemplate template;

    public WebhookEndpointRepositoryAdapter(WebhookEndpointR2dbcRepository r2dbc, R2dbcEntityTemplate template) {
        this.r2dbc = r2dbc;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<WebhookEndpoint> save(WebhookEndpoint endpoint) {
        WebhookEndpointEntity entity = toEntity(endpoint);
        return r2dbc.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbc.save(entity) : template.insert(entity))
                .map(this::toDomain);
    }

    @Override
    public Mono<WebhookEndpoint> findByIdAndTenantId(UUID id, TenantId tenantId) {
        return r2dbc.findByIdAndTenantId(id, tenantId.value()).map(this::toDomain);
    }

    @Override
    public Flux<WebhookEndpoint> findAllByTenantId(TenantId tenantId) {
        return r2dbc.findByTenantId(tenantId.value()).map(this::toDomain);
    }

    @Override
    public Flux<WebhookEndpoint> findActiveByTenantId(TenantId tenantId) {
        return r2dbc.findByTenantIdAndActiveTrue(tenantId.value()).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByIdAndTenantId(UUID id, TenantId tenantId) {
        return r2dbc.deleteByIdAndTenantId(id, tenantId.value());
    }

    private WebhookEndpoint toDomain(WebhookEndpointEntity e) {
        return new WebhookEndpoint(e.getId(), TenantId.of(e.getTenantId()), e.getUrl(), e.getSecret(),
                e.getDescription(), splitEventTypes(e.getEventTypes()), e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private WebhookEndpointEntity toEntity(WebhookEndpoint w) {
        WebhookEndpointEntity e = new WebhookEndpointEntity();
        e.setId(w.id());
        e.setTenantId(w.tenantId().value());
        e.setUrl(w.url());
        e.setSecret(w.secret());
        e.setDescription(w.description());
        e.setEventTypes(String.join(",", w.eventTypes()));
        e.setActive(w.active());
        e.setCreatedAt(w.createdAt());
        e.setUpdatedAt(w.updatedAt());
        return e;
    }

    private List<String> splitEventTypes(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.asList(raw.split(","));
    }
}
