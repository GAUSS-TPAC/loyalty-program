package com.yowyob.loyaulty.program.infrastructure.persistence.tenant.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.entity.TenantEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TenantR2dbcRepository extends ReactiveCrudRepository<TenantEntity, UUID> {

    Mono<TenantEntity> findBySlug(String slug);

    @Query("SELECT * FROM tenants WHERE status = 'ACTIVE'")
    Flux<TenantEntity> findAllActive();
}
