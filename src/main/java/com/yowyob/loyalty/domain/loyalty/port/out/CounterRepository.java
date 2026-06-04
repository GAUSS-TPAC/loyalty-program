package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.util.List;
import java.util.Optional;

public interface CounterRepository {
    Counter save(Counter counter);
    Optional<Counter> findByKey(TenantId tenantId, UserId memberId, String counterKey);
    List<Counter> findAllByMember(TenantId tenantId, UserId memberId);
}
