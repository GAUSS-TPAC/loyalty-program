package com.yowyob.loyaulty.program.domain.rule.port.out;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CounterRepository {
    Mono<Long> increment(TenantId tenantId, String memberId, UUID ruleId);
    Mono<Void> reset(TenantId tenantId, String memberId, UUID ruleId);
    Mono<Map<UUID, Long>> getAll(TenantId tenantId, String memberId, List<UUID> ruleIds);
}
