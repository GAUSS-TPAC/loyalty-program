package com.yowyob.loyalty.infrastructure.persistence.tenant.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import com.yowyob.loyalty.domain.tenant.port.out.ApiKeyRepository;
import com.yowyob.loyalty.infrastructure.persistence.tenant.entity.ApiKeyEntity;
import com.yowyob.loyalty.infrastructure.persistence.tenant.repository.ApiKeyR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

    private final ApiKeyR2dbcRepository r2dbc;
    private final R2dbcEntityTemplate template;

    public ApiKeyRepositoryAdapter(ApiKeyR2dbcRepository r2dbc, R2dbcEntityTemplate template) {
        this.r2dbc = r2dbc;
        this.template = template;
    }

    @Override
    public Mono<ApiKey> findByKeyHash(String keyHash) {
        return r2dbc.findByKeyHashAndActiveTrue(keyHash).map(this::toDomain);
    }

    @Override
    public Flux<ApiKey> findByTenantId(TenantId tenantId) {
        return r2dbc.findByTenantId(tenantId.value()).map(this::toDomain);
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<ApiKey> save(ApiKey key) {
        ApiKeyEntity entity = toEntity(key);
        return r2dbc.existsById(entity.getId())
                .flatMap(exists -> exists ? r2dbc.save(entity) : template.insert(entity))
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbc.deleteById(id);
    }

    private ApiKey toDomain(ApiKeyEntity e) {
        ApiKeyMode mode = e.getMode() != null ? ApiKeyMode.valueOf(e.getMode()) : ApiKeyMode.LIVE;
        return new ApiKey(e.getId(), TenantId.of(e.getTenantId()), e.getName(),
                e.getKeyHash(), e.getKeyPrefix(), mode, e.isActive(), e.getCreatedAt(), e.getLastUsedAt());
    }

    private ApiKeyEntity toEntity(ApiKey k) {
        ApiKeyEntity e = new ApiKeyEntity();
        e.setId(k.id());
        e.setTenantId(k.tenantId().value());
        e.setName(k.name());
        e.setKeyHash(k.keyHash());
        e.setKeyPrefix(k.keyPrefix());
        e.setMode(k.mode() != null ? k.mode().name() : ApiKeyMode.LIVE.name());
        e.setActive(k.active());
        e.setCreatedAt(k.createdAt());
        e.setLastUsedAt(k.lastUsedAt());
        return e;
    }
}
