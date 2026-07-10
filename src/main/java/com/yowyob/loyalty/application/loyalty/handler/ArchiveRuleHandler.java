package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.rule.Rule;
import com.yowyob.loyalty.domain.loyalty.port.in.ArchiveRuleUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
public class ArchiveRuleHandler {

    private final ArchiveRuleUseCase archiveRuleUseCase;

    public ArchiveRuleHandler(ArchiveRuleUseCase archiveRuleUseCase) {
        this.archiveRuleUseCase = archiveRuleUseCase;
    }

    public Mono<Rule> archive(UUID ruleId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> Mono.fromCallable(() -> archiveRuleUseCase.archiveRule(tenantId, ruleId))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
