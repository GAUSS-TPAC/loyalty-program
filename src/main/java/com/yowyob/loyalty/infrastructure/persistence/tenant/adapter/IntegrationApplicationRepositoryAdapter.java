package com.yowyob.loyalty.infrastructure.persistence.tenant.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import com.yowyob.loyalty.domain.tenant.port.out.IntegrationApplicationRepository;
import com.yowyob.loyalty.infrastructure.persistence.tenant.entity.IntegrationApplicationEntity;
import com.yowyob.loyalty.infrastructure.persistence.tenant.repository.IntegrationApplicationR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class IntegrationApplicationRepositoryAdapter implements IntegrationApplicationRepository {

    private final IntegrationApplicationR2dbcRepository r2dbc;
    private final R2dbcEntityTemplate template;

    public IntegrationApplicationRepositoryAdapter(IntegrationApplicationR2dbcRepository r2dbc,
                                                   R2dbcEntityTemplate template) {
        this.r2dbc = r2dbc;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<IntegrationApplication> save(IntegrationApplication application) {
        IntegrationApplicationEntity entity = toEntity(application);
        return r2dbc.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbc.save(entity) : template.insert(entity))
                .map(this::toDomain);
    }

    @Override
    public Flux<IntegrationApplication> findAllByTenantId(TenantId tenantId) {
        return r2dbc.findByTenantId(tenantId.value()).map(this::toDomain);
    }

    @Override
    public Mono<IntegrationApplication> findByIdAndTenantId(UUID id, TenantId tenantId) {
        return r2dbc.findByIdAndTenantId(id, tenantId.value()).map(this::toDomain);
    }

    @Override
    public Mono<IntegrationApplication> findByPublicKey(String publicKey) {
        return r2dbc.findByPublicKey(publicKey).map(this::toDomain);
    }

    @Override
    public Mono<IntegrationApplication> findByWebhookEndpointId(UUID webhookEndpointId) {
        return r2dbc.findByWebhookEndpointId(webhookEndpointId).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByIdAndTenantId(UUID id, TenantId tenantId) {
        return r2dbc.deleteByIdAndTenantId(id, tenantId.value());
    }

    private IntegrationApplication toDomain(IntegrationApplicationEntity e) {
        ApiKeyMode mode = e.getMode() != null ? ApiKeyMode.valueOf(e.getMode()) : ApiKeyMode.LIVE;
        return new IntegrationApplication(e.getId(), TenantId.of(e.getTenantId()), e.getName(),
                e.getDescription(), e.getWebsiteUrl(), e.getLogoUrl(), e.getPublicKey(),
                e.getApiKeyId(), e.getWebhookEndpointId(), mode, e.isActive(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private IntegrationApplicationEntity toEntity(IntegrationApplication a) {
        IntegrationApplicationEntity e = new IntegrationApplicationEntity();
        e.setId(a.id());
        e.setTenantId(a.tenantId().value());
        e.setName(a.name());
        e.setDescription(a.description());
        e.setWebsiteUrl(a.websiteUrl());
        e.setLogoUrl(a.logoUrl());
        e.setPublicKey(a.publicKey());
        e.setApiKeyId(a.apiKeyId());
        e.setWebhookEndpointId(a.webhookEndpointId());
        e.setMode(a.mode() != null ? a.mode().name() : ApiKeyMode.LIVE.name());
        e.setActive(a.active());
        e.setCreatedAt(a.createdAt());
        e.setUpdatedAt(a.updatedAt());
        return e;
    }
}
