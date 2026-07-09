package com.yowyob.loyalty.infrastructure.persistence.subscription.adapter;

import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import com.yowyob.loyalty.domain.subscription.port.out.SubscriptionPlanRepository;
import com.yowyob.loyalty.infrastructure.persistence.subscription.entity.SubscriptionPlanEntity;
import com.yowyob.loyalty.infrastructure.persistence.subscription.mapper.SubscriptionMapper;
import com.yowyob.loyalty.infrastructure.persistence.subscription.repository.SubscriptionPlanR2dbcRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class SubscriptionPlanRepositoryAdapter implements SubscriptionPlanRepository {

    private final SubscriptionPlanR2dbcRepository repository;
    private final SubscriptionMapper mapper;
    private final R2dbcEntityTemplate template;

    public SubscriptionPlanRepositoryAdapter(SubscriptionPlanR2dbcRepository repository, SubscriptionMapper mapper, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.mapper = mapper;
        this.template = template;
    }

    // Client-generated UUID id + no Persistable => save() always issues UPDATE. Decide
    // insert vs update explicitly (see RuleRepositoryAdapter for the full explanation).
    @Override
    public Mono<SubscriptionPlan> save(SubscriptionPlan plan) {
        SubscriptionPlanEntity entity = mapper.toEntity(plan);
        return repository.existsById(entity.getId())
                .flatMap(exists -> exists ? repository.save(entity) : template.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<SubscriptionPlan> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<SubscriptionPlan> findByCode(String code) {
        return repository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Flux<SubscriptionPlan> findAllActive() {
        return repository.findAllByActiveTrue().map(mapper::toDomain);
    }

    @Override
    public Flux<SubscriptionPlan> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }
}
