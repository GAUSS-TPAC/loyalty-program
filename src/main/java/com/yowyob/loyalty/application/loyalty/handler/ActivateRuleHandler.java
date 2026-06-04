package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.port.in.ActivateRuleUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ActivateRuleHandler {

    private final ActivateRuleUseCase activateRuleUseCase;

    public ActivateRuleHandler(ActivateRuleUseCase activateRuleUseCase) {
        this.activateRuleUseCase = activateRuleUseCase;
    }

    public Mono<Rule> activate(UUID ruleId) {
        return TenantContextHolder.getTenantId()
                .map(tenantId -> activateRuleUseCase.activateRule(tenantId, ruleId));
    }
}
