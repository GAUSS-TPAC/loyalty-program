package com.yowyob.loyalty.infrastructure.persistence.tenant.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyalty.infrastructure.persistence.tenant.mapper.TenantMapper;
import com.yowyob.loyalty.infrastructure.persistence.tenant.repository.TenantR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TenantRepositoryAdapter implements TenantRepository {

    private final TenantR2dbcRepository r2dbcRepo;
    private final TenantMapper mapper;

    public TenantRepositoryAdapter(TenantR2dbcRepository r2dbcRepo, TenantMapper mapper) {
        this.r2dbcRepo = r2dbcRepo;
        this.mapper = mapper;
    }

    @Override
    public Mono<Tenant> findById(TenantId id) {
        return r2dbcRepo.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findBySlug(String slug) {
        return r2dbcRepo.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(TenantId id) {
        return r2dbcRepo.existsById(id.value());
    }

    @Override
    public Mono<Tenant> save(Tenant tenant) {
        return Mono.just(tenant)
                .map(mapper::toEntity)
                .flatMap(r2dbcRepo::save)
                .map(mapper::toDomain);
    }
}
