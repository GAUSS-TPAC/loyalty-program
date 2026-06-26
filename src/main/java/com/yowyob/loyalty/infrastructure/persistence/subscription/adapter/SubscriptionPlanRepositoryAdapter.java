package com.yowyob.loyalty.infrastructure.persistence.subscription.adapter;

import com.yowyob.loyalty.domain.subscription.model.SubscriptionPlan;
import com.yowyob.loyalty.domain.subscription.port.out.SubscriptionPlanRepository;
import com.yowyob.loyalty.infrastructure.persistence.subscription.mapper.SubscriptionMapper;
import com.yowyob.loyalty.infrastructure.persistence.subscription.repository.SubscriptionPlanR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class SubscriptionPlanRepositoryAdapter implements SubscriptionPlanRepository {

    private final SubscriptionPlanR2dbcRepository repository;
    private final SubscriptionMapper mapper;

    public SubscriptionPlanRepositoryAdapter(SubscriptionPlanR2dbcRepository repository, SubscriptionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<SubscriptionPlan> save(SubscriptionPlan plan) {
        return repository.save(mapper.toEntity(plan)).map(mapper::toDomain);
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
