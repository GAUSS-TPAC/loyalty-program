package com.yowyob.loyalty.infrastructure.persistence.loyalty.repository;

import com.yowyob.loyalty.infrastructure.persistence.loyalty.entity.MemberTierEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberTierR2dbcRepository extends ReactiveCrudRepository<MemberTierEntity, UUID> {

    Mono<MemberTierEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);
}
