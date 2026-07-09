package com.yowyob.loyalty.infrastructure.persistence.subscription.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.TenantSubscription;
import com.yowyob.loyalty.domain.subscription.port.out.TenantSubscriptionRepository;
import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.TenantSubscriptionEntity;
import com.yowyob.loyalty.infrastructure.persistence.subscription.mapper.SubscriptionMapper;
import com.yowyob.loyalty.infrastructure.persistence.subscription.repository.TenantSubscriptionR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class TenantSubscriptionRepositoryAdapter implements TenantSubscriptionRepository {

    private final TenantSubscriptionR2dbcRepository repository;
    private final SubscriptionMapper mapper;
    private final R2dbcEntityTemplate template;

    public TenantSubscriptionRepositoryAdapter(TenantSubscriptionR2dbcRepository repository, SubscriptionMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<TenantSubscription> save(TenantSubscription subscription) {
        TenantSubscriptionEntity entity = mapper.toEntity(subscription);
        return repository.existsById(entity.getId())
                .flatMap(exists -> exists ? repository.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<TenantSubscription> findByTenantId(TenantId tenantId) {
        return repository.findByTenantId(tenantId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<TenantSubscription> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<TenantSubscription> findExpiredTrials(Instant now) {
        return repository.findExpiredTrials(now).map(mapper::toDomain);
    }

    @Override
    public Flux<TenantSubscription> findExpiredActive(Instant now) {
        return repository.findExpiredActive(now).map(mapper::toDomain);
    }
}
