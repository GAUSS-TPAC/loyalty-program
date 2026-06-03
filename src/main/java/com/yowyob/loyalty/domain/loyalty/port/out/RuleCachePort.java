package com.yowyob.loyalty.domain.loyalty.port.out;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.shared.model.TenantId;

import java.util.List;

public interface RuleCachePort {
    List<Rule> getCachedRules(TenantId tenantId, String eventType);
    void cacheRules(TenantId tenantId, String eventType, List<Rule> rules);
    void invalidateCache(TenantId tenantId);
}
