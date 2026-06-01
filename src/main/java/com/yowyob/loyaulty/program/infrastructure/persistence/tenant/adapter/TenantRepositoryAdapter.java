package com.yowyob.loyaulty.program.infrastructure.persistence.tenant.adapter;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.mapper.TenantMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.repository.TenantR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TenantRepositoryAdapter implements TenantRepository {

    private final TenantR2dbcRepository repository;
    private final TenantMapper mapper;

    public TenantRepositoryAdapter(TenantR2dbcRepository repository, TenantMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Tenant> findById(TenantId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findBySlug(String slug) {
        return repository.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(TenantId id) {
        return repository.existsById(id.value());
    }

    @Override
    public Mono<Tenant> save(Tenant tenant) {
        return repository.save(mapper.toEntity(tenant)).map(mapper::toDomain);
    }

    @Override
    public Flux<Tenant> findAllActive() {
        return repository.findAllActive().map(mapper::toDomain);
    }
}
