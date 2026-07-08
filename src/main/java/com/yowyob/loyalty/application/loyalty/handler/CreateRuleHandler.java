package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.EffectDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.model.rule.TriggerDefinition;
import com.yowyob.loyalty.domain.loyalty.port.in.CreateRuleUseCase;
import com.yowyob.loyalty.domain.loyalty.port.out.RuleCachePort;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

@Service
public class CreateRuleHandler {

    private final CreateRuleUseCase createRuleUseCase;
    private final RuleCachePort ruleCache;

    public CreateRuleHandler(CreateRuleUseCase createRuleUseCase, RuleCachePort ruleCache) {
        this.createRuleUseCase = createRuleUseCase;
        this.ruleCache = ruleCache;
    }

    public Mono<Rule> create(
            String name,
            String description,
            TriggerDefinition trigger,
            List<ConditionDefinition> conditions,
            List<EffectDefinition> effects,
            int priority,
            Instant validFrom,
            Instant validUntil
    ) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> Mono.fromCallable(() -> {
                    Rule rule = createRuleUseCase.createRule(
                            tenantId, name, description, trigger, conditions, effects,
                            priority, validFrom, validUntil
                    );
                    ruleCache.invalidateCache(tenantId);
                    return rule;
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}
